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
        val filtered = allTrains.filter { train -> isTrainAvailableOnDate(train.id, date) }
        Log.d("TTBS_TRAIN", "search: from=$fromStationId to=$toStationId date=$date, all=${allTrains.size}, available=${filtered.size}")

        val result = if (filtered.size < 3 && allTrains.isNotEmpty()) {
            // 确保每天至少3趟车
            val forced = allTrains.take(minOf(3, allTrains.size))
            forced.map { TrainWithStops(it, dao.getStops(it.id)) }
        } else {
            filtered.map { TrainWithStops(it, dao.getStops(it.id)) }
        }
        Log.d("TTBS_TRAIN", "search result: ${result.size} trains")
        return result
    }

    private fun isTrainAvailableOnDate(trainId: Long, date: String): Boolean {
        val seed = "$trainId-$date"
        return Math.abs(seed.hashCode().toLong()) % 100 < 70
    }

    suspend fun getById(id: Long): Train? = dao.getById(id)
    suspend fun getStops(trainId: Long): List<TrainStop> = dao.getStops(trainId)
    suspend fun getAll(): List<Train> = dao.getAll()
    suspend fun insert(train: Train) { dao.insertAll(listOf(train)) }
    suspend fun insertStop(stop: TrainStop) { dao.insertStops(listOf(stop)) }
    suspend fun getReachableStationIds(fromStationId: Long): List<Long> = dao.getReachableStationIds(fromStationId)
}
