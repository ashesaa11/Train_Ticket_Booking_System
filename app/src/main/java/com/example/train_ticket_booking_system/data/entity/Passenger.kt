package com.example.train_ticket_booking_system.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "passenger",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["phone"],
            childColumns = ["userPhone"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userPhone")]
)
data class Passenger(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userPhone: String,
    val name: String,
    val idCard: String,
    val passengerType: String
)
