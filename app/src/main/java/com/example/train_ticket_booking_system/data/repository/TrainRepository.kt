package com.example.train_ticket_booking_system.data.repository

import com.example.train_ticket_booking_system.data.dao.TrainDao
import com.example.train_ticket_booking_system.data.entity.Train
import com.example.train_ticket_booking_system.data.entity.TrainStop

data class TrainWithStops(
    val train: Train,
    val stops: List<TrainStop>
)

class TrainRepository(private val dao: TrainDao) {
    suspend fun search(fromStationId: Long, toStationId: Long): List<TrainWithStops> {
        val trains = dao.searchTrains(fromStationId, toStationId)
        return trains.map { train ->
            TrainWithStops(train, dao.getStops(train.id))
        }
    }

    suspend fun getById(id: Long): Train? = dao.getById(id)
    suspend fun getStops(trainId: Long): List<TrainStop> = dao.getStops(trainId)
    suspend fun getAll(): List<Train> = dao.getAll()
    suspend fun insert(train: Train) { dao.insertAll(listOf(train)) }
    suspend fun insertStop(stop: TrainStop) { dao.insertStops(listOf(stop)) }
}
