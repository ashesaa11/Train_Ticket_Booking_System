package com.example.train_ticket_booking_system.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.util.Log
import com.example.train_ticket_booking_system.data.entity.Passenger
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import com.example.train_ticket_booking_system.data.repository.PassengerRepository
import com.example.train_ticket_booking_system.data.repository.StationRepository
import com.example.train_ticket_booking_system.data.repository.TrainRepository
import com.example.train_ticket_booking_system.data.repository.UserRepository
import com.example.train_ticket_booking_system.ui.ai.AIChatScreen
import com.example.train_ticket_booking_system.ui.auth.LoginScreen
import com.example.train_ticket_booking_system.ui.home.HomeScreen
import com.example.train_ticket_booking_system.ui.order.OrderConfirmScreen
import com.example.train_ticket_booking_system.ui.order.OrderDetailScreen
import com.example.train_ticket_booking_system.ui.order.OrderListScreen
import com.example.train_ticket_booking_system.ui.order.PaymentScreen
import com.example.train_ticket_booking_system.ui.profile.DataManageScreen
import com.example.train_ticket_booking_system.ui.profile.PassengerManageScreen
import com.example.train_ticket_booking_system.ui.profile.ProfileScreen
import com.example.train_ticket_booking_system.ui.train.PassengerSelectScreen
import com.example.train_ticket_booking_system.ui.train.SeatSelectScreen
import com.example.train_ticket_booking_system.ui.train.TrainListScreen
import kotlinx.coroutines.launch

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val ORDERS = "orders"
    const val PROFILE = "profile"
    const val TRAIN_LIST = "train_list/{fromId}/{toId}/{date}"
    const val SEAT_SELECT = "seat_select/{trainId}/{trainNumber}/{depTime}/{arrTime}/{depStation}/{arrStation}/{date}"
    const val PASSENGER_SELECT = "passenger_select/{trainId}/{trainNumber}/{depTime}/{arrTime}/{depStation}/{arrStation}/{date}/{seatTypeId}/{seatTypeName}/{seatPrice}"
    const val ORDER_CONFIRM = "order_confirm/{trainId}/{trainNumber}/{depTime}/{arrTime}/{depStation}/{arrStation}/{date}/{seatTypeName}/{seatPrice}/{passengerIds}"
    const val PAYMENT = "payment/{trainId}/{trainNumber}/{depStation}/{arrStation}/{date}/{depTime}/{arrTime}/{seatTypeName}/{seatPrice}/{passengerIds}"
    const val ORDER_DETAIL = "order_detail/{orderId}"
    const val PASSENGER_MANAGE = "passenger_manage"
    const val DATA_MANAGE = "data_manage"
    const val AI_CHAT = "ai_chat"
}

data class Repos(
    val userRepo: UserRepository,
    val stationRepo: StationRepository,
    val trainRepo: TrainRepository,
    val passengerRepo: PassengerRepository,
    val orderRepo: OrderRepository
)

