package com.example.train_ticket_booking_system.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    var userPhone by remember { mutableStateOf("") }
    var navigateToHome by remember { mutableStateOf(false) }

    LaunchedEffect(navigateToHome) {
        if (navigateToHome) {
            navController.navigate(Routes.HOME)
        }
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = { phone ->
                userPhone = phone
                navigateToHome = true
            })
        }
        composable(Routes.HOME) {
            MainScreen(userPhone, repos) { navController.navigate(it) }
        }
        composable(Routes.ORDERS) {
            OrderListScreen(repos, userPhone, navController)
        }
        composable(Routes.PROFILE) {
            ProfileScreen(repos, navController)
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
