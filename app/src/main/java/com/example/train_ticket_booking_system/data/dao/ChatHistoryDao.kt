package com.example.train_ticket_booking_system.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.train_ticket_booking_system.data.entity.ChatHistory

@Dao
interface ChatHistoryDao {
    @Insert
    suspend fun insert(history: ChatHistory)

    @Insert
    suspend fun insertAll(histories: List<ChatHistory>)

    @Query("SELECT * FROM chat_history WHERE userPhone = :userPhone ORDER BY timestamp ASC")
    suspend fun getByUser(userPhone: String): List<ChatHistory>

    @Query("DELETE FROM chat_history WHERE userPhone = :userPhone")
    suspend fun deleteByUser(userPhone: String)
}
