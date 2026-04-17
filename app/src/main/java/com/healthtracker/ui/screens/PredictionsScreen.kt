package com.healthtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthtracker.data.model.UserProfile
import com.healthtracker.data.repository.HealthRepository
import com.healthtracker.ui.components.StatCard

@Composable
fun PredictionsScreen(
    profile: UserProfile?,
    repository: HealthRepository,
    avgDailyCalories: Int,
    avgDailyProtein: Double
) {
    if (profile == null) return

    val bmr = repository.calculateBMR(profile)
    val tdee = repository.calculateTDEE(profile)
    val weeklyDeficit = repository.calculateWeeklyDeficit(profile, avgDailyCalories)

    val prediction4Weeks = repository.predictWeightInWeeks(profile.currentWeight, weeklyDeficit, 4)
    val prediction8Weeks = repository.predictWeightInWeeks(profile.currentWeight, weeklyDeficit, 8)
    val prediction12Weeks = repository.predictWeightInWeeks(profile.currentWeight, weeklyDeficit, 12)

    val tips = repository.generateDietTips(profile, avgDailyCalories, avgDailyProtein)

    val isLosing = weeklyDeficit > 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Insights & Predictions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isLosing) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(
                            text = if (isLosing) "You're on Track!" else "Calorie Surplus Detected",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val weeklyLbs = weeklyDeficit / 3500.0
                    Text(
                        text = if (isLosing)
                            "At your current pace, you're projected to lose about ${String.format("%.1f", weeklyLbs)} lbs per week."
                        else
                            "You're eating above your TDEE. Consider adjusting your intake to start seeing weight loss.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        item {
            Text(
                text = "Your Numbers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "BMR",
                    value = "${bmr.toInt()}",
                    modifier = Modifier.weight(1f),
                    subtitle = "cal/day at rest"
                )
                StatCard(
                    label = "TDEE",
                    value = "${tdee.toInt()}",
                    modifier = Modifier.weight(1f),
                    subtitle = "cal/day total"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Avg Daily Intake",
                    value = "$avgDailyCalories",
                    modifier = Modifier.weight(1f),
                    subtitle = "cal/day"
                )
                StatCard(
                    label = "Daily Deficit",
                    value = "${(tdee - avgDailyCalories).toInt()}",
                    modifier = Modifier.weight(1f),
                    subtitle = "cal/day"
                )
            }
        }

        item {
            Text(
                text = "Weight Predictions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PredictionCard(
                    weeks = 4,
                    predictedWeight = prediction4Weeks,
                    currentWeight = profile.currentWeight,
                    modifier = Modifier.weight(1f)
                )
                PredictionCard(
                    weeks = 8,
                    predictedWeight = prediction8Weeks,
                    currentWeight = profile.currentWeight,
                    modifier = Modifier.weight(1f)
                )
                PredictionCard(
                    weeks = 12,
                    predictedWeight = prediction12Weeks,
                    currentWeight = profile.currentWeight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tips & Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(tips) { tip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = tip,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PredictionCard(
    weeks: Int,
    predictedWeight: Double,
    currentWeight: Double,
    modifier: Modifier = Modifier
) {
    val diff = currentWeight - predictedWeight
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${weeks}wk",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "${String.format("%.1f", predictedWeight)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "lbs",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = if (diff >= 0) "-${String.format("%.1f", diff)}" else "+${String.format("%.1f", -diff)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
