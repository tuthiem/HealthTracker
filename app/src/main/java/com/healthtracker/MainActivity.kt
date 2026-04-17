package com.healthtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.healthtracker.ui.HealthViewModel
import com.healthtracker.ui.navigation.Screen
import com.healthtracker.ui.navigation.bottomNavItems
import com.healthtracker.ui.screens.DashboardScreen
import com.healthtracker.ui.screens.DietScreen
import com.healthtracker.ui.screens.LiftsScreen
import com.healthtracker.ui.screens.OnboardingScreen
import com.healthtracker.ui.screens.PredictionsScreen
import com.healthtracker.ui.theme.HealthTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthTrackerTheme {
                HealthTrackerApp()
            }
        }
    }
}

@Composable
fun HealthTrackerApp(viewModel: HealthViewModel = viewModel()) {
    val navController = rememberNavController()
    val profile by viewModel.profile.collectAsState()
    val dietSummary by viewModel.todayDietSummary.collectAsState()
    val avgCalories by viewModel.avgDailyCalories.collectAsState()
    val avgProtein by viewModel.avgDailyProtein.collectAsState()

    val startDestination = if (profile == null) Screen.Onboarding.route else Screen.Dashboard.route

    Scaffold(
        bottomBar = {
            if (profile != null) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = { name, weight, goalWeight, heightInches, age, activityLevel ->
                        viewModel.createProfile(name, weight, goalWeight, heightInches, age, activityLevel)
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    profile = profile,
                    weightEntries = viewModel.recentWeightEntries,
                    todayCalories = dietSummary?.totalCalories ?: 0,
                    todayProtein = dietSummary?.totalProtein ?: 0.0,
                    onAddWeight = { viewModel.addWeight(it) }
                )
            }

            composable(Screen.Diet.route) {
                val tdee = profile?.let { viewModel.repository.calculateTDEE(it) } ?: 2000.0
                val weightKg = (profile?.currentWeight ?: 150.0) * 0.453592
                val proteinTarget = weightKg * 1.6

                DietScreen(
                    todayEntries = viewModel.todayDietEntries,
                    todayCalories = dietSummary?.totalCalories ?: 0,
                    todayProtein = dietSummary?.totalProtein ?: 0.0,
                    todayCarbs = dietSummary?.totalCarbs ?: 0.0,
                    todayFat = dietSummary?.totalFat ?: 0.0,
                    calorieTarget = tdee.toInt(),
                    proteinTarget = proteinTarget,
                    onAddEntry = { viewModel.addDietEntry(it) },
                    onDeleteEntry = { viewModel.deleteDietEntry(it) }
                )
            }

            composable(Screen.Lifts.route) {
                LiftsScreen(
                    liftEntries = viewModel.allLiftEntries,
                    personalRecords = viewModel.personalRecords,
                    trackedExercises = viewModel.trackedExercises,
                    onAddLift = { viewModel.addLiftEntry(it) },
                    onDeleteLift = { viewModel.deleteLiftEntry(it) }
                )
            }

            composable(Screen.Predictions.route) {
                PredictionsScreen(
                    profile = profile,
                    repository = viewModel.repository,
                    avgDailyCalories = avgCalories,
                    avgDailyProtein = avgProtein
                )
            }
        }
    }
}
