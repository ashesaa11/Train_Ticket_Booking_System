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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.train_ticket_booking_system.data.entity.SeatType
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectScreen(
    trainId: Long,
    trainNumber: String,
    date: String,
    orderRepo: OrderRepository,
    onBack: () -> Unit,
    onSeatSelect: (Long, String, Double) -> Unit
) {
    var seatTypes by remember { mutableStateOf<List<SeatType>>(emptyList()) }
    var selected by remember { mutableStateOf<SeatType?>(null) }
    var availableCounts by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(trainId, date) {
        val types = orderRepo.getSeatTypes(trainId)
        seatTypes = types
        val counts = mutableMapOf<Long, Int>()
        types.forEach { counts[it.id] = orderRepo.getAvailableSeats(it, date) }
        availableCounts = counts
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$trainNumber - 选择座位") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("$date", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(seatTypes) { seat ->
                    val available = availableCounts[seat.id] ?: 0
                    val isSoldOut = available <= 0
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = !isSoldOut) { selected = seat },
                        colors = if (selected?.id == seat.id)
                            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else
                            CardDefaults.cardColors()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected?.id == seat.id,
                                onClick = { if (!isSoldOut) selected = seat },
                                enabled = !isSoldOut
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(seat.typeName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (isSoldOut) "已售罄" else "余票: $available 张",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSoldOut) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "¥${String.format("%.1f", seat.price)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            androidx.compose.material3.Button(
                onClick = { selected?.let { onSeatSelect(it.id, it.typeName, it.price) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = selected != null
            ) {
                Text("下一步 - 选择乘客")
            }
        }
    }
}
