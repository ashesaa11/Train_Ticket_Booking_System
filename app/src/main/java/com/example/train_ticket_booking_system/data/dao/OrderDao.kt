package com.example.train_ticket_booking_system.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.example.train_ticket_booking_system.data.entity.OrderItem
import com.example.train_ticket_booking_system.data.entity.TrainOrder

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: TrainOrder): Long

    @Insert
    suspend fun insertItems(items: List<OrderItem>)

    @Transaction
    @Query("SELECT * FROM train_order WHERE id = :id")
    suspend fun getOrderWithItems(id: Long): OrderWithItems?

    @Query("SELECT * FROM train_order WHERE userPhone = :userPhone ORDER BY createdAt DESC")
    suspend fun getOrdersByUser(userPhone: String): List<TrainOrder>

    @Query("SELECT * FROM train_order WHERE userPhone = :userPhone AND status = :status ORDER BY createdAt DESC")
    suspend fun getOrdersByUserAndStatus(userPhone: String, status: String): List<TrainOrder>

    @Query("UPDATE train_order SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT * FROM train_order WHERE status = '未出行' AND departureDate < :today")
    suspend fun getExpiredOrders(today: String): List<TrainOrder>

    @Query("UPDATE train_order SET status = '已出行' WHERE id in (:ids)")
    suspend fun batchUpdateStatus(ids: List<Long>)

    @Query("SELECT * FROM train_order WHERE id = :id")
    suspend fun getOrderById(id: Long): TrainOrder?

    @Query("SELECT * FROM order_item WHERE orderId = :orderId")
    suspend fun getItemsByOrderId(orderId: Long): List<OrderItem>

    data class OrderWithItems(
        @Embedded val order: TrainOrder,
        @Relation(
            parentColumn = "id",
            entityColumn = "orderId"
        )
        val items: List<OrderItem>
    )
}
