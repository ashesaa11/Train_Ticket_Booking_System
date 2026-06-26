package com.example.train_ticket_booking_system.data.repository

import com.example.train_ticket_booking_system.data.dao.UserDao
import com.example.train_ticket_booking_system.data.entity.User

class UserRepository(private val dao: UserDao) {
    suspend fun getByPhone(phone: String): User? = dao.getByPhone(phone)

    suspend fun register(phone: String): User {
        val user = User(phone = phone, paymentPassword = "")
        dao.insert(user)
        return user
    }

    suspend fun setPaymentPassword(phone: String, password: String) {
        dao.updatePassword(phone, password)
    }

    suspend fun verifyPassword(phone: String, password: String): Boolean {
        val user = dao.getByPhone(phone) ?: return false
        return user.paymentPassword == password
    }
}
