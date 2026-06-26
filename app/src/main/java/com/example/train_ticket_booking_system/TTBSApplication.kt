package com.example.train_ticket_booking_system

import android.app.Application
import android.util.Log
import com.example.train_ticket_booking_system.data.AppDatabase
import com.example.train_ticket_booking_system.data.seed.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TTBSApplication : Application() {
    lateinit var database: AppDatabase
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        seedIfNeeded()
    }

    private fun seedIfNeeded() {
        appScope.launch {
            try {
                val stationCount = database.stationDao().count()
                if (stationCount == 0) {
                    SeedData.seed(database)
                }
            } catch (e: Exception) {
                Log.e("TTBS", "Seed failed", e)
            }
        }
    }
}
