package com.example.train_ticket_booking_system.ui.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.train_ticket_booking_system.TTBSApplication
import com.example.train_ticket_booking_system.ai.IntentParser
import com.example.train_ticket_booking_system.ai.ParsedIntent
import com.example.train_ticket_booking_system.ai.ServiceRouter
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import com.example.train_ticket_booking_system.data.repository.StationRepository
import com.example.train_ticket_booking_system.data.repository.TrainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val isUser: Boolean, val text: String)

data class AIChatState(
    val messages: List<ChatMessage> = emptyList()
)

class AIChatViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as TTBSApplication).database
    private val router = ServiceRouter(
        StationRepository(db.stationDao()),
        TrainRepository(db.trainDao()),
        OrderRepository(db.orderDao(), db.seatTypeDao())
    )

    private val _state = MutableStateFlow(AIChatState())
    val state: StateFlow<AIChatState> = _state

    fun sendMessage(text: String, userPhone: String) {
        val msgs = _state.value.messages.toMutableList()
        msgs.add(ChatMessage(true, text))
        _state.value = _state.value.copy(messages = msgs)

        viewModelScope.launch {
            val intent = IntentParser.parse(text)
            val response = router.execute(intent, userPhone)
            msgs.add(ChatMessage(false, response))
            _state.value = _state.value.copy(messages = msgs)
        }
    }
}
