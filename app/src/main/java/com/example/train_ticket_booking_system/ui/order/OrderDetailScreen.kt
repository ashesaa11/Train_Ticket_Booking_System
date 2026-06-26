package com.example.train_ticket_booking_system.ui.order

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.train_ticket_booking_system.data.dao.OrderDao
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import com.example.train_ticket_booking_system.util.DateTimeUtil
import com.example.train_ticket_booking_system.util.PriceCalculator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(orderId: Long, orderRepo: OrderRepository, onBack: () -> Unit, onToHome: () -> Unit) {
    var orderWithItems by remember { mutableStateOf<OrderDao.OrderWithItems?>(null) }
    var showRefund by remember { mutableStateOf(false) }
    var showReschedule by remember { mutableStateOf(false) }
    var refundAmount by remember { mutableStateOf(0.0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(orderId) { orderWithItems = orderRepo.getOrderWithItems(orderId) }
    LaunchedEffect(orderWithItems) {
        val o = orderWithItems?.order ?: return@LaunchedEffect
        if (o.status == "未出行") refundAmount = PriceCalculator.calcRefundAmount(o.totalPrice, DateTimeUtil.hoursUntilDeparture(o.departureDate, o.departureTime))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("订单详情", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A73E8), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        orderWithItems?.let { owi ->
            val o = owi.order
            val sc = when(o.status) { "未出行" -> Color(0xFF1A73E8); "已出行" -> Color(0xFF188038); "已退票" -> Color(0xFFEA4335); "已改签" -> Color(0xFFF9AB00); else -> Color(0xFF5F6368) }
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(o.trainNumber, fontWeight = FontWeight.Bold, fontSize = 22.sp); Spacer(Modifier.width(8.dp))
                            Text(o.status, color = sc, fontWeight = FontWeight.Medium)
                        }
                        Text("${o.departureStationName} → ${o.arrivalStationName}", color = Color(0xFF5F6368))
                        HorizontalDivider(Modifier.padding(vertical = 10.dp))
                        Row { Text("日期  ", color = Color(0xFF5F6368)); Text(o.departureDate) }
                        Row { Text("时间  ", color = Color(0xFF5F6368)); Text("${o.departureTime} - ${o.arrivalTime}") }
                        Row { Text("总价  ", color = Color(0xFF5F6368)); Text("¥${String.format("%.1f", o.totalPrice)}", fontWeight = FontWeight.Bold, color = Color(0xFFEA4335)) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("车票明细", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                LazyColumn(Modifier.weight(1f)) {
                    items(owi.items) { item ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.passengerName, fontWeight = FontWeight.Medium); Text(item.passengerIdCard, fontSize = 12.sp, color = Color(0xFF5F6368))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(item.seatType, fontSize = 13.sp, color = Color(0xFF5F6368)); Text("¥${String.format("%.0f", item.price)}", fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                                }
                            }
                        }
                    }
                }
                if (o.status == "未出行") {
                    Row(Modifier.fillMaxWidth()) {
                        Button(onClick = { showReschedule = true }, Modifier.weight(1f), enabled = o.originalOrderId == null, shape = RoundedCornerShape(12.dp)) { Text("改签") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { showRefund = true }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335)), shape = RoundedCornerShape(12.dp)) { Text("退票") }
                    }
                } else Button(onClick = onToHome, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("返回首页") }
            }

            if (showRefund) {
                val hours = DateTimeUtil.hoursUntilDeparture(o.departureDate, o.departureTime)
                val fee = PriceCalculator.calcRefundFee(o.totalPrice, hours)
                AlertDialog(
                    onDismissRequest = { showRefund = false },
                    title = { Text("确认退票") },
                    text = { Text("退票费: ¥${String.format("%.1f", fee)}\n退还: ¥${String.format("%.1f", refundAmount)}\n距出发: ${hours}小时") },
                    confirmButton = {
                        TextButton(onClick = { scope.launch { orderRepo.updateOrderStatus(o.id, "已退票"); showRefund = false; orderWithItems = orderRepo.getOrderWithItems(orderId) } }) { Text("确认退票") }
                    },
                    dismissButton = { TextButton(onClick = { showRefund = false }) { Text("取消") } }
                )
            }
            if (showReschedule) {
                AlertDialog(
                    onDismissRequest = { showReschedule = false },
                    title = { Text("改签提示") },
                    text = { Text("改签后将跳转到同路线车次列表，原订单作废。\n改签费根据距出发时间计算。") },
                    confirmButton = { TextButton(onClick = { showReschedule = false }) { Text("知道了") } },
                    dismissButton = { TextButton(onClick = { showReschedule = false }) { Text("关闭") } }
                )
            }
        }
    }
}
