package com.example.train_ticket_booking_system.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_item",
    foreignKeys = [
        ForeignKey(
            entity = TrainOrder::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orderId")]
)
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val passengerName: String,
    val passengerIdCard: String,
    val seatType: String,
    val price: Double
)
