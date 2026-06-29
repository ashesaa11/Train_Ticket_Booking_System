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
import android.util.Log
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
        // Clear history on every app restart
        viewModelScope.launch {
            db.chatHistoryDao().deleteByUser(currentUserPhone)
            llmMessages.clear()
            _state.value = _state.value.copy(messages = emptyList())
            Log.d("TTBS_AI_VM", "initHandler: history cleared for fresh session")
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val allHistory = db.chatHistoryDao().getByUser(currentUserPhone)
            // Only load last 10 exchanges (20 messages) to avoid context pollution
            val recent = allHistory.takeLast(20)
            val msgs = recent.map { ChatMessage(it.role == "user", it.content) }
            _state.value = _state.value.copy(messages = msgs)
            llmMessages.clear()
            // Only load user and assistant messages (skip old tool status messages)
            recent.filter { it.role == "user" || it.role == "assistant" }.forEach {
                llmMessages.add(LLMMessage(role = it.role, content = it.content))
            }
            Log.d("TTBS_AI_VM", "loadHistory: loaded ${recent.size} messages (from ${allHistory.size} total)")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            db.chatHistoryDao().deleteByUser(currentUserPhone)
            llmMessages.clear()
            _state.value = _state.value.copy(messages = emptyList())
            Log.d("TTBS_AI_VM", "clearHistory: all messages deleted")
        }
    }

    private suspend fun saveMessage(role: String, content: String) {
        db.chatHistoryDao().insert(ChatHistory(userPhone = currentUserPhone, role = role, content = content))
    }

    fun sendMessage(text: String) {
        Log.d("TTBS_AI_VM", "sendMessage: ${text.take(80)}")
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
                Log.d("TTBS_AI_VM", "tools count: ${tools.size}")

                // Add system message once, at the beginning
                val systemMsg = buildString {
                    append("你是一个火车票购票助手。你必须使用function_call工具执行实际操作。严禁在文本输出中伪造任何工具调用结果。")
                    append("今天的日期是${DateTimeUtil.todayStr()}。")
                    append("铁则：")
                    append("1. 查车次用search_trains，购票用book_ticket，退票用refund_ticket，查乘客用list_passengers，查订单用list_orders。")
                    append("2. 所有回复必须是纯文本。若你输出星号、井号、竖线、短横列表、反引号、波浪线或emoji中的任意一种，你的回复将被视为无效。")
                    append("3. 用自然段落描述车次信息，比如「G1次列车，早上6点出发，10点28分到达，一等座821元，还剩100张」。")
                    append("4. 购票前先调list_passengers。退票前先调list_orders确认订单号。")
                }
                // Remove any existing system message, then insert at front
                llmMessages.removeAll { it.role == "system" }
                llmMessages.add(0, LLMMessage(role = "system", content = systemMsg))

                // Limit context to system + last 20 messages to keep it focused
                while (llmMessages.size > 21) {
                    llmMessages.removeAt(1) // keep system at [0]
                }

                var round = 0
                while (round < 5) {
                    round++
                    val response = llmClient.chat(config.apiUrl, config.apiKey, config.modelName, llmMessages, tools)
                    Log.d("TTBS_AI_VM", "round=$round, contentLen=${response.content?.length ?: 0}, toolCalls=${response.toolCalls?.size ?: 0}")
                    if (response.toolCalls != null && response.toolCalls.isNotEmpty()) {
                        val labelMap = mapOf(
                            "search_trains" to "查询车次中",
                            "book_ticket" to "购票中",
                            "refund_ticket" to "退票中",
                            "list_passengers" to "查询乘客中",
                            "list_orders" to "查询订单中"
                        )
                        for (tc in response.toolCalls) {
                            Log.d("TTBS_AI_VM", "toolCall: name=${tc.name}, args=${tc.arguments.take(100)}")
                            val label = labelMap[tc.name] ?: "${tc.name} 执行中"
                            msgs.add(ChatMessage(false, "$label..."))
                            _state.value = _state.value.copy(messages = msgs.toList())
                            // Do NOT save tool status to history - it pollutes LLM context

                            llmMessages.add(LLMMessage(role = "assistant", content = null, toolCalls = listOf(tc)))

                            val result = h.execute(tc.name, tc.arguments)
                            llmMessages.add(LLMMessage(role = "tool", content = result, toolCallId = tc.id))

                            val done = "${labelMap[tc.name] ?: "操作"} 完成"
                            msgs.add(ChatMessage(false, done))
                            _state.value = _state.value.copy(messages = msgs.toList())
                            // Do NOT save tool status to history
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
                Log.e("TTBS_AI_VM", "sendMessage error", e)
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
