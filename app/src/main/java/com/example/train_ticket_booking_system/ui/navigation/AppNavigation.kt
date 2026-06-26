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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import com.example.train_ticket_booking_system.data.repository.PassengerRepository
import com.example.train_ticket_booking_system.data.repository.StationRepository
import com.example.train_ticket_booking_system.data.repository.TrainRepository
import com.example.train_ticket_booking_system.data.repository.UserRepository
import com.example.train_ticket_booking_system.ui.ai.AIChatScreen
import com.example.train_ticket_booking_system.ui.auth.LoginScreen
import com.example.train_ticket_booking_system.ui.home.HomeScreen
import com.example.train_ticket_booking_system.ui.order.OrderListScreen
import com.example.train_ticket_booking_system.ui.profile.ProfileScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val ORDERS = "orders"
    const val PROFILE = "profile"
    const val TRAIN_LIST = "train_list/{fromId}/{toId}/{date}"
    const val ORDER_DETAIL = "order_detail/{orderId}"
}

data class Repos(
    val userRepo: UserRepository,
    val stationRepo: StationRepository,
    val trainRepo: TrainRepository,
    val passengerRepo: PassengerRepository,
    val orderRepo: OrderRepository
)

@Composable
fun AppNavigation(
    repos: Repos,
    startDestination: String = Routes.LOGIN
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = { phone ->
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            MainScreen(navController = navController, repos = repos)
        }
        composable(Routes.ORDERS) {
            OrderListScreen(repos = repos, navController = navController)
        }
        composable(Routes.PROFILE) {
            ProfileScreen(repos = repos, navController = navController)
        }
    }
}

@Composable
fun MainScreen(
    navController: NavHostController,
    repos: Repos
) {
    val bottomNavItems = listOf(
        BottomNavItem("首页", Icons.Default.Home, Routes.HOME),
        BottomNavItem("订单", Icons.Default.Menu, Routes.ORDERS),
        BottomNavItem("我的", Icons.Default.Person, Routes.PROFILE)
    )

    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                innerNavController.navigate(item.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = innerNavController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(repos = repos, onNavigate = { route -> navController.navigate(route) })
            }
            composable(Routes.ORDERS) {
                OrderListScreen(repos = repos, navController = navController)
            }
            composable(Routes.PROFILE) {
                ProfileScreen(repos = repos, navController = navController)
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)
