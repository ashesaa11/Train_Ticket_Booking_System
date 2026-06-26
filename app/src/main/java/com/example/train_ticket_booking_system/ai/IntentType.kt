package com.example.train_ticket_booking_system.ai

enum class IntentType {
    SEARCH_TRAINS,
    BOOK_TICKET,
    QUERY_ORDERS,
    CANCEL_ORDER,
    ADD_PASSENGER,
    UNKNOWN
}

data class ParsedIntent(
    val type: IntentType,
    val params: Map<String, String> = emptyMap()
)
