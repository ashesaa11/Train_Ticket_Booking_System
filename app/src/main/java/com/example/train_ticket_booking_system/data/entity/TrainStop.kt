package com.example.train_ticket_booking_system.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "train_stop",
    foreignKeys = [
        ForeignKey(
            entity = Train::class,
            parentColumns = ["id"],
            childColumns = ["trainId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Station::class,
            parentColumns = ["id"],
            childColumns = ["stationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("trainId"),
        Index("stationId")
    ]
)
data class TrainStop(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trainId: Long,
    val stationId: Long,
    val stopOrder: Int,
    val arrivalTime: String,
    val departureTime: String,
    val dayOffset: Int = 0
)
