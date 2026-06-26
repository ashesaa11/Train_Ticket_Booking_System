package com.example.train_ticket_booking_system.data.repository

import com.example.train_ticket_booking_system.data.dao.StationDao
import com.example.train_ticket_booking_system.data.entity.Station

class StationRepository(private val dao: StationDao) {
    suspend fun getAll(): List<Station> = dao.getAll()
    suspend fun search(query: String): List<Station> = dao.search(query)
    suspend fun getById(id: Long): Station? = dao.getById(id)
    suspend fun insert(station: Station) { dao.insertAll(listOf(station)) }
}
