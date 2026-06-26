package com.example.train_ticket_booking_system.data.repository

import com.example.train_ticket_booking_system.data.dao.OrderDao
import com.example.train_ticket_booking_system.data.dao.SeatTypeDao
import com.example.train_ticket_booking_system.data.entity.OrderItem
import com.example.train_ticket_booking_system.data.entity.SeatType
import com.example.train_ticket_booking_system.data.entity.TrainOrder
import org.json.JSONObject

class OrderRepository(
    private val orderDao: OrderDao,
    private val seatTypeDao: SeatTypeDao
) {
    suspend fun createOrder(
        userPhone: String,
        trainId: Long,
        trainNumber: String,
        departureStationName: String,
        arrivalStationName: String,
        departureDate: String,
        departureTime: String,
        arrivalTime: String,
        totalPrice: Double,
        items: List<OrderItem>
    ): Long {
        val order = TrainOrder(
            userPhone = userPhone,
            trainId = trainId,
            trainNumber = trainNumber,
            departureStationName = departureStationName,
            arrivalStationName = arrivalStationName,
            departureDate = departureDate,
            departureTime = departureTime,
            arrivalTime = arrivalTime,
            status = "未出行",
            totalPrice = totalPrice
        )
        val orderId = orderDao.insertOrder(order)
        val orderItems = items.map { it.copy(orderId = orderId) }
        orderDao.insertItems(orderItems)

        // Update seat inventory
        for (item in items) {
            val seatTypes = seatTypeDao.getByTrainId(trainId)
            val seatType = seatTypes.find { it.typeName == item.seatType }
            if (seatType != null) {
                val sold = JSONObject(seatType.dailySold)
                val current = sold.optInt(departureDate, 0)
                sold.put(departureDate, current + 1)
                seatTypeDao.updateDailySold(seatType.id, sold.toString())
            }
        }
        return orderId
    }

    suspend fun getOrdersByUser(phone: String): List<TrainOrder> =
        orderDao.getOrdersByUser(phone)

    suspend fun getOrderWithItems(orderId: Long): OrderDao.OrderWithItems? =
        orderDao.getOrderWithItems(orderId)

    suspend fun getOrderById(id: Long): TrainOrder? = orderDao.getOrderById(id)

    suspend fun getSeatTypes(trainId: Long): List<SeatType> = seatTypeDao.getByTrainId(trainId)

    suspend fun getSeatTypeById(id: Long): SeatType? = seatTypeDao.getById(id)

    suspend fun getAvailableSeats(seatType: SeatType, date: String): Int {
        val sold = JSONObject(seatType.dailySold)
        val soldCount = sold.optInt(date, 0)
        return seatType.totalCount - soldCount
    }

    suspend fun updateOrderStatus(orderId: Long, status: String) =
        orderDao.updateStatus(orderId, status)

    suspend fun getItemsByOrderId(orderId: Long): List<OrderItem> =
        orderDao.getItemsByOrderId(orderId)
}
