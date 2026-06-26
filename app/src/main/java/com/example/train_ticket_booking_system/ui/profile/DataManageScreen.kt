package com.example.train_ticket_booking_system.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
    var depStationId by remember { mutableStateOf("") }
    var arrStationId by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据管理") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("添加站点", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(stationName, { stationName = it }, label = { Text("站名(如: 南京南)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(stationCity, { stationCity = it }, label = { Text("城市(如: 南京)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                scope.launch {
                    val code = stationName.take(3).uppercase()
                    repos.stationRepo.insert(Station(name = stationName, city = stationCity, code = code))
                    Toast.makeText(context, "站点已添加", Toast.LENGTH_SHORT).show()
                    stationName = ""; stationCity = ""
                }
            }, enabled = stationName.isNotBlank() && stationCity.isNotBlank()) { Text("添加站点") }
            Spacer(modifier = Modifier.height(24.dp))
            Text("添加车次", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(trainNumber, { trainNumber = it }, label = { Text("车次号(如: G123)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(trainType, { trainType = it }, label = { Text("类型(G/D/K)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(depStationId, { depStationId = it }, label = { Text("出发站ID(数字)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(arrStationId, { arrStationId = it }, label = { Text("到达站ID(数字)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(duration, { duration = it }, label = { Text("历时(分钟)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val dId = depStationId.toLongOrNull()
                val aId = arrStationId.toLongOrNull()
                val dur = duration.toIntOrNull()
                if (dId != null && aId != null && dur != null && trainNumber.isNotBlank() && trainType.isNotBlank()) {
                    scope.launch {
                        repos.trainRepo.insert(Train(number = trainNumber, type = trainType, departureStationId = dId, arrivalStationId = aId, durationMinutes = dur))
                        Toast.makeText(context, "车次已添加", Toast.LENGTH_SHORT).show()
                        trainNumber = ""; trainType = ""; depStationId = ""; arrStationId = ""; duration = ""
                    }
                }
            }, enabled = trainNumber.isNotBlank() && trainType.isNotBlank() && depStationId.isNotBlank() && arrStationId.isNotBlank() && duration.isNotBlank()) { Text("添加车次") }
        }
    }
}
