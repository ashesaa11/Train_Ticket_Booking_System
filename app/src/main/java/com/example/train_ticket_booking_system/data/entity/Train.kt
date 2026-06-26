package com.example.train_ticket_booking_system.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "train",
    foreignKeys = [
        ForeignKey(
            entity = Station::class,
            parentColumns = ["id"],
            childColumns = ["departureStationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Station::class,
            parentColumns = ["id"],
            childColumns = ["arrivalStationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("departureStationId"),
        Index("arrivalStationId")
    ]
)
data class Train(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val type: String,
    val departureStationId: Long,
    val arrivalStationId: Long,
    val durationMinutes: Int
)
