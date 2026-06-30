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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.train_ticket_booking_system.data.entity.TrainOrder
import com.example.train_ticket_booking_system.ui.navigation.Repos
import com.example.train_ticket_booking_system.util.DateTimeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(repos: Repos, userPhone: String, navController: NavController) {
    var orders by remember { mutableStateOf<List<TrainOrder>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("全部", "未出行", "已出行", "已退票")
    val statuses = listOf(null, "未出行", "已出行", "已退票")

    LaunchedEffect(userPhone) {
        repos.orderRepo.autoUpdateExpiredOrders(DateTimeUtil.todayStr())
        orders = repos.orderRepo.getOrdersByUser(userPhone)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的订单", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A73E8), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = Color(0xFF1A73E8), indicator = { tabPositions -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = Color(0xFF1A73E8)) }) {
                tabs.forEachIndexed { i, t -> Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(t) }) }
            }
            val filtered = if (selectedTab == 0) orders else orders.filter { it.status == statuses[selectedTab] }
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Train, null, tint = Color(0xFFDADCE0), modifier = Modifier.padding(16.dp))
                        Text("暂无订单", color = Color(0xFF9AA0A6), fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(Modifier.padding(16.dp)) {
                    items(filtered) { o ->
                        Card(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { navController.navigate("order_detail/${o.id}") },
                            shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row {
                                    Text(o.trainNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(Modifier.width(8.dp))
                                    val sc = when(o.status) { "未出行" -> Color(0xFF1A73E8); "已出行" -> Color(0xFF188038); "已退票" -> Color(0xFFEA4335); else -> Color(0xFF5F6368) }
                                    Text(o.status, color = sc, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text("${o.departureStationName} → ${o.arrivalStationName}", color = Color(0xFF5F6368))
                                Row { Text(o.departureDate, fontSize = 13.sp, color = Color(0xFF5F6368)); Spacer(Modifier.width(12.dp)); Text("${o.departureTime}-${o.arrivalTime}", fontSize = 13.sp, color = Color(0xFF5F6368)) }
                                Spacer(Modifier.height(4.dp))
                                Text("¥${String.format("%.1f", o.totalPrice)}", fontWeight = FontWeight.Bold, color = Color(0xFFEA4335), fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
