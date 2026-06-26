package com.example.train_ticket_booking_system.ai

object IntentParser {
    fun parse(input: String): ParsedIntent {
        val text = input.trim()
        return when {
            text.contains("查") && (text.contains("到") || text.contains("去")) -> parseSearchIntent(text)
            text.contains("订") || text.contains("买") || text.contains("购") -> ParsedIntent(IntentType.BOOK_TICKET)
            text.contains("订单") || text.contains("已购") -> ParsedIntent(IntentType.QUERY_ORDERS)
            text.contains("退") || text.contains("取消") -> ParsedIntent(IntentType.CANCEL_ORDER)
            text.contains("添加乘客") || text.contains("加乘客") -> ParsedIntent(IntentType.ADD_PASSENGER)
            else -> ParsedIntent(IntentType.UNKNOWN)
        }
    }

    private fun parseSearchIntent(text: String): ParsedIntent {
        // Pattern: "查北京到上海" or "北京去上海"
        val params = mutableMapOf<String, String>()
        val regex = Regex("""(查|从)?(.+?)(到|去|至)(.+)""")
        val match = regex.find(text)
        if (match != null) {
            val from = match.groupValues[2].trim()
            val to = match.groupValues[4].trim()
            params["from"] = from
            params["to"] = to
        }
        return ParsedIntent(IntentType.SEARCH_TRAINS, params)
    }
}
