package com.example.train_ticket_booking_system.data.repository

import android.util.Log
import com.example.train_ticket_booking_system.data.dao.UserDao
import com.example.train_ticket_booking_system.data.entity.User
import com.example.train_ticket_booking_system.util.PasswordHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val dao: UserDao) {
    suspend fun getByPhone(phone: String): User? = dao.getByPhone(phone)

    suspend fun register(phone: String, password: String): User? {
        if (dao.getByPhone(phone) != null) return null
        val hashedPassword = withContext(Dispatchers.Default) {
            PasswordHelper.hash(password)
        }
        Log.d("TTBS_AUTH", "register: storing hashed password for phone=$phone")
        val user = User(phone = phone, password = hashedPassword, nickname = "用户${phone.takeLast(4)}")
        dao.insert(user)
        return user
    }

    suspend fun login(phone: String, password: String): User? {
        val user = dao.getByPhone(phone) ?: return null
        val match = withContext(Dispatchers.Default) {
            PasswordHelper.verify(password, user.password)
        }
        Log.d("TTBS_AUTH", "login: phone=$phone, verifyResult=$match")
        return if (match) user else null
    }

    suspend fun setPaymentPassword(phone: String, password: String) {
        val hashed = withContext(Dispatchers.Default) {
            PasswordHelper.hash(password)
        }
        Log.d("TTBS_AUTH", "setPaymentPassword: storing hashed payment password for phone=$phone")
        dao.updatePaymentPassword(phone, hashed)
    }

    suspend fun verifyPaymentPassword(phone: String, password: String): Boolean {
        val user = dao.getByPhone(phone) ?: return false
        if (user.paymentPassword.isEmpty()) return false
        return withContext(Dispatchers.Default) {
            PasswordHelper.verify(password, user.paymentPassword)
        }
    }

    suspend fun getNickname(phone: String): String {
        return dao.getByPhone(phone)?.nickname ?: "用户"
    }

    suspend fun updateNickname(phone: String, nickname: String) {
        dao.updateNickname(phone, nickname)
    }
}
