package com.example.train_ticket_booking_system.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val phone: String,
    val password: String = "",
    val paymentPassword: String = "",
    val nickname: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
