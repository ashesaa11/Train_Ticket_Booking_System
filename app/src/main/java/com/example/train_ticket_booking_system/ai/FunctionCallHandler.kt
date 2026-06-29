package com.example.train_ticket_booking_system.ai

import com.example.train_ticket_booking_system.data.entity.OrderItem
import com.example.train_ticket_booking_system.data.entity.Passenger
import com.example.train_ticket_booking_system.data.entity.Train
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import com.example.train_ticket_booking_system.data.repository.PassengerRepository
import com.example.train_ticket_booking_system.data.repository.StationRepository
import com.example.train_ticket_booking_system.data.repository.TrainRepository
import android.util.Log
import com.example.train_ticket_booking_system.util.DateTimeUtil
import com.example.train_ticket_booking_system.util.PriceCalculator
import org.json.JSONArray
import org.json.JSONObject

class FunctionCallHandler(
    private val stationRepo: StationRepository,
    private val trainRepo: TrainRepository,
    private val orderRepo: OrderRepository,
    private val passengerRepo: PassengerRepository,
    private val userPhone: String
) {
    fun getToolDefinitions(): List<JSONObject> = listOf(
        JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", "search_trains")
                put("description", "查询指定出发站到到达站的火车车次列表，返回车次号、类型、时间、座位和价格")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("from", JSONObject().apply { put("type", "string"); put("description", "出发城市或站名，如北京、上海虹桥") })
                        put("to", JSONObject().apply { put("type", "string"); put("description", "到达城市或站名，如上海、广州南") })
                        put("date", JSONObject().apply { put("type", "string"); put("description", "出发日期，格式yyyy-MM-dd") })
                    })
                    put("required", JSONArray(listOf("from", "to", "date")))
                })
            })
        },
        JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", "book_ticket")
                put("description", "为用户预订火车票。需要先调用search_trains获取trainId和seatType。一次只能订一种座位类型，可订多张。")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("train_id", JSONObject().apply { put("type", "integer"); put("description", "车次ID，从search_trains结果中获取") })
                        put("seat_type", JSONObject().apply { put("type", "string"); put("description", "座位类型，如二等座、一等座、商务座、硬座、硬卧上/中/下、软卧") })
                        put("date", JSONObject().apply { put("type", "string"); put("description", "出发日期，格式yyyy-MM-dd") })
                        put("passenger_names", JSONObject().apply {
                            put("type", "array")
                            put("items", JSONObject().apply { put("type", "string") })
                            put("description", "乘客姓名列表，必须从用户的常用乘客中选择")
                        })
                    })
                    put("required", JSONArray(listOf("train_id", "seat_type", "date", "passenger_names")))
                })
            })
        },
        JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", "refund_ticket")
                put("description", "退订指定的订单，返回退票费和退还金额")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("order_id", JSONObject().apply { put("type", "integer"); put("description", "要退订的订单ID") })
                    })
                    put("required", JSONArray(listOf("order_id")))
                })
            })
        },
        JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", "list_passengers")
                put("description", "查询当前用户的常用乘客列表，返回姓名、乘客类型、身份证号。购票前必须先调用此接口获取可用乘客。")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject())
                })
            })
        }
    )

    companion object { private const val TAG = "TTBS_AI_FC" }

    suspend fun execute(name: String, arguments: String): String {
        val args = JSONObject(arguments)
        Log.d(TAG, "execute: name=$name, args=$arguments")
        val result = when (name) {
            "search_trains" -> searchTrains(args)
            "book_ticket" -> bookTicket(args)
            "refund_ticket" -> refundTicket(args)
            "list_passengers" -> listPassengers()
            else -> "未知操作: $name"
        }
        Log.d(TAG, "execute result($name): ${result.take(200)}")
        return result
    }

    private suspend fun searchTrains(args: JSONObject): String {
        val from = args.getString("from")
        val to = args.getString("to")
        val date = args.optString("date", DateTimeUtil.todayStr())
        Log.d(TAG, "search_trains: from=$from, to=$to, date=$date")

        val fromStations = stationRepo.search(from)
        val toStations = stationRepo.search(to)
        if (fromStations.isEmpty()) return "未找到出发站: $from"
        if (toStations.isEmpty()) return "未找到到达站: $to"

        val trains = trainRepo.search(fromStations.first().id, toStations.first().id, date)
        Log.d(TAG, "search_trains: found ${trains.size} trains")
        if (trains.isEmpty()) return "未找到${fromStations.first().name}到${toStations.first().name}($date)的车次"

        val result = mutableListOf<String>()
        for (tws in trains) {
            val t = tws.train
            val seatTypes = orderRepo.getSeatTypes(t.id)
            val seatInfoList = mutableListOf<String>()
            for (st in seatTypes) {
                val avail = orderRepo.getAvailableSeats(st, date)
                seatInfoList.add("${st.typeName} ¥${st.price.toInt()}(${avail}张)")
            }
            result.add("ID:${t.id} | ${t.type}${t.number} | ${tws.stops.first().departureTime}-${tws.stops.last().arrivalTime} | ${t.durationMinutes}min | ${seatInfoList.joinToString("，")}")
        }
        return result.joinToString("\n")
    }

    private suspend fun bookTicket(args: JSONObject): String {
        val trainId = args.getLong("train_id")
        val seatType = args.getString("seat_type")
        val date = args.getString("date")
        val passengerNames = args.getJSONArray("passenger_names").let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        }
        Log.d(TAG, "book_ticket: trainId=$trainId, seatType=$seatType, date=$date, passengers=$passengerNames")

        val train = trainRepo.getById(trainId) ?: return "车次ID=$trainId 不存在"
        val seatTypes = orderRepo.getSeatTypes(trainId)
        val seat = seatTypes.find { it.typeName == seatType } ?: return "该车次没有${seatType}，可选: ${seatTypes.joinToString { it.typeName }}"

        val avail = orderRepo.getAvailableSeats(seat, date)
        if (avail < passengerNames.size) return "${seatType}仅剩${avail}张，不够${passengerNames.size}张"

        val allPassengers = passengerRepo.getByUser(userPhone)
        val matched = passengerNames.mapNotNull { name -> allPassengers.find { it.name == name } }
        if (matched.size != passengerNames.size) return "以下乘客不在常用列表中: ${passengerNames.filter { n -> allPassengers.none { it.name == n } }}"

        val stops = trainRepo.getStops(trainId)
        val items = matched.map { p -> OrderItem(orderId = 0, passengerName = p.name, passengerIdCard = p.idCard, seatType = seatType, price = seat.price) }
        val totalPrice = seat.price * matched.size
        val stations = listOf(stationRepo.getById(train.departureStationId), stationRepo.getById(train.arrivalStationId))

        val orderId = orderRepo.createOrder(
            userPhone, trainId, train.number,
            stations[0]?.name ?: "", stations[1]?.name ?: "",
            date, stops.first().departureTime, stops.last().arrivalTime, totalPrice, items
        )
        Log.d(TAG, "book_ticket success: orderId=$orderId, totalPrice=$totalPrice")
        return "购票成功！订单ID:$orderId | ${train.number} $date | $seatType x${matched.size} | 总价¥${totalPrice.toInt()}"
    }

    private suspend fun refundTicket(args: JSONObject): String {
        val orderId = args.getLong("order_id")
        Log.d(TAG, "refund_ticket: orderId=$orderId")
        val order = orderRepo.getOrderById(orderId) ?: return "订单ID=$orderId 不存在"
        if (order.status != "未出行") return "该订单状态为「${order.status}」，无法退票"

        val hours = DateTimeUtil.hoursUntilDeparture(order.departureDate, order.departureTime)
        val refundAmount = PriceCalculator.calcRefundAmount(order.totalPrice, hours)
        val fee = PriceCalculator.calcRefundFee(order.totalPrice, hours)
        orderRepo.updateOrderStatus(orderId, "已退票")

        Log.d(TAG, "refund_ticket success: orderId=$orderId, fee=$fee, refundAmount=$refundAmount")
        return "退票成功！订单ID:$orderId | ${order.trainNumber} | 退票费¥${fee.toInt()} | 退还¥${refundAmount.toInt()}"
    }

    private suspend fun listPassengers(): String {
        Log.d(TAG, "list_passengers called")
        val list = passengerRepo.getByUser(userPhone)
        if (list.isEmpty()) return "暂无常用乘客，请在「我的-常用乘客」中添加"
        Log.d(TAG, "list_passengers: found ${list.size} passengers")
        return list.joinToString("\n") { "${it.name} | ${it.passengerType} | ${it.idCard}" }
    }
}
