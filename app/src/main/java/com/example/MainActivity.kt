package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.executor.ExecutorEngine
import com.example.core.voice.VoiceEngine
import com.example.data.local.BoloDatabase
import com.example.data.repository.BoloRepository
import com.example.features.dashboard.BoloViewModel
import com.example.features.dashboard.BoloViewModelFactory
import com.example.features.dashboard.DashboardScreen
import com.example.features.dashboard.VoiceOverlayComponent
import com.example.features.help.HelpScreen
import com.example.features.routines.RoutinesScreen
import com.example.features.settings.PrivacyScreen
import com.example.ui.theme.ActiveMint
import com.example.ui.theme.DarkSteel
import com.example.ui.theme.DeepCoal
import com.example.ui.theme.MidnightSlate
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.SunsetAmber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize DB, Repos, and Engines
        val database = BoloDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = BoloRepository(database.boloDao())
        val voiceEngine = VoiceEngine(applicationContext)
        val executorEngine = ExecutorEngine(applicationContext)

        // 2. Initialize ViewModel via Factory
        val viewModelFactory = BoloViewModelFactory(
            application = this.application,
            repository = repository,
            voiceEngine = voiceEngine,
            executorEngine = executorEngine
        )
        val viewModel = ViewModelProvider(this, viewModelFactory)[BoloViewModel::class.java]

        setContent {
            MyApplicationTheme {
                BoloAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BoloAppScaffold(viewModel: BoloViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MidnightSlate,
        bottomBar = {
            NavigationBar(
                containerColor = DeepCoal,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                // Item 1: Dashboard
                NavigationBarItem(
                    selected = currentRoute == "dashboard",
                    onClick = {
                        navController.navigate("dashboard") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Mic, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MidnightSlate,
                        selectedTextColor = SunsetAmber,
                        indicatorColor = SunsetAmber,
                        unselectedIconColor = SoftCyan.copy(alpha = 0.6f),
                        unselectedTextColor = SoftCyan.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )

                // Item 2: Routines
                NavigationBarItem(
                    selected = currentRoute == "routines",
                    onClick = {
                        navController.navigate("routines") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.FlashOn, contentDescription = "Routines") },
                    label = { Text("Routines", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MidnightSlate,
                        selectedTextColor = SunsetAmber,
                        indicatorColor = SunsetAmber,
                        unselectedIconColor = SoftCyan.copy(alpha = 0.6f),
                        unselectedTextColor = SoftCyan.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_routines")
                )

                // Item 3: Privacy
                NavigationBarItem(
                    selected = currentRoute == "privacy",
                    onClick = {
                        navController.navigate("privacy") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Security, contentDescription = "Privacy") },
                    label = { Text("Privacy", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MidnightSlate,
                        selectedTextColor = SunsetAmber,
                        indicatorColor = SunsetAmber,
                        unselectedIconColor = SoftCyan.copy(alpha = 0.6f),
                        unselectedTextColor = SoftCyan.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_privacy")
                )

                // Item 4: Guide (Help)
                NavigationBarItem(
                    selected = currentRoute == "help",
                    onClick = {
                        navController.navigate("help") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.HelpOutline, contentDescription = "Guide") },
                    label = { Text("Guide", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MidnightSlate,
                        selectedTextColor = SunsetAmber,
                        indicatorColor = SunsetAmber,
                        unselectedIconColor = SoftCyan.copy(alpha = 0.6f),
                        unselectedTextColor = SoftCyan.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_guide")
                )
            }
        }
    ) { innerPadding ->
        // NavHost routing
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRoutines = { navController.navigate("routines") },
                    onNavigateToPrivacy = { navController.navigate("privacy") }
                )
            }
            composable("routines") {
                RoutinesScreen(viewModel = viewModel)
            }
            composable("privacy") {
                PrivacyScreen(viewModel = viewModel)
            }
            composable("help") {
                HelpScreen(
                    viewModel = viewModel,
                    onNavigateToDashboard = { navController.navigate("dashboard") }
                )
            }
        }

        // 3. Mount Global voice trigger overlay
        VoiceOverlayComponent(viewModel = viewModel)
    }
}
