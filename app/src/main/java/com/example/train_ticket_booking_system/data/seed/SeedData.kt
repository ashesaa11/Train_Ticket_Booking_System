package com.example.train_ticket_booking_system.data.seed

import com.example.train_ticket_booking_system.data.AppDatabase
import com.example.train_ticket_booking_system.data.entity.SeatType
import com.example.train_ticket_booking_system.data.entity.Station
import com.example.train_ticket_booking_system.data.entity.Train
import com.example.train_ticket_booking_system.data.entity.TrainStop

object SeedData {
    suspend fun seed(db: AppDatabase) {
        val stations = listOf(
            Station(name = "北京南", city = "北京", code = "BJP"),
            Station(name = "北京西", city = "北京", code = "BXP"),
            Station(name = "上海虹桥", city = "上海", code = "SHH"),
            Station(name = "上海", city = "上海", code = "SH"),
            Station(name = "广州南", city = "广州", code = "GZN"),
            Station(name = "深圳北", city = "深圳", code = "SZB"),
            Station(name = "武汉", city = "武汉", code = "WH"),
            Station(name = "成都东", city = "成都", code = "CDD"),
            Station(name = "南京南", city = "南京", code = "NJN"),
            Station(name = "杭州东", city = "杭州", code = "HZD"),
            Station(name = "西安北", city = "西安", code = "XAB"),
            Station(name = "郑州东", city = "郑州", code = "ZZD"),
            Station(name = "长沙南", city = "长沙", code = "CSN"),
            Station(name = "天津", city = "天津", code = "TJ"),
            Station(name = "济南西", city = "济南", code = "JNX"),
            Station(name = "沈阳北", city = "沈阳", code = "SYB"),
            Station(name = "哈尔滨西", city = "哈尔滨", code = "HEB"),
            Station(name = "福州南", city = "福州", code = "FZN"),
            Station(name = "重庆北", city = "重庆", code = "CQB"),
            Station(name = "昆明南", city = "昆明", code = "KMN")
        )
        db.stationDao().insertAll(stations)

        // G1: 北京南 → 上海虹桥, 4h28m
        val g1 = Train(number = "G1", type = "G", departureStationId = 1, arrivalStationId = 3, durationMinutes = 268)
        val g2 = Train(number = "G2", type = "G", departureStationId = 3, arrivalStationId = 1, durationMinutes = 268)
        // G79: 北京西 → 广州南, 8h
        val g79 = Train(number = "G79", type = "G", departureStationId = 2, arrivalStationId = 5, durationMinutes = 480)
        // G80: 广州南 → 北京西, 8h
        val g80 = Train(number = "G80", type = "G", departureStationId = 5, arrivalStationId = 2, durationMinutes = 480)
        // G100: 上海虹桥 → 广州南, 7h
        val g100 = Train(number = "G100", type = "G", departureStationId = 3, arrivalStationId = 5, durationMinutes = 420)
        // D301: 北京南 → 南京南, 3h30m
        val d301 = Train(number = "D301", type = "D", departureStationId = 1, arrivalStationId = 9, durationMinutes = 210)
        // D302: 上海 → 杭州东, 1h
        val d302 = Train(number = "D302", type = "D", departureStationId = 4, arrivalStationId = 10, durationMinutes = 60)
        // G350: 武汉 → 广州南, 4h
        val g350 = Train(number = "G350", type = "G", departureStationId = 7, arrivalStationId = 5, durationMinutes = 240)
        // G400: 成都东 → 西安北, 3h
        val g400 = Train(number = "G400", type = "G", departureStationId = 8, arrivalStationId = 11, durationMinutes = 180)
        // D501: 郑州东 → 武汉, 2h
        val d501 = Train(number = "D501", type = "D", departureStationId = 12, arrivalStationId = 7, durationMinutes = 120)
        // K180: 北京西 → 郑州, 6h
        val k180 = Train(number = "K180", type = "K", departureStationId = 2, arrivalStationId = 12, durationMinutes = 360)
        // G600: 深圳北 → 长沙南, 3h
        val g600 = Train(number = "G600", type = "G", departureStationId = 6, arrivalStationId = 13, durationMinutes = 180)
        // D701: 南京南 → 武汉, 2h30m
        val d701 = Train(number = "D701", type = "D", departureStationId = 9, arrivalStationId = 7, durationMinutes = 150)
        // G700: 西安北 → 郑州东, 2h
        val g700 = Train(number = "G700", type = "G", departureStationId = 11, arrivalStationId = 12, durationMinutes = 120)
        // K280: 上海 → 杭州东, 2h30m
        val k280 = Train(number = "K280", type = "K", departureStationId = 4, arrivalStationId = 10, durationMinutes = 150)

        val trains = listOf(g1, g2, g79, g80, g100, d301, d302, g350, g400, d501, k180, g600, d701, g700, k280)
        db.trainDao().insertAll(trains)

        // TrainStop data for G1: 北京南 → 上海虹桥 (only departure/arrival for simplicity)
        val g1Stops = listOf(
            TrainStop(trainId = 1, stationId = 1, stopOrder = 1, arrivalTime = "--", departureTime = "07:00", dayOffset = 0),
            TrainStop(trainId = 1, stationId = 9, stopOrder = 2, arrivalTime = "10:00", departureTime = "10:02", dayOffset = 0),
            TrainStop(trainId = 1, stationId = 3, stopOrder = 3, arrivalTime = "11:28", departureTime = "--", dayOffset = 0)
        )
        val g2Stops = listOf(
            TrainStop(trainId = 2, stationId = 3, stopOrder = 1, arrivalTime = "--", departureTime = "09:00", dayOffset = 0),
            TrainStop(trainId = 2, stationId = 9, stopOrder = 2, arrivalTime = "12:00", departureTime = "12:02", dayOffset = 0),
            TrainStop(trainId = 2, stationId = 1, stopOrder = 3, arrivalTime = "13:28", departureTime = "--", dayOffset = 0)
        )
        val g79Stops = listOf(
            TrainStop(trainId = 3, stationId = 2, stopOrder = 1, arrivalTime = "--", departureTime = "10:00", dayOffset = 0),
            TrainStop(trainId = 3, stationId = 5, stopOrder = 2, arrivalTime = "18:00", departureTime = "--", dayOffset = 0)
        )
        val g80Stops = listOf(
            TrainStop(trainId = 4, stationId = 5, stopOrder = 1, arrivalTime = "--", departureTime = "12:00", dayOffset = 0),
            TrainStop(trainId = 4, stationId = 2, stopOrder = 2, arrivalTime = "20:00", departureTime = "--", dayOffset = 0)
        )
        val g100Stops = listOf(
            TrainStop(trainId = 5, stationId = 3, stopOrder = 1, arrivalTime = "--", departureTime = "08:00", dayOffset = 0),
            TrainStop(trainId = 5, stationId = 5, stopOrder = 2, arrivalTime = "15:00", departureTime = "--", dayOffset = 0)
        )
        val d301Stops = listOf(
            TrainStop(trainId = 6, stationId = 1, stopOrder = 1, arrivalTime = "--", departureTime = "14:00", dayOffset = 0),
            TrainStop(trainId = 6, stationId = 9, stopOrder = 2, arrivalTime = "17:30", departureTime = "--", dayOffset = 0)
        )
        val d302Stops = listOf(
            TrainStop(trainId = 7, stationId = 4, stopOrder = 1, arrivalTime = "--", departureTime = "16:00", dayOffset = 0),
            TrainStop(trainId = 7, stationId = 10, stopOrder = 2, arrivalTime = "17:00", departureTime = "--", dayOffset = 0)
        )
        val g350Stops = listOf(
            TrainStop(trainId = 8, stationId = 7, stopOrder = 1, arrivalTime = "--", departureTime = "10:00", dayOffset = 0),
            TrainStop(trainId = 8, stationId = 5, stopOrder = 2, arrivalTime = "14:00", departureTime = "--", dayOffset = 0)
        )
        val g400Stops = listOf(
            TrainStop(trainId = 9, stationId = 8, stopOrder = 1, arrivalTime = "--", departureTime = "08:00", dayOffset = 0),
            TrainStop(trainId = 9, stationId = 11, stopOrder = 2, arrivalTime = "11:00", departureTime = "--", dayOffset = 0)
        )
        val d501Stops = listOf(
            TrainStop(trainId = 10, stationId = 12, stopOrder = 1, arrivalTime = "--", departureTime = "13:00", dayOffset = 0),
            TrainStop(trainId = 10, stationId = 7, stopOrder = 2, arrivalTime = "15:00", departureTime = "--", dayOffset = 0)
        )
        val k180Stops = listOf(
            TrainStop(trainId = 11, stationId = 2, stopOrder = 1, arrivalTime = "--", departureTime = "22:00", dayOffset = 0),
            TrainStop(trainId = 11, stationId = 12, stopOrder = 2, arrivalTime = "04:00", departureTime = "--", dayOffset = 1)
        )
        val g600Stops = listOf(
            TrainStop(trainId = 12, stationId = 6, stopOrder = 1, arrivalTime = "--", departureTime = "14:00", dayOffset = 0),
            TrainStop(trainId = 12, stationId = 13, stopOrder = 2, arrivalTime = "17:00", departureTime = "--", dayOffset = 0)
        )
        val d701Stops = listOf(
            TrainStop(trainId = 13, stationId = 9, stopOrder = 1, arrivalTime = "--", departureTime = "09:00", dayOffset = 0),
            TrainStop(trainId = 13, stationId = 7, stopOrder = 2, arrivalTime = "11:30", departureTime = "--", dayOffset = 0)
        )
        val g700Stops = listOf(
            TrainStop(trainId = 14, stationId = 11, stopOrder = 1, arrivalTime = "--", departureTime = "07:00", dayOffset = 0),
            TrainStop(trainId = 14, stationId = 12, stopOrder = 2, arrivalTime = "09:00", departureTime = "--", dayOffset = 0)
        )
        val k280Stops = listOf(
            TrainStop(trainId = 15, stationId = 4, stopOrder = 1, arrivalTime = "--", departureTime = "08:00", dayOffset = 0),
            TrainStop(trainId = 15, stationId = 10, stopOrder = 2, arrivalTime = "10:30", departureTime = "--", dayOffset = 0)
        )

        db.trainDao().insertStops(g1Stops + g2Stops + g79Stops + g80Stops + g100Stops +
            d301Stops + d302Stops + g350Stops + g400Stops + d501Stops +
            k180Stops + g600Stops + d701Stops + g700Stops + k280Stops)

        // Seat types for each train
        val allSeatTypes = mutableListOf<SeatType>()

        // G trains (high-speed): 二等座/一等座/商务座
        val gTrains = listOf(1L, 2L, 3L, 4L, 5L, 8L, 9L, 12L, 14L)
        for (trainId in gTrains) {
            allSeatTypes.add(SeatType(trainId = trainId, typeName = "二等座", price = 553.0, totalCount = 600))
            allSeatTypes.add(SeatType(trainId = trainId, typeName = "一等座", price = 933.0, totalCount = 100))
            allSeatTypes.add(SeatType(trainId = trainId, typeName = "商务座", price = 1750.0, totalCount = 28))
        }

        // D trains: 二等座/一等座
        val dTrains = listOf(6L, 7L, 10L, 13L)
        for (trainId in dTrains) {
            allSeatTypes.add(SeatType(trainId = trainId, typeName = "二等座", price = 320.0, totalCount = 500))
            allSeatTypes.add(SeatType(trainId = trainId, typeName = "一等座", price = 512.0, totalCount = 80))
        }

        // K trains: 硬座/硬卧上/硬卧中/硬卧下/软卧
        val kTrains = listOf(11L, 15L)
        for (trainId in kTrains) {
            allSeatTypes.add(SeatType(trainId = trainId, typeName = "硬座", price = 128.0, totalCount = 500))
            allSeatTypes.add(SeatType(trainId = trainId, typeName = "硬卧上", price = 230.0, totalCount = 200))
            allSeatTypes.add(SeatType(trainId = trainId, typeName = "硬卧中", price = 240.0, totalCount = 200))
            allSeatTypes.add(SeatType(trainId = trainId, typeName = "硬卧下", price = 250.0, totalCount = 200))
            allSeatTypes.add(SeatType(trainId = trainId, typeName = "软卧", price = 380.0, totalCount = 80))
        }

        db.seatTypeDao().insertAll(allSeatTypes)
    }
}
