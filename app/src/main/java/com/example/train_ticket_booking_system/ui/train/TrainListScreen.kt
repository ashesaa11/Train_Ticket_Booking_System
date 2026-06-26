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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainListScreen(
    fromId: Long, toId: Long, date: String,
    onBack: () -> Unit,
    viewModel: TrainListViewModel = viewModel(),
    onTrainSelect: (Long, String, String, String, String, String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(fromId, toId, date) { viewModel.search(fromId, toId, date) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.fromStationName + " → " + state.toStationName, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A73E8), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Surface(color = Color(0xFFF1F3F4), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(8.dp)) {
                Text(date, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Medium, color = Color(0xFF1A73E8))
            }

            if (state.loading) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp))
            else if (state.trains.isEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Train, null, modifier = Modifier.padding(16.dp), tint = Color(0xFFDADCE0))
                    Text("暂无车次", color = Color(0xFF9AA0A6), fontSize = 16.sp)
                    Text("试试调整出发站或日期", color = Color(0xFF9AA0A6), fontSize = 13.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    items(state.trains) { tws ->
                        val t = tws.train
                        val depStop = tws.stops.firstOrNull { it.stationId == fromId }
                        val arrStop = tws.stops.firstOrNull { it.stationId == toId }
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable {
                                onTrainSelect(t.id, t.number, depStop?.departureTime ?: "--", arrStop?.arrivalTime ?: "--", state.fromStationName, state.toStationName)
                            },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(color = when(t.type) { "G" -> Color(0xFF1A73E8); "D" -> Color(0xFF188038); else -> Color(0xFF5F6368) }, shape = RoundedCornerShape(4.dp)) {
                                        Text(t.type, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(t.number, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Spacer(Modifier.height(14.dp))
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(depStop?.departureTime ?: "--", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202124))
                                        Text(state.fromStationName, fontSize = 12.sp, color = Color(0xFF5F6368))
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Train, null, tint = Color(0xFF1A73E8))
                                        Text("${t.durationMinutes / 60}h${t.durationMinutes % 60}min", fontSize = 12.sp, color = Color(0xFF5F6368))
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(arrStop?.arrivalTime ?: "--", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202124))
                                        Text(state.toStationName, fontSize = 12.sp, color = Color(0xFF5F6368))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
