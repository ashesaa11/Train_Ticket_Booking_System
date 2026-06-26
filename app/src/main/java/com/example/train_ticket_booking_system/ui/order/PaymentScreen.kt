package com.example.train_ticket_booking_system.ui.order

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.train_ticket_booking_system.TTBSApplication
import com.example.train_ticket_booking_system.data.entity.OrderItem
import com.example.train_ticket_booking_system.data.entity.Passenger
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import com.example.train_ticket_booking_system.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PaymentState(
    val processing: Boolean = false,
    val success: Boolean = false,
    val orderId: Long? = null,
    val error: String? = null
)

class PaymentViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as TTBSApplication).database
    private val userRepo = UserRepository(db.userDao())
    private val orderRepo = OrderRepository(db.orderDao(), db.seatTypeDao())

    private val _state = MutableStateFlow(PaymentState())
    val state: StateFlow<PaymentState> = _state

    fun pay(
        userPhone: String,
        trainId: Long,
        trainNumber: String,
        departureStationName: String,
        arrivalStationName: String,
        departureDate: String,
        departureTime: String,
        arrivalTime: String,
        seatTypeName: String,
        seatPrice: Double,
        passengers: List<Passenger>,
        password: String
    ) {
        _state.value = _state.value.copy(processing = true, error = null)
        viewModelScope.launch {
            if (!userRepo.verifyPassword(userPhone, password)) {
                _state.value = _state.value.copy(processing = false, error = "支付密码错误")
                return@launch
            }
            val items = passengers.map { p ->
                OrderItem(
                    orderId = 0,
                    passengerName = p.name,
                    passengerIdCard = p.idCard,
                    seatType = seatTypeName,
                    price = seatPrice
                )
            }
            val totalPrice = seatPrice * passengers.size
            val orderId = orderRepo.createOrder(
                userPhone = userPhone,
                trainId = trainId,
                trainNumber = trainNumber,
                departureStationName = departureStationName,
                arrivalStationName = arrivalStationName,
                departureDate = departureDate,
                departureTime = departureTime,
                arrivalTime = arrivalTime,
                totalPrice = totalPrice,
                items = items
            )
            _state.value = _state.value.copy(processing = false, success = true, orderId = orderId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    userPhone: String,
    trainId: Long,
    trainNumber: String,
    departureStationName: String,
    arrivalStationName: String,
    departureDate: String,
    departureTime: String,
    arrivalTime: String,
    seatTypeName: String,
    seatPrice: Double,
    passengers: List<Passenger>,
    onBack: () -> Unit,
    viewModel: PaymentViewModel = viewModel(),
    onSuccess: (Long) -> Unit
) {
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    if (state.success) {
        LaunchedEffect(state.orderId) {
            Toast.makeText(context, "支付成功!", Toast.LENGTH_SHORT).show()
            state.orderId?.let { onSuccess(it) }
        }
    }

    val totalPrice = seatPrice * passengers.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("支付") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.height(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("应付款", style = MaterialTheme.typography.titleMedium)
            Text(
                "¥${String.format("%.1f", totalPrice)}",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it.take(6) },
                label = { Text("请输入6位支付密码") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            state.error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.pay(
                        userPhone, trainId, trainNumber,
                        departureStationName, arrivalStationName,
                        departureDate, departureTime, arrivalTime,
                        seatTypeName, seatPrice, passengers, password
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = password.length == 6 && !state.processing
            ) {
                Text(if (state.processing) "支付中..." else "确认支付")
            }
        }
    }
}
