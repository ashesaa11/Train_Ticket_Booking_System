package com.example.train_ticket_booking_system.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.train_ticket_booking_system.data.entity.Station
import com.example.train_ticket_booking_system.ui.navigation.Repos
import com.example.train_ticket_booking_system.util.DateTimeUtil
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repos: Repos,
    onNavigate: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var stations by remember { mutableStateOf<List<Station>>(emptyList()) }
    var fromStation by remember { mutableStateOf<Station?>(null) }
    var toStation by remember { mutableStateOf<Station?>(null) }
    var selectedDate by remember { mutableStateOf(DateTimeUtil.todayStr()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }
    var fromSearch by remember { mutableStateOf("") }
    var toSearch by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<String?>(null) }

    fun loadStations() {
        scope.launch {
            stations = repos.stationRepo.getAll()
        }
    }
    if (stations.isEmpty()) loadStations()

    fun doSearch() {
        if (fromStation == null || toStation == null) {
            showError = "请选择出发站和到达站"
            return
        }
        if (fromStation!!.id == toStation!!.id) {
            showError = "出发站和到达站不能相同"
            return
        }
        val date = DateTimeUtil.parseDate(selectedDate)
        if (date.isBefore(LocalDate.now())) {
            showError = "出发日期不能早于今天"
            return
        }
        showError = null
        onNavigate("train_list/${fromStation!!.id}/${toStation!!.id}/$selectedDate")
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "火车票预订",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Station selectors
            ExposedDropdownMenuBox(
                expanded = fromExpanded,
                onExpandedChange = { fromExpanded = it }
            ) {
                OutlinedTextField(
                    value = fromSearch.ifEmpty { fromStation?.name ?: "" },
                    onValueChange = { fromSearch = it },
                    readOnly = false,
                    label = { Text("出发站") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = fromExpanded,
                    onDismissRequest = { fromExpanded = false }
                ) {
                    stations.filter {
                        fromSearch.isEmpty() || it.name.contains(fromSearch, true) || it.city.contains(fromSearch, true)
                    }.forEach { station ->
                        DropdownMenuItem(
                            text = { Text("${station.name} (${station.city})") },
                            onClick = {
                                fromStation = station
                                fromSearch = ""
                                fromExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = toExpanded,
                onExpandedChange = { toExpanded = it }
            ) {
                OutlinedTextField(
                    value = toSearch.ifEmpty { toStation?.name ?: "" },
                    onValueChange = { toSearch = it },
                    readOnly = false,
                    label = { Text("到达站") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = toExpanded,
                    onDismissRequest = { toExpanded = false }
                ) {
                    stations.filter {
                        toSearch.isEmpty() || it.name.contains(toSearch, true) || it.city.contains(toSearch, true)
                    }.forEach { station ->
                        DropdownMenuItem(
                            text = { Text("${station.name} (${station.city})") },
                            onClick = {
                                toStation = station
                                toSearch = ""
                                toExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Date picker
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("出发日期") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = DateTimeUtil.parseDate(selectedDate)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault()).toLocalDate()
                                    .format(DateTimeFormatter.ISO_LOCAL_DATE)
                            }
                            showDatePicker = false
                        }) { Text("确定") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("取消") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { doSearch() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("查询车次")
            }

            showError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Entry cards
            Text(
                text = "快捷入口",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { doSearch() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Train, contentDescription = null, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("车票预订", style = MaterialTheme.typography.labelLarge)
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("orders") },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("订单查询", style = MaterialTheme.typography.labelLarge)
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("profile") },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("我的行程", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
