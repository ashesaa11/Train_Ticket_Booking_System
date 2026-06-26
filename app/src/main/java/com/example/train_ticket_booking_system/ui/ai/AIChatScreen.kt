package com.example.train_ticket_booking_system.ui.ai

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.train_ticket_booking_system.data.ApiConfig
import com.example.train_ticket_booking_system.data.ApiConfigStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    userPhone: String,
    navController: NavController,
    viewModel: AIChatViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) { viewModel.initHandler(userPhone) }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI助手", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = {
                    IconButton(onClick = { viewModel.showConfig() }) { Icon(Icons.Default.Settings, "设置", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A73E8), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (!state.apiConfigured) {
                Card(
                    Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("尚未配置API", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        Spacer(Modifier.height(4.dp))
                        Text("点击右上角设置图标，填入API KEY后即可使用AI助手", fontSize = 13.sp, color = Color(0xFF5F6368))
                    }
                }
            }

            LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), state = listState) {
                items(state.messages) { msg ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.isUser) Color(0xFF1A73E8)
                                else Color(0xFFF1F3F4)
                            ),
                            modifier = Modifier.width(if (msg.text.length > 20) 300.dp else 200.dp)
                        ) {
                            Text(
                                msg.text, Modifier.padding(12.dp),
                                color = if (msg.isUser) Color.White else Color(0xFF202124),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                if (state.loading) {
                    item {
                        Row(Modifier.fillMaxWidth().padding(8.dp)) {
                            CircularProgressIndicator(Modifier.padding(8.dp), strokeWidth = 2.dp)
                            Text("AI正在思考...", Modifier.padding(8.dp), color = Color(0xFF5F6368), fontSize = 13.sp)
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    input, { input = it }, placeholder = { Text("输入指令...") },
                    singleLine = true, modifier = Modifier.weight(1f),
                    enabled = !state.loading, shape = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (input.isNotBlank() && !state.loading) {
                            viewModel.sendMessage(input.trim()); input = ""
                        }
                    },
                    enabled = !state.loading
                ) {
                    Icon(Icons.Default.Send, "发送", tint = if (!state.loading && input.isNotBlank()) Color(0xFF1A73E8) else Color(0xFFDADCE0))
                }
            }
        }
    }

    // API Config Dialog
    if (state.showConfig) {
        var apiUrl by remember { mutableStateOf("https://api.deepseek.com/v1") }
        var apiKey by remember { mutableStateOf("") }
        var model by remember { mutableStateOf("deepseek-chat") }
        val scope = rememberCoroutineScope()
        var loaded by remember { mutableStateOf(false) }
        val ctx = androidx.compose.ui.platform.LocalContext.current
        LaunchedEffect(Unit) {
            if (!loaded) {
                scope.launch {
                    val config = ApiConfigStore(ctx).config.first()
                    apiUrl = config.apiUrl; apiKey = config.apiKey; model = config.modelName
                    loaded = true
                }
            }
        }

        AlertDialog(
            onDismissRequest = { viewModel.hideConfig() },
            title = { Text("AI API 配置") },
            text = {
                Column {
                    Text("填入您的API信息，支持OpenAI兼容接口（DeepSeek、通义千问等）", fontSize = 13.sp, color = Color(0xFF5F6368))
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(apiUrl, { apiUrl = it }, label = { Text("API地址") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(apiKey, { apiKey = it }, label = { Text("API KEY") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(model, { model = it }, label = { Text("模型名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.saveApiConfig(apiUrl, apiKey, model) }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideConfig() }) { Text("取消") }
            }
        )
    }
}
