package com.example.train_ticket_booking_system.data.repository

import com.example.train_ticket_booking_system.data.dao.UserDao
import com.example.train_ticket_booking_system.data.entity.User

class UserRepository(private val dao: UserDao) {
    suspend fun getByPhone(phone: String): User? = dao.getByPhone(phone)

    suspend fun register(phone: String, password: String): User? {
        if (dao.getByPhone(phone) != null) return null
        val user = User(phone = phone, password = password, nickname = "用户${phone.takeLast(4)}")
        dao.insert(user)
        return user
    }

    suspend fun login(phone: String, password: String): User? =
        dao.login(phone, password)

    suspend fun setPaymentPassword(phone: String, password: String) {
        dao.updatePaymentPassword(phone, password)
    }

    suspend fun verifyPaymentPassword(phone: String, password: String): Boolean {
        val user = dao.getByPhone(phone) ?: return false
        return user.paymentPassword == password
    }

    suspend fun getNickname(phone: String): String {
        return dao.getByPhone(phone)?.nickname ?: "用户"
    }

    suspend fun updateNickname(phone: String, nickname: String) {
        dao.updateNickname(phone, nickname)
    }
}
