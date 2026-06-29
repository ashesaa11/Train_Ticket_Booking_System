package com.example.train_ticket_booking_system.data.seed

import android.util.Log
import com.example.train_ticket_booking_system.data.AppDatabase
import com.example.train_ticket_booking_system.data.entity.SeatType
import com.example.train_ticket_booking_system.data.entity.Station
import com.example.train_ticket_booking_system.data.entity.Train
import com.example.train_ticket_booking_system.data.entity.TrainStop
import kotlin.random.Random

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

        // Major hub IDs
        val hubBeijing = setOf(1L, 2L, 3L)      // 北京南/西/北京
        val hubShanghai = setOf(4L, 5L)          // 上海虹桥/上海
        val hubGuangzhou = setOf(6L, 7L)         // 广州南/深圳北
        val hubWuhan = setOf(8L, 9L)             // 武汉/汉口
        val hubChengdu = setOf(10L, 21L)         // 成都东/重庆北
        val hubXian = setOf(13L)                 // 西安北
        val hubNanjing = setOf(11L, 28L)         // 南京南/合肥南
        val hubChangsha = setOf(15L, 27L)        // 长沙南/南昌西
        val hubDongbei = setOf(18L, 19L)         // 沈阳北/哈尔滨西
        val hubZhengzhou = setOf(14L)            // 郑州东

        // Route definition: pair of station IDs
        data class Route(val dep: Long, val arr: Long)

        val routes = mutableListOf<Route>()

        // Connect each hub pair if they have a route
        fun connect(a: Set<Long>, b: Set<Long>) {
            for (da in a) for (db in b) routes.add(Route(da, db))
        }

        // Beijing ↔ all hubs
        connect(hubBeijing, hubShanghai)
        connect(hubBeijing, hubGuangzhou)
        connect(hubBeijing, hubWuhan)
        connect(hubBeijing, hubChengdu)
        connect(hubBeijing, hubXian)
        connect(hubBeijing, hubNanjing)
        connect(hubBeijing, hubChangsha)
        connect(hubBeijing, hubDongbei)
        connect(hubBeijing, hubZhengzhou)
        connect(hubBeijing, setOf(16L))      // 天津
        connect(hubBeijing, setOf(17L))      // 济南西
        connect(hubBeijing, setOf(12L))      // 杭州东
        connect(hubBeijing, setOf(20L))      // 福州南
        connect(hubBeijing, setOf(22L))      // 昆明南
        connect(hubBeijing, setOf(23L))      // 厦门北
        connect(hubBeijing, setOf(25L))      // 青岛北
        connect(hubBeijing, setOf(26L))      // 贵阳北
        connect(hubBeijing, setOf(24L))      // 大连北

        // Shanghai ↔ all
        connect(hubShanghai, hubGuangzhou)
        connect(hubShanghai, hubWuhan)
        connect(hubShanghai, hubChengdu)
        connect(hubShanghai, hubXian)
        connect(hubShanghai, hubChangsha)
        connect(hubShanghai, hubDongbei)
        connect(hubShanghai, hubZhengzhou)
        connect(hubShanghai, setOf(12L))     // 杭州东
        connect(hubShanghai, setOf(17L))     // 济南西
        connect(hubShanghai, setOf(20L))     // 福州南
        connect(hubShanghai, setOf(22L))     // 昆明南
        connect(hubShanghai, setOf(23L))     // 厦门北
        connect(hubShanghai, setOf(25L))     // 青岛北
        connect(hubShanghai, setOf(26L))     // 贵阳北
        connect(hubShanghai, setOf(24L))     // 大连北
        connect(hubShanghai, setOf(16L))     // 天津

        // Guangzhou ↔ all
        connect(hubGuangzhou, hubWuhan)
        connect(hubGuangzhou, hubChengdu)
        connect(hubGuangzhou, hubXian)
        connect(hubGuangzhou, hubChangsha)
        connect(hubGuangzhou, hubDongbei)
        connect(hubGuangzhou, hubZhengzhou)
        connect(hubGuangzhou, setOf(12L))    // 杭州东
        connect(hubGuangzhou, setOf(20L))    // 福州南
        connect(hubGuangzhou, setOf(22L))    // 昆明南
        connect(hubGuangzhou, setOf(23L))    // 厦门北
        connect(hubGuangzhou, setOf(26L))    // 贵阳北
        connect(hubGuangzhou, setOf(11L))    // 南京南
        connect(hubGuangzhou, setOf(28L))    // 合肥南
        connect(hubGuangzhou, setOf(17L))    // 济南西

        // Wuhan ↔ rest
        connect(hubWuhan, hubChengdu)
        connect(hubWuhan, hubXian)
        connect(hubWuhan, hubChangsha)
        connect(hubWuhan, hubDongbei)
        connect(hubWuhan, setOf(12L))        // 杭州东
        connect(hubWuhan, setOf(20L))        // 福州南
        connect(hubWuhan, setOf(22L))        // 昆明南
        connect(hubWuhan, setOf(23L))        // 厦门北
        connect(hubWuhan, setOf(26L))        // 贵阳北
        connect(hubWuhan, setOf(17L))        // 济南西
        connect(hubWuhan, setOf(25L))        // 青岛北

        // Chengdu ↔ rest
        connect(hubChengdu, hubXian)
        connect(hubChengdu, hubChangsha)
        connect(hubChengdu, hubDongbei)
        connect(hubChengdu, setOf(12L))      // 杭州东
        connect(hubChengdu, setOf(20L))      // 福州南
        connect(hubChengdu, setOf(22L))      // 昆明南
        connect(hubChengdu, setOf(23L))      // 厦门北
        connect(hubChengdu, setOf(26L))      // 贵阳北
        connect(hubChengdu, setOf(17L))      // 济南西
        connect(hubChengdu, setOf(25L))      // 青岛北

        // Xian ↔ rest
        connect(hubXian, hubChangsha)
        connect(hubXian, hubDongbei)
        connect(hubXian, setOf(12L))         // 杭州东
        connect(hubXian, setOf(22L))         // 昆明南
        connect(hubXian, setOf(23L))         // 厦门北
        connect(hubXian, setOf(26L))         // 贵阳北
        connect(hubXian, setOf(20L))         // 福州南
        connect(hubXian, setOf(25L))         // 青岛北

        // Regional
        connect(setOf(16L), setOf(17L))      // 天津↔济南西
        connect(setOf(16L), setOf(18L))      // 天津↔沈阳北
        connect(setOf(16L), setOf(24L))      // 天津↔大连北
        connect(setOf(17L), setOf(25L))      // 济南西↔青岛北
        connect(setOf(17L), setOf(18L))      // 济南西↔沈阳北
        connect(setOf(18L), setOf(19L))      // 沈阳北↔哈尔滨西
        connect(setOf(18L), setOf(24L))      // 沈阳北↔大连北
        connect(setOf(12L), setOf(20L))      // 杭州东↔福州南
        connect(setOf(12L), setOf(23L))      // 杭州东↔厦门北
        connect(setOf(20L), setOf(23L))      // 福州南↔厦门北
        connect(setOf(15L), setOf(27L))      // 长沙南↔南昌西
        connect(setOf(26L), setOf(22L))      // 贵阳北↔昆明南
        connect(setOf(28L), setOf(11L))      // 合肥南↔南京南
        connect(setOf(14L), setOf(17L))      // 郑州东↔济南西
        connect(setOf(14L), setOf(8L))       // 郑州东↔武汉
        connect(setOf(14L), setOf(13L))      // 郑州东↔西安北

        // Generate trains: 2-3 per route at different times
        val rng = Random(42)
        var trainNum = 1
        val allTrains = mutableListOf<Train>()
        val allStops = mutableListOf<TrainStop>()

        val typeWeights = listOf("G" to 60, "D" to 25, "K" to 15)

        for (route in routes) {
            // Determine how many trains for this route (2-3)
            val dist = routeDistance(route.dep, route.arr)
            val count = when {
                dist < 8 -> 2     // short distance: 2 trains
                dist < 15 -> 2 + rng.nextInt(2)  // medium: 2-3
                else -> 3          // long distance: 3 trains
            }

            val timeSlots = when (count) {
                2 -> listOf(7, 14)       // morning, afternoon
                else -> listOf(6, 12, 17) // morning, noon, evening
            }

            for (slot in timeSlots.take(count)) {
                val type = weightedPick(typeWeights, rng)
                val dur = (dist * 40 + rng.nextInt(-30, 30)).coerceAtLeast(30)
                val depHour = slot + rng.nextInt(0, 2)
                val depMin = rng.nextInt(0, 60)
                val depTime = "${pad2(depHour)}:${pad2(depMin)}"
                val arrTotal = depHour * 60 + depMin + dur
                val arrHour = arrTotal / 60 % 24
                val arrMin = arrTotal % 60
                val arrTime = "${pad2(arrHour)}:${pad2(arrMin)}"
                val dayOff = if (depHour * 60 + depMin + dur >= 1440) 1 else 0

                val prefix = when (type) {
                    "G" -> "G"
                    "D" -> "D"
                    else -> "K"
                }
                val number = "$prefix$trainNum"
                trainNum++

                allTrains.add(Train(
                    number = number,
                    type = type,
                    departureStationId = route.dep,
                    arrivalStationId = route.arr,
                    durationMinutes = dur
                ))

                val trainId = allTrains.size.toLong()
                allStops.add(TrainStop(trainId = trainId, stationId = route.dep, stopOrder = 1,
                    arrivalTime = "--", departureTime = depTime, dayOffset = 0))
                allStops.add(TrainStop(trainId = trainId, stationId = route.arr, stopOrder = 2,
                    arrivalTime = arrTime, departureTime = "--", dayOffset = dayOff))
            }
        }

        db.trainDao().insertAll(allTrains)
        db.trainDao().insertStops(allStops)

        // Seat types
        val allSeatTypes = mutableListOf<SeatType>()
        for ((i, train) in allTrains.withIndex()) {
            val tid = i + 1L
            val dur = train.durationMinutes
            when (train.type) {
                "G" -> {
                    allSeatTypes.add(SeatType(trainId = tid, typeName = "二等座", price = 300 + (dur * 0.8).toInt().toDouble(), totalCount = 600))
                    allSeatTypes.add(SeatType(trainId = tid, typeName = "一等座", price = 500 + (dur * 1.2).toInt().toDouble(), totalCount = 100))
                    allSeatTypes.add(SeatType(trainId = tid, typeName = "商务座", price = 900 + (dur * 2.0).toInt().toDouble(), totalCount = 28))
                }
                "D" -> {
                    allSeatTypes.add(SeatType(trainId = tid, typeName = "二等座", price = 150 + (dur * 0.5).toInt().toDouble(), totalCount = 500))
                    allSeatTypes.add(SeatType(trainId = tid, typeName = "一等座", price = 250 + (dur * 0.8).toInt().toDouble(), totalCount = 80))
                }
                "K" -> {
                    allSeatTypes.add(SeatType(trainId = tid, typeName = "硬座", price = 50 + (dur * 0.2).toInt().toDouble(), totalCount = 500))
                    allSeatTypes.add(SeatType(trainId = tid, typeName = "硬卧上", price = 100 + (dur * 0.3).toInt().toDouble(), totalCount = 200))
                    allSeatTypes.add(SeatType(trainId = tid, typeName = "硬卧中", price = 110 + (dur * 0.3).toInt().toDouble(), totalCount = 200))
                    allSeatTypes.add(SeatType(trainId = tid, typeName = "硬卧下", price = 120 + (dur * 0.3).toInt().toDouble(), totalCount = 200))
                    allSeatTypes.add(SeatType(trainId = tid, typeName = "软卧", price = 200 + (dur * 0.5).toInt().toDouble(), totalCount = 80))
                }
            }
        }
        db.seatTypeDao().insertAll(allSeatTypes)
        Log.d("TTBS_SEED", "Seeded ${allTrains.size} trains, ${allStops.size} stops, ${allSeatTypes.size} seat types")
    }

    private fun pad2(n: Int) = n.toString().padStart(2, '0')

    // Approximate station index distance (proxy for geographic distance)
    private fun routeDistance(dep: Long, arr: Long): Int {
        val position = mapOf(
            1L to 0, 2L to 0, 3L to 0,     // 北京
            16L to 1,                        // 天津
            17L to 3, 25L to 3,             // 济南/青岛
            14L to 5,                        // 郑州
            13L to 7,                        // 西安
            18L to 8, 19L to 12, 24L to 8,  // 东北
            4L to 10, 5L to 10, 11L to 10, 28L to 10, 12L to 11, // 长三角
            8L to 12, 9L to 12,             // 武汉
            20L to 13, 23L to 13,           // 福建
            10L to 15, 21L to 14,           // 成渝
            15L to 14, 27L to 14,           // 湘赣
            26L to 16, 22L to 17,           // 云贵
            6L to 16, 7L to 16              // 广东
        )
        val p1 = position[dep] ?: 10
        val p2 = position[arr] ?: 10
        return kotlin.math.abs(p1 - p2) + 2
    }

    private fun weightedPick(items: List<Pair<String, Int>>, rng: Random): String {
        val total = items.sumOf { it.second }
        var pick = rng.nextInt(total)
        for ((name, weight) in items) {
            pick -= weight
            if (pick < 0) return name
        }
        return items.first().first
    }
}
