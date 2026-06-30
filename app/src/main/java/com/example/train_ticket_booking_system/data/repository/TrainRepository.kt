package com.example.train_ticket_booking_system.data.repository

import android.util.Log
import com.example.train_ticket_booking_system.data.dao.TrainDao
import com.example.train_ticket_booking_system.data.entity.Train
import com.example.train_ticket_booking_system.data.entity.TrainStop

data class TrainWithStops(
    val train: Train,
    val stops: List<TrainStop>
)

class TrainRepository(private val dao: TrainDao) {
    suspend fun search(fromStationId: Long, toStationId: Long, date: String): List<TrainWithStops> {
        val allTrains = dao.searchTrains(fromStationId, toStationId)
        val targetCount = 8
        val result: List<Train>
        if (allTrains.size <= targetCount) {
            result = allTrains
        } else {
            val dateSeed = Math.abs(date.hashCode().toLong())
            val startIndex = (dateSeed % allTrains.size).toInt()
            val selected = mutableListOf<Train>()
            for (i in 0 until targetCount) {
                selected.add(allTrains[(startIndex + i) % allTrains.size])
            }
            result = selected
        }
        Log.d("TTBS_TRAIN", "search: from=$fromStationId to=$toStationId date=$date, all=${allTrains.size}, result=${result.size}")
        return result.map { TrainWithStops(it, dao.getStops(it.id)) }
    }

    suspend fun getById(id: Long): Train? = dao.getById(id)
    suspend fun getStops(trainId: Long): List<TrainStop> = dao.getStops(trainId)
    suspend fun getAll(): List<Train> = dao.getAll()
    suspend fun insert(train: Train) { dao.insertAll(listOf(train)) }
    suspend fun insertStop(stop: TrainStop) { dao.insertStops(listOf(stop)) }
    suspend fun getReachableStationIds(fromStationId: Long): List<Long> = dao.getReachableStationIds(fromStationId)
}
