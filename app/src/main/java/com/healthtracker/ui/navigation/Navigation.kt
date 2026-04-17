package com.healthtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    data object Diet : Screen("diet", "Diet", Icons.Default.Restaurant)
    data object Lifts : Screen("lifts", "Lifts", Icons.Default.FitnessCenter)
    data object Predictions : Screen("predictions", "Insights", Icons.Default.TrendingUp)
    data object Onboarding : Screen("onboarding", "Setup", Icons.Default.Home)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Diet,
    Screen.Lifts,
    Screen.Predictions
)
