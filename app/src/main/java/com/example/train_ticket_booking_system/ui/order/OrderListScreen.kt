package com.example.train_ticket_booking_system.ui.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.train_ticket_booking_system.data.entity.TrainOrder
import com.example.train_ticket_booking_system.ui.navigation.Repos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    repos: Repos,
    userPhone: String,
    navController: NavController
) {
    var orders by remember { mutableStateOf<List<TrainOrder>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("全部", "未出行", "已出行", "已退票", "已改签")
    val statuses = listOf(null, "未出行", "已出行", "已退票", "已改签")

    LaunchedEffect(userPhone) {
        orders = repos.orderRepo.getOrdersByUser(userPhone)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的订单") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
            val filtered = if (selectedTab == 0) orders else orders.filter { it.status == statuses[selectedTab] }
            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("暂无订单", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(filtered) { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                .clickable { navController.navigate("order_detail/${order.id}") }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row {
                                    Text(order.trainNumber, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(order.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${order.departureStationName} → ${order.arrivalStationName}", style = MaterialTheme.typography.bodyMedium)
                                Row {
                                    Text(order.departureDate, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("${order.departureTime} - ${order.arrivalTime}", style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("¥${String.format("%.1f", order.totalPrice)}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
