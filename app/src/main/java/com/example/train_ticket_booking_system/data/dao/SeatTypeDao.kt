package com.example.train_ticket_booking_system.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.train_ticket_booking_system.data.entity.SeatType

@Dao
interface SeatTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(seatTypes: List<SeatType>)

    @Query("SELECT * FROM seat_type WHERE trainId = :trainId")
    suspend fun getByTrainId(trainId: Long): List<SeatType>

    @Query("SELECT * FROM seat_type WHERE id = :id")
    suspend fun getById(id: Long): SeatType?

    @Query("UPDATE seat_type SET dailySold = :dailySold WHERE id = :id")
    suspend fun updateDailySold(id: Long, dailySold: String)
}
