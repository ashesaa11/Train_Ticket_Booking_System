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

    fun calcRescheduleFee(totalPrice: Double, hoursBeforeDeparture: Long): Double {
        return when {
            hoursBeforeDeparture > 48 -> 0.0
            hoursBeforeDeparture in 25..48 -> totalPrice * 0.05
            else -> totalPrice * 0.15
        }
    }

    fun calcRescheduleTotal(
        originalPrice: Double,
        newPrice: Double,
        hoursBeforeDeparture: Long
    ): RescheduleResult {
        val fee = calcRescheduleFee(originalPrice, hoursBeforeDeparture)
        val priceDiff = newPrice - originalPrice
        val amountToPay = if (priceDiff > 0) priceDiff + fee else fee
        val refund = if (priceDiff < 0) -priceDiff - fee else 0.0
        return RescheduleResult(
            fee = fee,
            priceDifference = priceDiff,
            amountToPay = maxOf(0.0, amountToPay),
            amountToRefund = maxOf(0.0, refund)
        )
    }

    data class RescheduleResult(
        val fee: Double,
        val priceDifference: Double,
        val amountToPay: Double,
        val amountToRefund: Double
    )
}
