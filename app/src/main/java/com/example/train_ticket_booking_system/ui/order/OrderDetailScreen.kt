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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.train_ticket_booking_system.data.dao.OrderDao
import com.example.train_ticket_booking_system.data.repository.OrderRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long,
    orderRepo: OrderRepository,
    onBack: () -> Unit,
    onToHome: () -> Unit
) {
    var orderWithItems by remember { mutableStateOf<OrderDao.OrderWithItems?>(null) }
    LaunchedEffect(orderId) {
        orderWithItems = orderRepo.getOrderWithItems(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("订单详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") }
                }
            )
        }
    ) { padding ->
        orderWithItems?.let { owi ->
            val o = owi.order
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(o.trainNumber, style = MaterialTheme.typography.headlineMedium)
                        Text("${o.departureStationName} → ${o.arrivalStationName}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row { Text("日期: "); Text(o.departureDate) }
                        Row { Text("时间: "); Text("${o.departureTime} - ${o.arrivalTime}") }
                        Row { Text("状态: "); Text(o.status, color = MaterialTheme.colorScheme.primary) }
                        Row { Text("总价: "); Text("¥${String.format("%.1f", o.totalPrice)}", color = MaterialTheme.colorScheme.primary) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("车票明细", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(owi.items) { item ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(item.passengerName, style = MaterialTheme.typography.titleSmall)
                                Text("${item.seatType} · ¥${String.format("%.1f", item.price)}", style = MaterialTheme.typography.bodySmall)
                                Text(item.passengerIdCard, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onToHome, modifier = Modifier.fillMaxWidth()) {
                    Text("返回首页")
                }
            }
        }
    }
}
