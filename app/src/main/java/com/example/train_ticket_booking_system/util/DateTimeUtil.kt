package com.example.train_ticket_booking_system.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object DateTimeUtil {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun parseDate(date: String): LocalDate = LocalDate.parse(date, dateFormatter)
    fun parseTime(time: String): LocalTime = LocalTime.parse(time, timeFormatter)
    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    fun getDepartureDateTime(departureDate: String, departureTime: String): LocalDateTime {
        val date = parseDate(departureDate)
        val time = parseTime(departureTime)
        return LocalDateTime.of(date, time)
    }

    fun hoursUntilDeparture(departureDate: String, departureTime: String): Long {
        val departure = getDepartureDateTime(departureDate, departureTime)
        val now = LocalDateTime.now()
        return Duration.between(now, departure).toHours()
    }

    fun todayStr(): String = LocalDate.now().format(dateFormatter)

    fun isBefore(date: String): Boolean = parseDate(date).isBefore(LocalDate.now())

    fun isToday(date: String): Boolean = parseDate(date) == LocalDate.now()
}
