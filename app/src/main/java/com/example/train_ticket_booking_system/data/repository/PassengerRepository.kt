package com.example.train_ticket_booking_system.data.repository

import com.example.train_ticket_booking_system.data.dao.PassengerDao
import com.example.train_ticket_booking_system.data.entity.Passenger

class PassengerRepository(private val dao: PassengerDao) {
    suspend fun getByUser(phone: String): List<Passenger> = dao.getByUserPhone(phone)
    suspend fun getById(id: Long): Passenger? = dao.getById(id)
    suspend fun add(passenger: Passenger): Long = dao.insert(passenger)
    suspend fun update(passenger: Passenger) = dao.update(passenger)
    suspend fun delete(passenger: Passenger) = dao.delete(passenger)
}
