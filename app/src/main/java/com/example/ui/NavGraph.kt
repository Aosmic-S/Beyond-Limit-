package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AIAssistantScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FocusModeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.SettingsScreen

@Composable
fun UltraInstinctApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val userName by viewModel.userName.collectAsStateWithLifecycle()

    val startDest = if (userName == null) "onboarding" else "dashboard"

    Scaffold(
        bottomBar = {
            if (currentRoute != "focus" && currentRoute != "onboarding" && currentRoute != "settings") {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = { navController.navigate("dashboard") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "planner",
                        onClick = { navController.navigate("planner") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.List, contentDescription = "Planner") },
                        label = { Text("Planner") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "assistant",
                        onClick = { navController.navigate("assistant") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Coach") },
                        label = { Text("AI Coach") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "library",
                        onClick = { navController.navigate("library") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.LibraryBooks, contentDescription = "Library") },
                        label = { Text("Library") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 8 } },
            exitTransition = { fadeOut(tween(300)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 8 } }
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    onNameSubmitted = { name ->
                        viewModel.saveUserName(name)
                        navController.navigate("dashboard") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToFocus = { navController.navigate("focus") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("planner") {
                PlannerScreen(viewModel = viewModel)
            }
            composable("library") {
                LibraryScreen(viewModel = viewModel)
            }
            composable("focus") {
                FocusModeScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("assistant") {
                AIAssistantScreen(viewModel)
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
