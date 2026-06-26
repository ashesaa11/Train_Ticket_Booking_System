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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.train_ticket_booking_system.data.dao.OrderDao
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import com.example.train_ticket_booking_system.util.DateTimeUtil
import com.example.train_ticket_booking_system.util.PriceCalculator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long,
    orderRepo: OrderRepository,
    onBack: () -> Unit,
    onToHome: () -> Unit
) {
    var orderWithItems by remember { mutableStateOf<OrderDao.OrderWithItems?>(null) }
    var showRefundDialog by remember { mutableStateOf(false) }
    var showRescheduleDialog by remember { mutableStateOf(false) }
    var refundAmount by remember { mutableStateOf(0.0) }

    LaunchedEffect(orderId) {
        orderWithItems = orderRepo.getOrderWithItems(orderId)
    }

    // Calculate refund amount
    LaunchedEffect(orderWithItems) {
        val o = orderWithItems?.order ?: return@LaunchedEffect
        if (o.status == "未出行") {
            val hours = DateTimeUtil.hoursUntilDeparture(o.departureDate, o.departureTime)
            refundAmount = PriceCalculator.calcRefundAmount(o.totalPrice, hours)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("订单详情") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }
            )
        }
    ) { padding ->
        orderWithItems?.let { owi ->
            val o = owi.order
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row {
                            Text(o.trainNumber, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(o.status, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("${o.departureStationName} → ${o.arrivalStationName}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row { Text("日期: "); Text(o.departureDate) }
                        Row { Text("时间: "); Text("${o.departureTime} - ${o.arrivalTime}") }
                        Row { Text("总价: "); Text("¥${String.format("%.1f", o.totalPrice)}", color = MaterialTheme.colorScheme.primary) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("车票明细", style = MaterialTheme.typography.titleMedium)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(owi.items) { item ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(item.passengerName, style = MaterialTheme.typography.titleSmall)
                                Text("${item.seatType} · ¥${String.format("%.1f", item.price)}")
                            }
                        }
                    }
                }
                if (o.status == "未出行") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showRescheduleDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = o.originalOrderId == null
                        ) { Text("改签") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showRefundDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("退票") }
                    }
                } else {
                    Button(onClick = onToHome, modifier = Modifier.fillMaxWidth()) { Text("返回首页") }
                }
            }

            if (showRefundDialog) {
                val hours = DateTimeUtil.hoursUntilDeparture(o.departureDate, o.departureTime)
                val fee = PriceCalculator.calcRefundFee(o.totalPrice, hours)
                AlertDialog(
                    onDismissRequest = { showRefundDialog = false },
                    title = { Text("确认退票") },
                    text = { Text("退票费: ¥${String.format("%.1f", fee)}\n退还: ¥${String.format("%.1f", refundAmount)}\n距离出发: ${hours}小时") },
                    confirmButton = {
                        val scope = rememberCoroutineScope()
                        TextButton(onClick = {
                            scope.launch {
                                orderRepo.updateOrderStatus(o.id, "已退票")
                                showRefundDialog = false
                                orderWithItems = orderRepo.getOrderWithItems(orderId)
                            }
                        }) { Text("确认退票") }
                    },
                    dismissButton = { TextButton(onClick = { showRefundDialog = false }) { Text("取消") } }
                )
            }

            if (showRescheduleDialog) {
                AlertDialog(
                    onDismissRequest = { showRescheduleDialog = false },
                    title = { Text("改签") },
                    text = {
                        val hours = DateTimeUtil.hoursUntilDeparture(o.departureDate, o.departureTime)
                        val fee = PriceCalculator.calcRescheduleFee(o.totalPrice, hours)
                        Text("改签费: ¥${String.format("%.1f", fee)}\n改签后可重新选择同路线车次\n距离出发: ${hours}小时")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showRescheduleDialog = false
                            // Navigate to train list for reschedule
                        }) { Text("继续改签") }
                    },
                    dismissButton = { TextButton(onClick = { showRescheduleDialog = false }) { Text("取消") } }
                )
            }
        }
    }
}
