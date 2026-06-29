package com.example.train_ticket_booking_system.util

object PriceCalculator {
    fun calcRefundFee(totalPrice: Double, hoursBeforeDeparture: Long): Double {
        return when {
            hoursBeforeDeparture > 48 -> 0.0
            hoursBeforeDeparture in 25..48 -> totalPrice * 0.05
            else -> totalPrice * 0.10
        }
    }

    fun calcRefundAmount(totalPrice: Double, hoursBeforeDeparture: Long): Double {
        return totalPrice - calcRefundFee(totalPrice, hoursBeforeDeparture)
    }

}
