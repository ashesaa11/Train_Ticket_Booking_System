package com.example.train_ticket_booking_system.ui.train

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AirlineSeatFlat
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.train_ticket_booking_system.data.entity.SeatType
import com.example.train_ticket_booking_system.data.repository.OrderRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectScreen(
    trainId: Long, trainNumber: String, date: String,
    orderRepo: OrderRepository,
    onBack: () -> Unit,
    onSeatSelect: (Long, String, Double) -> Unit
) {
    var seatTypes by remember { mutableStateOf<List<SeatType>>(emptyList()) }
    var selected by remember { mutableStateOf<SeatType?>(null) }
    var availableCounts by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }

    LaunchedEffect(trainId, date) {
        seatTypes = orderRepo.getSeatTypes(trainId)
        val counts = mutableMapOf<Long, Int>()
        seatTypes.forEach { counts[it.id] = orderRepo.getAvailableSeats(it, date) }
        availableCounts = counts
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$trainNumber · 选择座位", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A73E8), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(date, color = Color(0xFF5F6368), fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            LazyColumn(Modifier.weight(1f)) {
                items(seatTypes) { seat ->
                    val available = availableCounts[seat.id] ?: 0
                    val soldOut = available <= 0
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .clickable(enabled = !soldOut) { selected = seat },
                        shape = RoundedCornerShape(12.dp),
                        colors = if (selected?.id == seat.id) CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE))
                        else CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(if (selected?.id == seat.id) 4.dp else 1.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected?.id == seat.id, { if (!soldOut) selected = seat }, enabled = !soldOut)
                            Spacer(Modifier.width(12.dp))
                            Icon(seatIcon(seat.typeName), null, tint = if (soldOut) Color(0xFFDADCE0) else Color(0xFF1A73E8), modifier = Modifier.padding(end = 8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(seat.typeName, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                if (soldOut) Text("已售罄", color = Color(0xFFEA4335), fontSize = 12.sp)
                                else {
                                    Text("${available}/${seat.totalCount} 张", color = Color(0xFF5F6368), fontSize = 12.sp)
                                    Spacer(Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { (seat.totalCount - available).toFloat() / seat.totalCount },
                                        modifier = Modifier.fillMaxWidth(0.5f).height(4.dp),
                                        color = Color(0xFF1A73E8),
                                        trackColor = Color(0xFFDADCE0)
                                    )
                                }
                            }
                            Text("¥${String.format("%.0f", seat.price)}", fontWeight = FontWeight.Bold, color = if (soldOut) Color(0xFFDADCE0) else Color(0xFFEA4335), fontSize = 18.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { selected?.let { onSeatSelect(it.id, it.typeName, it.price) } },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = selected != null,
                shape = RoundedCornerShape(12.dp)
            ) { Text("下一步 · 选择乘客", fontSize = 16.sp) }
        }
    }
}

fun seatIcon(name: String): ImageVector = when {
    name.contains("商务") -> Icons.Default.AirlineSeatFlat
    name.contains("一等") -> Icons.Default.EventSeat
    name.contains("软卧") || name.contains("硬卧") -> Icons.Default.Hotel
    else -> Icons.Default.Chair
}
