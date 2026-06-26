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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.train_ticket_booking_system.data.entity.Passenger
import com.example.train_ticket_booking_system.data.repository.PassengerRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerSelectScreen(
    userPhone: String,
    passengerRepo: PassengerRepository,
    onBack: () -> Unit,
    onConfirm: (List<Passenger>) -> Unit
) {
    var passengers by remember { mutableStateOf<List<Passenger>>(emptyList()) }
    val selected = remember { mutableStateListOf<Passenger>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        passengers = passengerRepo.getByUser(userPhone)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择乘客") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (passengers.isEmpty()) {
                Text(
                    "暂无常用乘客，请先在「我的」中添加",
                    modifier = Modifier.padding(32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(passengers) { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selected.contains(p)) selected.remove(p)
                                    else selected.add(p)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selected.contains(p),
                                onCheckedChange = {
                                    if (it) selected.add(p) else selected.remove(p)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(p.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${p.idCard} · ${p.passengerType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "已选: ${selected.size} 人",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onConfirm(selected.toList()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selected.isNotEmpty()
            ) {
                Text("确认乘客")
            }
        }
    }
}
