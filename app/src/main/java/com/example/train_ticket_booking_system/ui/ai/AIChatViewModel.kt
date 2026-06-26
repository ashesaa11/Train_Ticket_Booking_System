package com.example.train_ticket_booking_system.ui.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.train_ticket_booking_system.TTBSApplication
import com.example.train_ticket_booking_system.ai.LLMClient
import com.example.train_ticket_booking_system.ai.LLMMessage
import com.example.train_ticket_booking_system.ai.FunctionCallHandler
import com.example.train_ticket_booking_system.data.ApiConfigStore
import com.example.train_ticket_booking_system.data.entity.ChatHistory
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import com.example.train_ticket_booking_system.data.repository.PassengerRepository
import com.example.train_ticket_booking_system.data.repository.StationRepository
import com.example.train_ticket_booking_system.data.repository.TrainRepository
import com.example.train_ticket_booking_system.util.DateTimeUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ChatMessage(val isUser: Boolean, val text: String)

data class AIChatState(
    val messages: List<ChatMessage> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val showConfig: Boolean = false,
    val apiConfigured: Boolean = false
)

class AIChatViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as TTBSApplication).database
    private val configStore = ApiConfigStore(application)
    private val llmClient = LLMClient()
    private var handler: FunctionCallHandler? = null
    private val llmMessages = mutableListOf<LLMMessage>()
    private var currentUserPhone = ""

    private val _state = MutableStateFlow(AIChatState())
    val state: StateFlow<AIChatState> = _state

    init {
        viewModelScope.launch {
            val config = configStore.config.first()
            _state.value = _state.value.copy(apiConfigured = config.apiKey.isNotBlank())
        }
    }

    fun initHandler(userPhone: String) {
        currentUserPhone = userPhone
        handler = FunctionCallHandler(
            StationRepository(db.stationDao()),
            TrainRepository(db.trainDao()),
            OrderRepository(db.orderDao(), db.seatTypeDao()),
            PassengerRepository(db.passengerDao()),
            userPhone
        )
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val history = db.chatHistoryDao().getByUser(currentUserPhone)
            val msgs = history.map { ChatMessage(it.role == "user", it.content) }
            _state.value = _state.value.copy(messages = msgs)
            // Also rebuild LLM messages for context
            llmMessages.clear()
            history.forEach { llmMessages.add(LLMMessage(role = it.role, content = it.content)) }
        }
    }

    private suspend fun saveMessage(role: String, content: String) {
        db.chatHistoryDao().insert(ChatHistory(userPhone = currentUserPhone, role = role, content = content))
    }

    fun sendMessage(text: String) {
        val msgs = _state.value.messages.toMutableList()
        msgs.add(ChatMessage(true, text))
        _state.value = _state.value.copy(messages = msgs, loading = true, error = null)
        viewModelScope.launch { saveMessage("user", text) }

        llmMessages.add(LLMMessage(role = "user", content = text))

        viewModelScope.launch {
            try {
                val config = configStore.config.first()
                if (config.apiKey.isBlank()) {
                    _state.value = _state.value.copy(loading = false, error = "请先配置API KEY")
                    return@launch
                }

                val h = handler ?: run {
                    _state.value = _state.value.copy(loading = false, error = "Handler未初始化")
                    return@launch
                }
                val tools = h.getToolDefinitions()

                val systemMsg = buildString {
                    append("你是一个火车票购票助手。你可以使用提供的工具来帮助用户查询车次、购票和退票。")
                    append("今天的日期是${DateTimeUtil.todayStr()}。")
                    append("重要规则：")
                    append("1. 购票时必须使用用户常用乘客列表中的姓名，不要编造乘客。")
                    append("2. 退票前应告知用户退票费金额并请求确认。")
                    append("3. 查询车次后列出关键信息（车次ID、车次号、时间、座位和价格）。")
                    append("4. 所有操作成功后给出清晰的成功提示。")
                    append("5. 使用中文回复，不要使用emoji表情符号。")
                    append("6. 不要使用Markdown语法（如**加粗**、`代码块`、#标题等），纯文本回复。")
                }
                llmMessages.add(0, LLMMessage(role = "system", content = systemMsg))

                var round = 0
                while (round < 5) {
                    round++
                    val response = llmClient.chat(config.apiUrl, config.apiKey, config.modelName, llmMessages, tools)
                    if (response.toolCalls != null && response.toolCalls.isNotEmpty()) {
                        for (tc in response.toolCalls) {
                            val status = "[${tc.name}] 执行中..."
                            msgs.add(ChatMessage(false, status))
                            _state.value = _state.value.copy(messages = msgs.toList())
                            viewModelScope.launch { saveMessage("assistant", status) }

                            llmMessages.add(LLMMessage(role = "assistant", content = null, toolCalls = listOf(tc)))

                            val result = h.execute(tc.name, tc.arguments)
                            llmMessages.add(LLMMessage(role = "tool", content = result, toolCallId = tc.id))

                            val done = "[${tc.name}] 完成"
                            msgs.add(ChatMessage(false, done))
                            _state.value = _state.value.copy(messages = msgs.toList())
                            viewModelScope.launch { saveMessage("assistant", done) }
                        }
                    } else if (response.content != null) {
                        msgs.add(ChatMessage(false, response.content))
                        llmMessages.add(LLMMessage(role = "assistant", content = response.content))
                        _state.value = _state.value.copy(messages = msgs.toList(), loading = false)
                        viewModelScope.launch { saveMessage("assistant", response.content) }
                        return@launch
                    } else {
                        msgs.add(ChatMessage(false, "AI未返回有效回复"))
                        _state.value = _state.value.copy(messages = msgs.toList(), loading = false)
                        return@launch
                    }
                }
                msgs.add(ChatMessage(false, "对话轮次超限，请重试"))
                _state.value = _state.value.copy(messages = msgs.toList(), loading = false)
            } catch (e: Exception) {
                val errMsg = when {
                    e.message?.contains("API error") == true -> e.message!!
                    e.message?.contains("Unable to resolve host") == true -> "网络连接失败，请检查API地址和网络"
                    e.message?.contains("timeout") == true -> "请求超时，请重试"
                    else -> "请求失败: ${e.message}"
                }
                msgs.add(ChatMessage(false, "错误: $errMsg"))
                _state.value = _state.value.copy(messages = msgs.toList(), loading = false, error = null)
            }
        }
    }

    fun saveApiConfig(url: String, key: String, model: String) {
        viewModelScope.launch {
            configStore.saveConfig(com.example.train_ticket_booking_system.data.ApiConfig(url, key, model))
            _state.value = _state.value.copy(apiConfigured = key.isNotBlank(), showConfig = false)
        }
    }

    fun showConfig() { _state.value = _state.value.copy(showConfig = true) }
    fun hideConfig() { _state.value = _state.value.copy(showConfig = false) }
}
