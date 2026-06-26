package com.example.train_ticket_booking_system.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.train_ticket_booking_system.data.entity.Station

@Dao
interface StationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stations: List<Station>)

    @Query("SELECT * FROM station ORDER BY city")
    suspend fun getAll(): List<Station>

    @Query("SELECT * FROM station WHERE name LIKE '%' || :query || '%' OR city LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<Station>

    @Query("SELECT * FROM station WHERE id = :id")
    suspend fun getById(id: Long): Station?

    @Query("SELECT COUNT(*) FROM station")
    suspend fun count(): Int
}