@Composable
fun AppNavigation(repos: Repos) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var userPhone by remember { mutableStateOf("") }
    var needsPaymentPwd by remember { mutableStateOf(false) }
    var paymentPwdInput by remember { mutableStateOf("") }
    var paymentPwdConfirm by remember { mutableStateOf("") }
    var pwdError by remember { mutableStateOf<String?>(null) }
    var isLoggedIn by remember { mutableStateOf(false) }

    fun doNavigateHome() {
        Log.d("TTBS_NAV", "doNavigateHome called, current dest: ${navController.currentDestination?.route}")
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.LOGIN) { inclusive = false }
            launchSingleTop = true
        }
        Log.d("TTBS_NAV", "doNavigateHome done, current dest: ${navController.currentDestination?.route}")
    }

    // Payment password setup dialog
    if (needsPaymentPwd) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("设置支付密码") },
            text = {
                Column {
                    Text("首次使用需设置6位数字支付密码，用于购票支付验证。")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        paymentPwdInput,
                        { v -> if (v.length <= 6 && v.all { it.isDigit() }) { paymentPwdInput = v; pwdError = null } },
                        label = { Text("支付密码（6位数字）") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        paymentPwdConfirm,
                        { v -> if (v.length <= 6 && v.all { it.isDigit() }) { paymentPwdConfirm = v; pwdError = null } },
                        label = { Text("确认支付密码") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    pwdError?.let { Spacer(Modifier.height(4.dp)); Text(it, color = Color(0xFFEA4335)) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when {
                        paymentPwdInput.length != 6 -> pwdError = "支付密码必须为6位数字"
                        paymentPwdInput != paymentPwdConfirm -> pwdError = "两次密码不一致"
                        else -> scope.launch {
                            repos.userRepo.setPaymentPassword(userPhone, paymentPwdInput)
                            needsPaymentPwd = false
                            doNavigateHome()
                        }
                    }
                }) { Text("确认") }
            },
            dismissButton = {}
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = { phone ->
                Log.d("TTBS_NAV", "onLoginSuccess called, phone=$phone")
                userPhone = phone
                isLoggedIn = true
                scope.launch {
                    val user = repos.userRepo.getByPhone(phone)
                    if (user != null && user.paymentPassword.isEmpty()) {
                        Log.d("TTBS_NAV", "new user, showing payment pwd dialog")
                        needsPaymentPwd = true
                    } else {
                        doNavigateHome()
                    }
                }
            })
        }
        composable(Routes.HOME) {
            MainScreen(userPhone, repos) { navController.navigate(it) }
        }
        composable(Routes.ORDERS) {
            OrderListScreen(repos, userPhone, navController)
        }
        composable(Routes.PROFILE) {
            ProfileScreen(userPhone, repos, navController)
        }

        // Buy flow
        composable(Routes.TRAIN_LIST,
            arguments = listOf(navArgument("fromId") { type = NavType.LongType }, navArgument("toId") { type = NavType.LongType }, navArgument("date") { type = NavType.StringType })
        ) { entry ->
            TrainListScreen(entry.arguments!!.getLong("fromId"), entry.arguments!!.getLong("toId"), entry.arguments!!.getString("date")!!,
                { navController.popBackStack() }
            ) { tid, tn, dt, at, ds, asName ->
                navController.navigate("seat_select/$tid/$tn/$dt/$at/$ds/$asName/${entry.arguments!!.getString("date")}")
            }
        }

        composable(Routes.SEAT_SELECT,
            arguments = listOf(navArgument("trainId") { type = NavType.LongType }, navArgument("trainNumber") { type = NavType.StringType }, navArgument("depTime") { type = NavType.StringType }, navArgument("arrTime") { type = NavType.StringType }, navArgument("depStation") { type = NavType.StringType }, navArgument("arrStation") { type = NavType.StringType }, navArgument("date") { type = NavType.StringType })
        ) { entry ->
            val a = entry.arguments!!
            SeatSelectScreen(a.getLong("trainId"), a.getString("trainNumber")!!, a.getString("date")!!, repos.orderRepo,
                { navController.popBackStack() }
            ) { stId, stName, stPrice ->
                navController.navigate("passenger_select/${a.getLong("trainId")}/${a.getString("trainNumber")}/${a.getString("depTime")}/${a.getString("arrTime")}/${a.getString("depStation")}/${a.getString("arrStation")}/${a.getString("date")}/$stId/$stName/$stPrice")
            }
        }

        composable(Routes.PASSENGER_SELECT,
            arguments = listOf(navArgument("trainId") { type = NavType.LongType }, navArgument("trainNumber") { type = NavType.StringType }, navArgument("depTime") { type = NavType.StringType }, navArgument("arrTime") { type = NavType.StringType }, navArgument("depStation") { type = NavType.StringType }, navArgument("arrStation") { type = NavType.StringType }, navArgument("date") { type = NavType.StringType }, navArgument("seatTypeId") { type = NavType.LongType }, navArgument("seatTypeName") { type = NavType.StringType }, navArgument("seatPrice") { type = NavType.FloatType })
        ) { entry ->
            val a = entry.arguments!!
            PassengerSelectScreen(userPhone, repos.passengerRepo, { navController.popBackStack() }) { passengers ->
                val ids = passengers.joinToString(",") { it.id.toString() }
                navController.navigate("order_confirm/${a.getLong("trainId")}/${a.getString("trainNumber")}/${a.getString("depTime")}/${a.getString("arrTime")}/${a.getString("depStation")}/${a.getString("arrStation")}/${a.getString("date")}/${a.getString("seatTypeName")}/${a.getFloat("seatPrice")}/$ids")
            }
        }

        composable(Routes.ORDER_CONFIRM,
            arguments = listOf(navArgument("trainId") { type = NavType.LongType }, navArgument("trainNumber") { type = NavType.StringType }, navArgument("depTime") { type = NavType.StringType }, navArgument("arrTime") { type = NavType.StringType }, navArgument("depStation") { type = NavType.StringType }, navArgument("arrStation") { type = NavType.StringType }, navArgument("date") { type = NavType.StringType }, navArgument("seatTypeName") { type = NavType.StringType }, navArgument("seatPrice") { type = NavType.FloatType }, navArgument("passengerIds") { type = NavType.StringType })
        ) { entry ->
            val a = entry.arguments!!
            var passengers by remember { mutableStateOf<List<Passenger>>(emptyList()) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(a.getString("passengerIds")) {
                val ids = a.getString("passengerIds")!!.split(",").map { it.toLong() }
                passengers = ids.mapNotNull { repos.passengerRepo.getById(it) }
            }
            OrderConfirmScreen(a.getString("trainNumber")!!, a.getString("depTime")!!, a.getString("arrTime")!!,
                "${a.getString("depStation")} → ${a.getString("arrStation")}",
                a.getString("date")!!, a.getString("seatTypeName")!!, a.getFloat("seatPrice").toDouble(), passengers,
                { navController.popBackStack() }
            ) {
                navController.navigate("payment/${a.getLong("trainId")}/${a.getString("trainNumber")}/${a.getString("depStation")}/${a.getString("arrStation")}/${a.getString("date")}/${a.getString("depTime")}/${a.getString("arrTime")}/${a.getString("seatTypeName")}/${a.getFloat("seatPrice")}/${a.getString("passengerIds")}")
            }
        }

        composable(Routes.PAYMENT,
            arguments = listOf(navArgument("trainId") { type = NavType.LongType }, navArgument("trainNumber") { type = NavType.StringType }, navArgument("depStation") { type = NavType.StringType }, navArgument("arrStation") { type = NavType.StringType }, navArgument("date") { type = NavType.StringType }, navArgument("depTime") { type = NavType.StringType }, navArgument("arrTime") { type = NavType.StringType }, navArgument("seatTypeName") { type = NavType.StringType }, navArgument("seatPrice") { type = NavType.FloatType }, navArgument("passengerIds") { type = NavType.StringType })
        ) { entry ->
            val a = entry.arguments!!
            var passengers by remember { mutableStateOf<List<Passenger>>(emptyList()) }
            LaunchedEffect(a.getString("passengerIds")) {
                val ids = a.getString("passengerIds")!!.split(",").map { it.toLong() }
                passengers = ids.mapNotNull { repos.passengerRepo.getById(it) }
            }
            PaymentScreen(userPhone, a.getLong("trainId"), a.getString("trainNumber")!!,
                a.getString("depStation")!!, a.getString("arrStation")!!, a.getString("date")!!,
                a.getString("depTime")!!, a.getString("arrTime")!!, a.getString("seatTypeName")!!,
                a.getFloat("seatPrice").toDouble(), passengers,
                { navController.popBackStack() }
            ) { orderId ->
                navController.navigate("order_detail/$orderId") { popUpTo(Routes.HOME) }
            }
        }

        composable(Routes.ORDER_DETAIL, arguments = listOf(navArgument("orderId") { type = NavType.LongType })) { entry ->
            OrderDetailScreen(entry.arguments!!.getLong("orderId"), repos.orderRepo,
                { navController.popBackStack() },
                { navController.popBackStack(Routes.HOME, false) }
            )
        }
        composable(Routes.PASSENGER_MANAGE) {
            PassengerManageScreen(userPhone, navController, repos.passengerRepo)
        }
        composable(Routes.DATA_MANAGE) {
            DataManageScreen(repos, navController)
        }
        composable(Routes.AI_CHAT) {
            AIChatScreen(userPhone, navController)
        }
    }

    // AI Draggable floating button - hides on login and AI chat page
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(-80f) }
    if (isLoggedIn && currentRoute != Routes.LOGIN && currentRoute != Routes.AI_CHAT) {
        FloatingActionButton(
            onClick = { navController.navigate(Routes.AI_CHAT) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = offsetX.dp, y = offsetY.dp)
                .padding(24.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x * 0.4f
                        offsetY += dragAmount.y * 0.4f
                    }
                },
            containerColor = Color(0xFF1A73E8)
        ) {
            Icon(Icons.Default.SmartToy, "AI助手", tint = Color.White)
        }
    }
    }
}

@Composable
fun MainScreen(userPhone: String, repos: Repos, onNavigate: (String) -> Unit) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    BottomNavItem("首页", Icons.Default.Home, Routes.HOME),
                    BottomNavItem("订单", Icons.Default.Menu, Routes.ORDERS),
                    BottomNavItem("我的", Icons.Default.Person, Routes.PROFILE)
                ).forEach { item ->
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            if (item.route != Routes.HOME) onNavigate(item.route)
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        HomeScreen(repos = repos, onNavigate = onNavigate)
    }
}

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)
