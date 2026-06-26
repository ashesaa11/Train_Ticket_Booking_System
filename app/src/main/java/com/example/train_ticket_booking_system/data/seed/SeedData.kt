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
            Station(name = "北京", city = "北京", code = "BJ"),
            Station(name = "上海虹桥", city = "上海", code = "SHH"),
            Station(name = "上海", city = "上海", code = "SH"),
            Station(name = "广州南", city = "广州", code = "GZN"),
            Station(name = "深圳北", city = "深圳", code = "SZB"),
            Station(name = "武汉", city = "武汉", code = "WH"),
            Station(name = "汉口", city = "武汉", code = "HK"),
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
            Station(name = "昆明南", city = "昆明", code = "KMN"),
            Station(name = "厦门北", city = "厦门", code = "XMB"),
            Station(name = "大连北", city = "大连", code = "DLB"),
            Station(name = "青岛北", city = "青岛", code = "QDB"),
            Station(name = "贵阳北", city = "贵阳", code = "GYB"),
            Station(name = "南昌西", city = "南昌", code = "NCX"),
            Station(name = "合肥南", city = "合肥", code = "HFN")
        )
        db.stationDao().insertAll(stations)

        // Define trains: number, type, depStationId, arrStationId, durationMinutes
        data class T(val number: String, val type: String, val dep: Long, val arr: Long, val dur: Int)
        val trainDefs = listOf(
            // 北京始发
            T("G1", "G", 1, 4, 268), T("G2", "G", 4, 1, 268),
            T("G3", "G", 1, 4, 280), T("G4", "G", 4, 1, 280),
            T("G79", "G", 2, 6, 480), T("G80", "G", 6, 2, 480),
            T("G101", "G", 1, 11, 210), T("G102", "G", 11, 1, 210),
            T("D301", "D", 1, 11, 220), T("D302", "D", 11, 1, 220),
            T("G201", "G", 1, 16, 30), T("G202", "G", 16, 1, 30),
            T("G301", "G", 1, 17, 110), T("G302", "G", 17, 1, 110),
            T("K180", "K", 2, 14, 380),
            T("G401", "G", 1, 18, 250), T("G402", "G", 18, 1, 250),
            T("D501", "D", 2, 14, 200),

            // 上海始发
            T("G100", "G", 4, 6, 420), T("G1010", "G", 6, 4, 420),
            T("G10", "G", 4, 11, 180), T("G11", "G", 11, 4, 180),
            T("G20", "G", 4, 12, 60), T("G21", "G", 12, 4, 60),
            T("K280", "K", 5, 12, 150),
            T("D201", "D", 4, 17, 240), T("D202", "D", 17, 4, 240),

            // 广深始发
            T("G600", "G", 7, 15, 180), T("G601", "G", 15, 7, 180),
            T("G610", "G", 6, 7, 30), T("G611", "G", 7, 6, 30),
            T("G620", "G", 7, 26, 300), T("G621", "G", 26, 7, 300),
            T("G630", "G", 6, 24, 210), T("G631", "G", 24, 6, 210),
            T("D701", "D", 7, 23, 200), T("D702", "D", 23, 7, 200),

            // 武汉始发
            T("G350", "G", 8, 6, 240), T("G351", "G", 6, 8, 240),
            T("G360", "G", 8, 10, 500), T("G361", "G", 10, 8, 500),
            T("D801", "D", 8, 15, 120), T("D802", "D", 15, 8, 120),
            T("G370", "G", 8, 22, 400), T("G371", "G", 22, 8, 400),

            // 蓉渝始发
            T("G400", "G", 10, 13, 180), T("G401", "G", 13, 10, 180),
            T("G410", "G", 10, 22, 100), T("G411", "G", 22, 10, 100),
            T("G420", "G", 21, 10, 400), T("G421", "G", 10, 21, 400),
            T("D901", "D", 21, 26, 200), T("D902", "D", 26, 21, 200),

            // 其他
            T("G700", "G", 13, 14, 120), T("G701", "G", 14, 13, 120),
            T("G710", "G", 13, 9, 240), T("G711", "G", 9, 13, 240),
            T("D150", "D", 25, 17, 160), T("D151", "D", 17, 25, 160),
            T("G800", "G", 27, 15, 110), T("G801", "G", 15, 27, 110),
            T("G900", "G", 28, 11, 80), T("G901", "G", 11, 28, 80),
            T("D250", "D", 16, 25, 80), T("D251", "D", 25, 16, 80),
            T("K500", "K", 19, 17, 480), T("K501", "K", 17, 19, 480),
            T("G500", "G", 5, 28, 180), T("G501", "G", 28, 5, 180),
            T("G510", "G", 5, 11, 220), T("G511", "G", 11, 5, 220),
        )

        val trains = trainDefs.map { Train(number = it.number, type = it.type, departureStationId = it.dep, arrivalStationId = it.arr, durationMinutes = it.dur) }
        db.trainDao().insertAll(trains)

        // Generate stops
        val allStops = mutableListOf<TrainStop>()
        trainDefs.forEachIndexed { index, t ->
            val trainId = index + 1L
            val depTime = departureTime(index)
            val depHM = depTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
            val arrTime = arrivalTime(depHM, t.dur)
            allStops.add(TrainStop(trainId = trainId, stationId = t.dep, stopOrder = 1, arrivalTime = "--", departureTime = depTime, dayOffset = 0))
            allStops.add(TrainStop(trainId = trainId, stationId = t.arr, stopOrder = 2, arrivalTime = arrTime, departureTime = "--", dayOffset = if(depHM + t.dur >= 1440) 1 else 0))
        }
        db.trainDao().insertStops(allStops)

        // Seat types
        val allSeatTypes = mutableListOf<SeatType>()
        for (i in 1..trainDefs.size.toLong()) {
            val def = trainDefs[i.toInt() - 1]
            when (def.type) {
                "G" -> {
                    allSeatTypes.add(SeatType(trainId = i, typeName = "二等座", price = 300 + (def.dur * 0.8).toInt().toDouble(), totalCount = 600))
                    allSeatTypes.add(SeatType(trainId = i, typeName = "一等座", price = 500 + (def.dur * 1.2).toInt().toDouble(), totalCount = 100))
                    allSeatTypes.add(SeatType(trainId = i, typeName = "商务座", price = 900 + (def.dur * 2.0).toInt().toDouble(), totalCount = 28))
                }
                "D" -> {
                    allSeatTypes.add(SeatType(trainId = i, typeName = "二等座", price = 150 + (def.dur * 0.5).toInt().toDouble(), totalCount = 500))
                    allSeatTypes.add(SeatType(trainId = i, typeName = "一等座", price = 250 + (def.dur * 0.8).toInt().toDouble(), totalCount = 80))
                }
                "K" -> {
                    allSeatTypes.add(SeatType(trainId = i, typeName = "硬座", price = 50 + (def.dur * 0.2).toInt().toDouble(), totalCount = 500))
                    allSeatTypes.add(SeatType(trainId = i, typeName = "硬卧上", price = 100 + (def.dur * 0.3).toInt().toDouble(), totalCount = 200))
                    allSeatTypes.add(SeatType(trainId = i, typeName = "硬卧中", price = 110 + (def.dur * 0.3).toInt().toDouble(), totalCount = 200))
                    allSeatTypes.add(SeatType(trainId = i, typeName = "硬卧下", price = 120 + (def.dur * 0.3).toInt().toDouble(), totalCount = 200))
                    allSeatTypes.add(SeatType(trainId = i, typeName = "软卧", price = 200 + (def.dur * 0.5).toInt().toDouble(), totalCount = 80))
                }
            }
        }
        db.seatTypeDao().insertAll(allSeatTypes)
    }

    private fun pad2(n: Int) = n.toString().padStart(2, '0')
    private fun departureTime(index: Int): String { val h = 6 + (index * 2 % 16); val m = (index * 17) % 60; return "${pad2(h)}:${pad2(m)}" }
    private fun arrivalTime(depMin: Int, dur: Int): String { val total = depMin + dur; val h = total / 60 % 24; val m = total % 60; return "${pad2(h)}:${pad2(m)}" }
}
