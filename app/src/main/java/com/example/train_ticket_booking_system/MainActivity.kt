package com.example.train_ticket_booking_system

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import com.example.train_ticket_booking_system.data.repository.PassengerRepository
import com.example.train_ticket_booking_system.data.repository.StationRepository
import com.example.train_ticket_booking_system.data.repository.TrainRepository
import com.example.train_ticket_booking_system.data.repository.UserRepository
import com.example.train_ticket_booking_system.ui.navigation.AppNavigation
import com.example.train_ticket_booking_system.ui.navigation.Repos
import com.example.train_ticket_booking_system.ui.theme.Train_Ticket_Booking_SystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as TTBSApplication
        val db = app.database
        val repos = Repos(
            userRepo = UserRepository(db.userDao()),
            stationRepo = StationRepository(db.stationDao()),
            trainRepo = TrainRepository(db.trainDao()),
            passengerRepo = PassengerRepository(db.passengerDao()),
            orderRepo = OrderRepository(db.orderDao(), db.seatTypeDao())
        )
        setContent {
            Train_Ticket_Booking_SystemTheme {
                AppNavigation(repos = repos)
            }
        }
    }
}
