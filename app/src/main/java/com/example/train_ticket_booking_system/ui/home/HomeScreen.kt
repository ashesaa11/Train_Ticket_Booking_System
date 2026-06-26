package com.example.train_ticket_booking_system.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.train_ticket_booking_system.data.entity.Station
import com.example.train_ticket_booking_system.ui.navigation.Repos
import com.example.train_ticket_booking_system.util.DateTimeUtil
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class HotRoute(val fromCity: String, val toCity: String, val fromStationName: String, val toStationName: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repos: Repos, onNavigate: (String) -> Unit) {
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
    var reachableIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    var showError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        stations = repos.stationRepo.getAll()
    }

    fun doSearch() {
        if (fromStation == null || toStation == null) { showError = "请选择出发站和到达站"; return }
        if (fromStation!!.id == toStation!!.id) { showError = "出发站和到达站不能相同"; return }
        if (DateTimeUtil.parseDate(selectedDate).isBefore(LocalDate.now())) { showError = "出发日期不能早于今天"; return }
        showError = null
        onNavigate("train_list/${fromStation!!.id}/${toStation!!.id}/$selectedDate")
    }

    fun searchRoute(from: String, to: String) {
        val fromSt = stations.find { it.name.contains(from) || it.city.contains(from) }
        val toSt = stations.find { it.name.contains(to) || it.city.contains(to) }
        if (fromSt != null && toSt != null) {
            onNavigate("train_list/${fromSt.id}/${toSt.id}/$selectedDate")
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Hero Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A73E8)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).padding(top = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("火车票预订", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("安全 · 便捷 · 智能出行", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Search Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-20).dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // From
                ExposedDropdownMenuBox(expanded = fromExpanded, onExpandedChange = { fromExpanded = it }) {
                    OutlinedTextField(
                        value = fromSearch.ifEmpty { fromStation?.name ?: "" },
                        onValueChange = { fromSearch = it },
                        label = { Text("出发站") },
                        leadingIcon = { Icon(Icons.Default.FlightTakeoff, null, tint = Color(0xFF1A73E8)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(fromExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                        stations.filter { fromSearch.isEmpty() || it.name.contains(fromSearch, true) || it.city.contains(fromSearch, true) }
                            .forEach { s ->
                                DropdownMenuItem(text = { Text("${s.name}（${s.city}）") }, onClick = {
                                    fromStation = s; fromSearch = ""; fromExpanded = false; toStation = null
                                    scope.launch { reachableIds = repos.trainRepo.getReachableStationIds(s.id) }
                                })
                            }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // To
                ExposedDropdownMenuBox(expanded = toExpanded, onExpandedChange = { toExpanded = it }) {
                    OutlinedTextField(
                        value = toSearch.ifEmpty { toStation?.name ?: "" },
                        onValueChange = { toSearch = it },
                        label = { Text("到达站") },
                        leadingIcon = { Icon(Icons.Default.FlightTakeoff, null, tint = Color(0xFF188038), modifier = Modifier.padding(start = 2.dp)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(toExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                        val filtered = if (fromStation != null && reachableIds.isNotEmpty()) {
                            stations.filter { reachableIds.contains(it.id) && (toSearch.isEmpty() || it.name.contains(toSearch, true)) }
                        } else {
                            stations.filter { toSearch.isEmpty() || it.name.contains(toSearch, true) || it.city.contains(toSearch, true) }
                        }
                        if (filtered.isEmpty() && fromStation != null) {
                            DropdownMenuItem(text = { Text("暂无直达车次") }, onClick = { toExpanded = false })
                        }
                        filtered.forEach { s ->
                            DropdownMenuItem(text = { Text("${s.name}（${s.city}）") }, onClick = {
                                toStation = s; toSearch = ""; toExpanded = false
                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Date
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("出发日期") },
                    leadingIcon = { Icon(Icons.Default.DateRange, null, tint = Color(0xFF5F6368)) },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp)
                )
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = DateTimeUtil.parseDate(selectedDate)
                            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = { TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                                    .format(DateTimeFormatter.ISO_LOCAL_DATE)
                            }
                            showDatePicker = false
                        }) { Text("确定") } },
                        dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
                    ) { DatePicker(state = datePickerState) }
                }

                showError?.let { Spacer(modifier = Modifier.height(4.dp)); Text(it, color = Color(0xFFEA4335), fontSize = 13.sp) }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { doSearch() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                ) {
                    Icon(Icons.Default.Search, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("查询车次", fontSize = 16.sp)
                }
            }
        }

        // Hot Routes
        Spacer(modifier = Modifier.height(24.dp))
        Text("热门路线", modifier = Modifier.padding(horizontal = 20.dp), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF202124))

        val hotRoutes = listOf(
            HotRoute("北京", "上海", "北京南", "上海虹桥"),
            HotRoute("广州", "深圳", "广州南", "深圳北"),
            HotRoute("成都", "西安", "成都东", "西安北"),
            HotRoute("武汉", "广州", "武汉", "广州南"),
            HotRoute("上海", "杭州", "上海", "杭州东"),
            HotRoute("深圳", "长沙", "深圳北", "长沙南"),
            HotRoute("郑州", "武汉", "郑州东", "武汉"),
            HotRoute("南京", "武汉", "南京南", "武汉")
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            hotRoutes.forEach { route ->
                Card(
                    modifier = Modifier.padding(end = 12.dp).clickable { searchRoute(route.fromCity, route.toCity) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${route.fromCity} → ${route.toCity}", fontWeight = FontWeight.Medium, color = Color(0xFF202124))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${route.fromStationName} - ${route.toStationName}", fontSize = 12.sp, color = Color(0xFF5F6368))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
