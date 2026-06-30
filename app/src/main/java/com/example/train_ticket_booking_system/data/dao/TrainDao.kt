package com.example.train_ticket_booking_system.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.train_ticket_booking_system.data.entity.Train
import com.example.train_ticket_booking_system.data.entity.TrainStop

@Dao
interface TrainDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trains: List<Train>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<TrainStop>)

    @Query("""
        SELECT DISTINCT t.* FROM train t
        INNER JOIN train_stop ts1 ON t.id = ts1.trainId
        INNER JOIN train_stop ts2 ON t.id = ts2.trainId
        WHERE ts1.stationId = :fromStationId
        AND ts2.stationId = :toStationId
        AND ts1.stopOrder < ts2.stopOrder
    """)
    suspend fun searchTrains(fromStationId: Long, toStationId: Long): List<Train>

    @Query("SELECT * FROM train WHERE id = :id")
    suspend fun getById(id: Long): Train?

    @Query("SELECT * FROM train ORDER BY number")
    suspend fun getAll(): List<Train>

    @Query("SELECT * FROM train_stop WHERE trainId = :trainId ORDER BY stopOrder")
    suspend fun getStops(trainId: Long): List<TrainStop>

    @Query("""
        SELECT DISTINCT ts2.stationId FROM train_stop ts1
        INNER JOIN train_stop ts2 ON ts1.trainId = ts2.trainId
        WHERE ts1.stationId = :fromStationId
        AND ts2.stopOrder > ts1.stopOrder
    """)
    suspend fun getReachableStationIds(fromStationId: Long): List<Long>
}
