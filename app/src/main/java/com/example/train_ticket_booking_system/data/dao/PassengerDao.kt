package com.example.train_ticket_booking_system.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.train_ticket_booking_system.data.entity.Passenger

@Dao
interface PassengerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(passenger: Passenger): Long

    @Update
    suspend fun update(passenger: Passenger)

    @Delete
    suspend fun delete(passenger: Passenger)

    @Query("SELECT * FROM passenger WHERE userPhone = :userPhone ORDER BY id")
    suspend fun getByUserPhone(userPhone: String): List<Passenger>

    @Query("SELECT * FROM passenger WHERE id = :id")
    suspend fun getById(id: Long): Passenger?
}
