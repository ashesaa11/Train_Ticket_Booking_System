package com.example.train_ticket_booking_system.ui.profile

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.train_ticket_booking_system.ui.navigation.Repos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(repos: Repos, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A73E8), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            // Avatar card
            Surface(color = Color(0xFF1A73E8), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(56.dp).clip(CircleShape), color = Color.White.copy(alpha = 0.2f)) {
                        Icon(Icons.Default.Person, null, Modifier.padding(12.dp), tint = Color.White)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("火车票用户", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("已登录", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            SectionTitle("常用功能")
            MenuCard(Icons.Default.Person, "常用乘客", "管理您的常用乘车人") { navController.navigate("passenger_manage") }
            MenuCard(Icons.Default.Edit, "数据管理", "添加站点和车次信息") { navController.navigate("data_manage") }
            MenuCard(Icons.Default.Settings, "AI助手", "智能查询和订票助手") { navController.navigate("ai_chat") }

            Spacer(Modifier.height(24.dp))
            Text("v1.0 · 火车票购票系统", color = Color(0xFF9AA0A6), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6368), fontSize = 13.sp)
}

@Composable
private fun MenuCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = Color(0xFFE8F0FE)) {
                Icon(icon, null, Modifier.padding(8.dp), tint = Color(0xFF1A73E8))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp); Text(subtitle, fontSize = 12.sp, color = Color(0xFF5F6368)) }
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFDADCE0))
        }
    }
}
