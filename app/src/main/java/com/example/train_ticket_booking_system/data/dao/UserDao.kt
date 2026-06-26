package com.example.train_ticket_booking_system.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.train_ticket_booking_system.data.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM user WHERE phone = :phone")
    suspend fun getByPhone(phone: String): User?

    @Query("UPDATE user SET paymentPassword = :password WHERE phone = :phone")
    suspend fun updatePassword(phone: String, password: String)
}
