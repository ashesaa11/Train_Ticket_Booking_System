package com.example.train_ticket_booking_system.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "train_order")
data class TrainOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userPhone: String,
    val trainId: Long,
    val trainNumber: String,
    val departureStationName: String,
    val arrivalStationName: String,
    val departureDate: String,
    val departureTime: String,
    val arrivalTime: String,
    val status: String,
    val totalPrice: Double,
    val originalOrderId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
