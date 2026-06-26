package com.example.train_ticket_booking_system.ai

import com.example.train_ticket_booking_system.data.entity.Station
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import com.example.train_ticket_booking_system.data.repository.StationRepository
import com.example.train_ticket_booking_system.data.repository.TrainRepository

class ServiceRouter(
    private val stationRepo: StationRepository,
    private val trainRepo: TrainRepository,
    private val orderRepo: OrderRepository
) {
    suspend fun execute(intent: ParsedIntent, userPhone: String): String {
        return when (intent.type) {
            IntentType.SEARCH_TRAINS -> handleSearch(intent.params)
            IntentType.QUERY_ORDERS -> handleQueryOrders(userPhone)
            IntentType.CANCEL_ORDER -> "请前往订单详情页面操作退票或改签。"
            IntentType.BOOK_TICKET -> "请使用首页的搜索功能进行购票：选择出发站、到达站和日期后查询车次。"
            IntentType.ADD_PASSENGER -> "请前往「我的」→「常用乘客」添加乘客信息。"
            IntentType.UNKNOWN -> "我没有理解您的意思。您可以尝试：\n- 查北京到上海的火车\n- 查看我的订单\n- 退票"
        }
    }

    private suspend fun handleSearch(params: Map<String, String>): String {
        val from = params["from"] ?: return "请说明出发城市。"
        val to = params["to"] ?: return "请说明目的城市。"
        val fromStations = stationRepo.search(from)
        val toStations = stationRepo.search(to)
        if (fromStations.isEmpty()) return "未找到出发站「$from」。"
        if (toStations.isEmpty()) return "未找到到达站「$to」。"

        val trains = trainRepo.search(fromStations.first().id, toStations.first().id)
        if (trains.isEmpty()) return "未找到${fromStations.first().name}到${toStations.first().name}的车次。"
        return trains.take(3).joinToString("\n") { "${it.train.number}(${it.train.type}) ${it.stops.first().departureTime}-${it.stops.last().arrivalTime}" }
    }

    private suspend fun handleQueryOrders(userPhone: String): String {
        val orders = orderRepo.getOrdersByUser(userPhone)
        if (orders.isEmpty()) return "您暂无订单记录。"
        return orders.take(5).joinToString("\n") {
            "${it.trainNumber} ${it.departureStationName}→${it.arrivalStationName} ${it.departureDate} ${it.status}"
        }
    }
}
