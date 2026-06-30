package com.example.train_ticket_booking_system.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.train_ticket_booking_system.data.entity.Station
import com.example.train_ticket_booking_system.data.entity.Train
import com.example.train_ticket_booking_system.data.entity.TrainStop
import com.example.train_ticket_booking_system.ui.navigation.Repos
import com.example.train_ticket_booking_system.util.DateTimeUtil
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManageScreen(repos: Repos, navController: NavController) {
    var stationName by remember { mutableStateOf("") }
    var stationCity by remember { mutableStateOf("") }
    var trainNumber by remember { mutableStateOf("") }
    var trainType by remember { mutableStateOf("") }
    var trainTypeExpanded by remember { mutableStateOf(false) }
    var depStation by remember { mutableStateOf<Station?>(null) }
    var arrStation by remember { mutableStateOf<Station?>(null) }
    var depExpanded by remember { mutableStateOf(false) }
    var arrExpanded by remember { mutableStateOf(false) }
    var depTime by remember { mutableStateOf("") }
    var arrTime by remember { mutableStateOf("") }
    var showDepTimePicker by remember { mutableStateOf(false) }
    var showArrTimePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(DateTimeUtil.todayStr()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var stationError by remember { mutableStateOf<String?>(null) }
    var trainError by remember { mutableStateOf<String?>(null) }
    var stations by remember { mutableStateOf<List<Station>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val trainTypes = listOf("G", "D", "K")

    LaunchedEffect(Unit) { stations = repos.stationRepo.getAll() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据管理") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("添加站点", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(stationName, { stationName = it; stationError = null }, label = { Text("站名(如: 南京南)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(stationCity, { stationCity = it; stationError = null }, label = { Text("城市(如: 南京)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            stationError?.let { Text(it, color = Color(0xFFEA4335), style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                when {
                    stationName.isBlank() -> stationError = "请输入站名"
                    stationCity.isBlank() -> stationError = "请输入城市"
                    stations.any { it.name == stationName } -> stationError = "站点「$stationName」已存在"
                    else -> scope.launch {
                        try {
                            repos.stationRepo.insert(Station(name = stationName, city = stationCity, code = stationName.take(3).uppercase()))
                            Toast.makeText(context, "站点已添加", Toast.LENGTH_SHORT).show()
                            stationName = ""; stationCity = ""; stationError = null
                            stations = repos.stationRepo.getAll()
                        } catch (e: Exception) {
                            stationError = "添加失败: ${e.message}"
                        }
                    }
                }
            }, enabled = stationName.isNotBlank() && stationCity.isNotBlank()) { Text("添加站点") }

            Spacer(modifier = Modifier.height(24.dp))
            Text("添加车次", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(trainNumber, { trainNumber = it; trainError = null }, label = { Text("车次号(如: G123)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            ExposedDropdownMenuBox(expanded = trainTypeExpanded, onExpandedChange = { trainTypeExpanded = it }) {
                OutlinedTextField(trainType, {}, readOnly = true, label = { Text("类型") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(trainTypeExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = trainTypeExpanded, onDismissRequest = { trainTypeExpanded = false }) {
                    trainTypes.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { trainType = it; trainTypeExpanded = false; trainError = null }) }
                }
            }

            ExposedDropdownMenuBox(expanded = depExpanded, onExpandedChange = { depExpanded = it }) {
                OutlinedTextField(depStation?.let { "${it.name}（${it.city}）" } ?: "", {}, readOnly = true, label = { Text("出发站") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(depExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = depExpanded, onDismissRequest = { depExpanded = false }) {
                    stations.forEach { s -> DropdownMenuItem(text = { Text("${s.name}（${s.city}）ID:${s.id}") }, onClick = { depStation = s; depExpanded = false; trainError = null }) }
                }
            }

            ExposedDropdownMenuBox(expanded = arrExpanded, onExpandedChange = { arrExpanded = it }) {
                OutlinedTextField(arrStation?.let { "${it.name}（${it.city}）" } ?: "", {}, readOnly = true, label = { Text("到达站") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(arrExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = arrExpanded, onDismissRequest = { arrExpanded = false }) {
                    stations.forEach { s -> DropdownMenuItem(text = { Text("${s.name}（${s.city}）ID:${s.id}") }, onClick = { arrStation = s; arrExpanded = false; trainError = null }) }
                }
            }

            // Date
            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = selectedDate, onValueChange = {}, readOnly = true,
                    label = { Text("出发日期") },
                    leadingIcon = { Icon(Icons.Default.DateRange, null, tint = Color(0xFF5F6368)) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false
                )
            }
            if (showDatePicker) {
                val todayEpoch = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dpState = rememberDatePickerState(
                    initialSelectedDateMillis = DateTimeUtil.parseDate(selectedDate).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayEpoch
                        override fun isSelectableYear(year: Int) = true
                    }
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = { TextButton(onClick = {
                        dpState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        }
                        showDatePicker = false
                    }) { Text("确定") } },
                    dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
                ) { DatePicker(state = dpState) }
            }

            // Departure time
            Box(modifier = Modifier.fillMaxWidth().clickable { showDepTimePicker = true }) {
                OutlinedTextField(
                    value = depTime.ifEmpty { "点击选择出发时间" },
                    onValueChange = {}, readOnly = true,
                    label = { Text("出发时间") },
                    leadingIcon = { Icon(Icons.Default.DateRange, null, tint = Color(0xFF5F6368)) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false
                )
            }
            if (showDepTimePicker) {
                val tpState = rememberTimePickerState(
                    initialHour = depTime.takeIf { it.isNotBlank() }?.split(":")?.get(0)?.toInt() ?: 8,
                    initialMinute = depTime.takeIf { it.isNotBlank() }?.split(":")?.get(1)?.toInt() ?: 0,
                    is24Hour = true
                )
                TimePickerDialog(
                    onDismissRequest = { showDepTimePicker = false },
                    title = { Text("出发时间") },
                    confirmButton = {
                        TextButton(onClick = {
                            depTime = "${tpState.hour.toString().padStart(2, '0')}:${tpState.minute.toString().padStart(2, '0')}"
                            showDepTimePicker = false
                        }) { Text("确定") }
                    },
                    dismissButton = { TextButton(onClick = { showDepTimePicker = false }) { Text("取消") } }
                ) { TimePicker(state = tpState) }
            }

            // Arrival time
            Box(modifier = Modifier.fillMaxWidth().clickable { showArrTimePicker = true }) {
                OutlinedTextField(
                    value = arrTime.ifEmpty { "点击选择到达时间" },
                    onValueChange = {}, readOnly = true,
                    label = { Text("到达时间") },
                    leadingIcon = { Icon(Icons.Default.DateRange, null, tint = Color(0xFF5F6368)) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false
                )
            }
            if (showArrTimePicker) {
                val tpState = rememberTimePickerState(
                    initialHour = arrTime.takeIf { it.isNotBlank() }?.split(":")?.get(0)?.toInt() ?: 12,
                    initialMinute = arrTime.takeIf { it.isNotBlank() }?.split(":")?.get(1)?.toInt() ?: 0,
                    is24Hour = true
                )
                TimePickerDialog(
                    onDismissRequest = { showArrTimePicker = false },
                    title = { Text("到达时间") },
                    confirmButton = {
                        TextButton(onClick = {
                            arrTime = "${tpState.hour.toString().padStart(2, '0')}:${tpState.minute.toString().padStart(2, '0')}"
                            showArrTimePicker = false
                        }) { Text("确定") }
                    },
                    dismissButton = { TextButton(onClick = { showArrTimePicker = false }) { Text("取消") } }
                ) { TimePicker(state = tpState) }
            }

            trainError?.let { Text(it, color = Color(0xFFEA4335), style = MaterialTheme.typography.bodySmall) }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                when {
                    trainNumber.isBlank() -> trainError = "请输入车次号"
                    trainType.isBlank() -> trainError = "请选择车次类型"
                    depStation == null -> trainError = "请选择出发站"
                    arrStation == null -> trainError = "请选择到达站"
                    depStation == arrStation -> trainError = "出发站和到达站不能相同"
                    depTime.isBlank() -> trainError = "请输入出发时间"
                    arrTime.isBlank() -> trainError = "请输入到达时间"
                    else -> scope.launch {
                        try {
                            val depTotal = parseTime(depTime)
                            val arrTotal = parseTime(arrTime)
                            if (arrTotal <= depTotal) { trainError = "到达时间必须晚于出发时间"; return@launch }
                            val dur = arrTotal - depTotal
                            val trainId = repos.trainRepo.insert(Train(number = trainNumber, type = trainType, departureStationId = depStation!!.id, arrivalStationId = arrStation!!.id, durationMinutes = dur))
                            repos.trainRepo.insertStop(TrainStop(trainId = trainId, stationId = depStation!!.id, stopOrder = 1, arrivalTime = "--", departureTime = depTime, dayOffset = 0))
                            repos.trainRepo.insertStop(TrainStop(trainId = trainId, stationId = arrStation!!.id, stopOrder = 2, arrivalTime = arrTime, departureTime = "--", dayOffset = 0))
                            Toast.makeText(context, "车次已添加（历时${dur}分钟）", Toast.LENGTH_SHORT).show()
                            trainNumber = ""; trainType = ""; depStation = null; arrStation = null; depTime = ""; arrTime = ""; trainError = null
                        } catch (e: Exception) {
                            trainError = "添加失败: ${e.message}"
                        }
                    }
                }
            }, enabled = trainNumber.isNotBlank() && trainType.isNotBlank() && depStation != null && arrStation != null && depTime.isNotBlank() && arrTime.isNotBlank()) { Text("添加车次") }
        }
    }
}

private fun parseTime(t: String): Int {
    val parts = t.trim().split(":")
    return parts[0].toInt() * 60 + parts[1].toInt()
}
