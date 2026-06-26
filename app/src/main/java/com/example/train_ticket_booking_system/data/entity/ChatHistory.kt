package com.example.train_ticket_booking_system.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_history")
data class ChatHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userPhone: String,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
