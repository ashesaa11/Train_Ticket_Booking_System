package com.example.train_ticket_booking_system.ui.profile

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.train_ticket_booking_system.data.entity.Passenger
import com.example.train_ticket_booking_system.data.repository.PassengerRepository
import com.example.train_ticket_booking_system.TTBSApplication
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerManageScreen(
    userPhone: String,
    navController: NavController,
    passengerRepo: PassengerRepository
) {
    var passengers by remember { mutableStateOf<List<Passenger>>(emptyList()) }
    var showAdd by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun load() { scope.launch { passengers = passengerRepo.getByUser(userPhone) } }
    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("常用乘客") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = {
                    IconButton(onClick = { showAdd = !showAdd }) { Icon(Icons.Default.Add, "添加") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (showAdd) {
                AddPassengerCard(
                    onSave = { name, idCard, type ->
                        scope.launch {
                            passengerRepo.add(Passenger(userPhone = userPhone, name = name, idCard = idCard, passengerType = type))
                            showAdd = false
                            load()
                            Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onCancel = { showAdd = false }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            LazyColumn {
                items(passengers) { p ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name, style = MaterialTheme.typography.titleMedium)
                                Text("${p.idCard} · ${p.passengerType}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = {
                                scope.launch { passengerRepo.delete(p); load() }
                            }) {
                                Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPassengerCard(onSave: (String, String, String) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var idCard by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("成人") }
    val types = listOf("成人", "儿童", "学生")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("姓名") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(idCard, { idCard = it }, label = { Text("身份证号") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                OutlinedTextField(type, {}, readOnly = true, label = { Text("乘客类型") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    types.forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = { type = it; typeExpanded = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { onSave(name, idCard, type) }, enabled = name.isNotBlank() && idCard.isNotBlank(), modifier = Modifier.weight(1f)) { Text("保存") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("取消") }
            }
        }
    }
}
