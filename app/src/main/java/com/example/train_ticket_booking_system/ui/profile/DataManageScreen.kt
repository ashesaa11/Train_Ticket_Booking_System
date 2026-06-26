package com.example.train_ticket_booking_system.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.train_ticket_booking_system.data.entity.Station
import com.example.train_ticket_booking_system.data.entity.Train
import com.example.train_ticket_booking_system.ui.navigation.Repos
import kotlinx.coroutines.launch

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
    var duration by remember { mutableStateOf("") }
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
            Text("添加站点", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(stationName, { stationName = it; stationError = null }, label = { Text("站名(如: 南京南)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(stationCity, { stationCity = it; stationError = null }, label = { Text("城市(如: 南京)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            stationError?.let { Text(it, color = androidx.compose.ui.graphics.Color(0xFFEA4335), style = androidx.compose.material3.MaterialTheme.typography.bodySmall) }
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
            Text("添加车次", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
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

            OutlinedTextField(duration, { duration = it; trainError = null }, label = { Text("历时(分钟)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            trainError?.let { Text(it, color = androidx.compose.ui.graphics.Color(0xFFEA4335), style = androidx.compose.material3.MaterialTheme.typography.bodySmall) }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val dur = duration.toIntOrNull()
                when {
                    trainNumber.isBlank() -> trainError = "请输入车次号"
                    trainType.isBlank() -> trainError = "请选择车次类型"
                    depStation == null -> trainError = "请选择出发站"
                    arrStation == null -> trainError = "请选择到达站"
                    depStation == arrStation -> trainError = "出发站和到达站不能相同"
                    dur == null || dur <= 0 -> trainError = "请输入有效的历时（分钟）"
                    else -> scope.launch {
                        try {
                            repos.trainRepo.insert(Train(number = trainNumber, type = trainType, departureStationId = depStation!!.id, arrivalStationId = arrStation!!.id, durationMinutes = dur))
                            Toast.makeText(context, "车次已添加", Toast.LENGTH_SHORT).show()
                            trainNumber = ""; trainType = ""; depStation = null; arrStation = null; duration = ""; trainError = null
                        } catch (e: Exception) {
                            trainError = "添加失败: ${e.message}"
                        }
                    }
                }
            }, enabled = trainNumber.isNotBlank() && trainType.isNotBlank() && depStation != null && arrStation != null && duration.isNotBlank()) { Text("添加车次") }
        }
    }
}
