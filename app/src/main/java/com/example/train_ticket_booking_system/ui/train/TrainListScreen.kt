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
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.train_ticket_booking_system.data.entity.TrainStop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainListScreen(
    fromId: Long,
    toId: Long,
    date: String,
    onBack: () -> Unit,
    viewModel: TrainListViewModel = viewModel(),
    onTrainSelect: (Long, String, String, String, String, String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(fromId, toId, date) {
        viewModel.search(fromId, toId, date)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${state.fromStationName} → ${state.toStationName}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "$date",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (state.trains.isEmpty()) {
                Text(
                    text = "未找到车次",
                    modifier = Modifier.padding(32.dp).align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    items(state.trains) { trainWithStops ->
                        val train = trainWithStops.train
                        val depStop = trainWithStops.stops.firstOrNull { it.stationId == fromId }
                        val arrStop = trainWithStops.stops.firstOrNull { it.stationId == toId }
                        TrainCard(
                            trainNumber = train.number,
                            trainType = train.type,
                            departureTime = depStop?.departureTime ?: "--",
                            arrivalTime = arrStop?.arrivalTime ?: "--",
                            departureStation = state.fromStationName,
                            arrivalStation = state.toStationName,
                            duration = train.durationMinutes,
                            onClick = {
                                onTrainSelect(
                                    train.id,
                                    train.number,
                                    depStop?.departureTime ?: "--",
                                    arrStop?.arrivalTime ?: "--",
                                    state.fromStationName,
                                    state.toStationName
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainCard(
    trainNumber: String,
    trainType: String,
    departureTime: String,
    arrivalTime: String,
    departureStation: String,
    arrivalStation: String,
    duration: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = trainType,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trainNumber,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(departureTime, style = MaterialTheme.typography.headlineSmall)
                    Text(departureStation, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Train, contentDescription = null)
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("${duration / 60}h${duration % 60}m", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(arrivalTime, style = MaterialTheme.typography.headlineSmall)
                    Text(arrivalStation, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
