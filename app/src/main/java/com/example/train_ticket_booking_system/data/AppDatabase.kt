package com.example.train_ticket_booking_system.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.train_ticket_booking_system.data.dao.OrderDao
import com.example.train_ticket_booking_system.data.dao.PassengerDao
import com.example.train_ticket_booking_system.data.dao.SeatTypeDao
import com.example.train_ticket_booking_system.data.dao.StationDao
import com.example.train_ticket_booking_system.data.dao.TrainDao
import com.example.train_ticket_booking_system.data.dao.UserDao
import com.example.train_ticket_booking_system.data.entity.OrderItem
import com.example.train_ticket_booking_system.data.entity.Passenger
import com.example.train_ticket_booking_system.data.entity.SeatType
import com.example.train_ticket_booking_system.data.entity.Station
import com.example.train_ticket_booking_system.data.entity.Train
import com.example.train_ticket_booking_system.data.entity.TrainOrder
import com.example.train_ticket_booking_system.data.entity.TrainStop
import com.example.train_ticket_booking_system.data.entity.User

@Database(
    entities = [
        User::class,
        Station::class,
        Train::class,
        TrainStop::class,
        SeatType::class,
        Passenger::class,
        TrainOrder::class,
        OrderItem::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun stationDao(): StationDao
    abstract fun trainDao(): TrainDao
    abstract fun seatTypeDao(): SeatTypeDao
    abstract fun passengerDao(): PassengerDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "train_ticket_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
