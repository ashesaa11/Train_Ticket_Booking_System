package com.example.train_ticket_booking_system.ui.order

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.train_ticket_booking_system.data.entity.Passenger
import com.example.train_ticket_booking_system.data.entity.OrderItem
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import com.example.train_ticket_booking_system.data.repository.UserRepository
import com.example.train_ticket_booking_system.TTBSApplication
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val INACTIVITY_TIMEOUT_MS = 60_000L

data class PaymentState(val processing: Boolean = false, val success: Boolean = false, val orderId: Long? = null, val error: String? = null)

class PaymentViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as TTBSApplication).database
    private val userRepo = UserRepository(db.userDao())
    private val orderRepo = OrderRepository(db.orderDao(), db.seatTypeDao())
    private val _state = MutableStateFlow(PaymentState())
    val state: StateFlow<PaymentState> = _state

    fun pay(userPhone: String, trainId: Long, trainNumber: String, departureStationName: String, arrivalStationName: String, departureDate: String, departureTime: String, arrivalTime: String, seatTypeName: String, seatPrice: Double, passengers: List<Passenger>, password: String) {
        _state.value = _state.value.copy(processing = true, error = null)
        viewModelScope.launch {
            if (!userRepo.verifyPaymentPassword(userPhone, password)) { _state.value = _state.value.copy(processing = false, error = "支付密码错误"); return@launch }
            val items = passengers.map { OrderItem(orderId = 0, passengerName = it.name, passengerIdCard = it.idCard, seatType = seatTypeName, price = seatPrice) }
            val orderId = orderRepo.createOrder(userPhone, trainId, trainNumber, departureStationName, arrivalStationName, departureDate, departureTime, arrivalTime, seatPrice * passengers.size, items)
            _state.value = _state.value.copy(processing = false, success = true, orderId = orderId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    userPhone: String, trainId: Long, trainNumber: String,
    departureStationName: String, arrivalStationName: String,
    departureDate: String, departureTime: String, arrivalTime: String,
    seatTypeName: String, seatPrice: Double, passengers: List<Passenger>,
    onBack: () -> Unit,
    viewModel: PaymentViewModel = viewModel(),
    onSuccess: (Long) -> Unit
) {
    var digits by remember { mutableStateOf(List(6) { "" }) }
    var inactivityResetKey by remember { mutableStateOf(0) }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val totalPrice = seatPrice * passengers.size

    LaunchedEffect(state.success) {
        if (state.success) {
            Toast.makeText(context, "支付成功!", Toast.LENGTH_SHORT).show()
            state.orderId?.let(onSuccess)
        }
    }

    LaunchedEffect(inactivityResetKey) {
        Log.d("TTBS_TIMEOUT", "计时启动 resetKey=$inactivityResetKey")
        delay(INACTIVITY_TIMEOUT_MS)
        if (!state.success) {
            Log.d("TTBS_TIMEOUT", "超时触发，自动取消")
            Toast.makeText(context, "支付超时，已自动取消，请重新支付", Toast.LENGTH_LONG).show()
            onBack()
        }
    }

    fun onDigitChange(index: Int, value: String) {
        if (value.length <= 1 && value.all { it.isDigit() }) {
            digits = digits.toMutableList().also { it[index] = value }
        }
    }

    val password = digits.joinToString("")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("确认支付", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A73E8), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(trainNumber, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("$departureStationName → $arrivalStationName", color = Color(0xFF5F6368))
            Text("$departureDate $departureTime-$arrivalTime", color = Color(0xFF5F6368), fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Text("$seatTypeName · ${passengers.size}人", color = Color(0xFF5F6368))

            Spacer(Modifier.height(32.dp))
            Text("应付款", color = Color(0xFF5F6368))
            Text("¥${String.format("%.0f", totalPrice)}", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
            Spacer(Modifier.height(24.dp))

            Icon(Icons.Default.Lock, null, tint = Color(0xFF5F6368))
            Text("请输入6位支付密码", color = Color(0xFF5F6368), fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                digits.forEachIndexed { i, d ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.size(48.dp).padding(4.dp)
                            .border(1.dp, if (d.isNotEmpty()) Color(0xFF1A73E8) else Color(0xFFDADCE0), RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(d, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Hidden input
            androidx.compose.foundation.layout.Box(modifier = Modifier.height(0.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = password,
                    onValueChange = { v ->
                        if (v.length <= 6 && v.all { it.isDigit() }) {
                            digits = List(6) { if (it < v.length) v[it].toString() else "" }
                            inactivityResetKey++
                            Log.d("TTBS_TIMEOUT", "数字输入重置")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
            }

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color(0xFFEA4335))
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { viewModel.pay(userPhone, trainId, trainNumber, departureStationName, arrivalStationName, departureDate, departureTime, arrivalTime, seatTypeName, seatPrice, passengers, password) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = password.length == 6 && !state.processing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335))
            ) {
                if (state.processing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("¥${String.format("%.0f", totalPrice)} 确认支付", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
