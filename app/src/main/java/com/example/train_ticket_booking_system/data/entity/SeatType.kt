package com.example.train_ticket_booking_system.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "seat_type",
    foreignKeys = [
        ForeignKey(
            entity = Train::class,
            parentColumns = ["id"],
            childColumns = ["trainId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trainId")]
)
data class SeatType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trainId: Long,
    val typeName: String,
    val price: Double,
    val totalCount: Int,
    val dailySold: String = "{}"
)
