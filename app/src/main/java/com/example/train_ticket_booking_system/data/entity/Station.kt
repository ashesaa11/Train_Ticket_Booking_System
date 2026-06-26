package com.example.train_ticket_booking_system.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "station")
data class Station(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val city: String,
    val code: String
)
