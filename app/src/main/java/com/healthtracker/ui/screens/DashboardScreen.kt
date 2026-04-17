package com.healthtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthtracker.data.model.UserProfile
import com.healthtracker.data.model.WeightEntry
import com.healthtracker.ui.components.NumberTextField
import com.healthtracker.ui.components.ProgressCard
import com.healthtracker.ui.components.StatCard
import com.healthtracker.util.DateUtils
import kotlinx.coroutines.flow.Flow

@Composable
fun DashboardScreen(
    profile: UserProfile?,
    weightEntries: Flow<List<WeightEntry>>,
    todayCalories: Int,
    todayProtein: Double,
    onAddWeight: (Double) -> Unit
) {
    val entries by weightEntries.collectAsState(initial = emptyList())
    var showAddWeight by remember { mutableStateOf(false) }
    var newWeight by remember { mutableStateOf("") }

    if (profile == null) return

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddWeight = true }) {
                Icon(Icons.Default.Add, contentDescription = "Log Weight")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Hey, ${profile.name}!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Here's your health snapshot",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Current Weight",
                        value = "${profile.currentWeight} lbs",
                        modifier = Modifier.weight(1f),
                        subtitle = "Goal: ${profile.goalWeight} lbs"
                    )
                    StatCard(
                        label = "Progress",
                        value = "${String.format("%.1f", profile.startingWeight - profile.currentWeight)} lbs",
                        modifier = Modifier.weight(1f),
                        subtitle = "lost so far"
                    )
                }
            }

            item {
                val weightToLose = (profile.startingWeight - profile.goalWeight).toFloat()
                val weightLost = (profile.startingWeight - profile.currentWeight).toFloat()
                ProgressCard(
                    label = "Weight Goal Progress",
                    current = weightLost,
                    target = weightToLose,
                    unit = "lbs lost",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Today's Calories",
                        value = "$todayCalories",
                        modifier = Modifier.weight(1f),
                        subtitle = "kcal"
                    )
                    StatCard(
                        label = "Today's Protein",
                        value = "${todayProtein.toInt()}g",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (entries.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Weight Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(entries.take(10)) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${entry.weight} lbs",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = DateUtils.formatDate(entry.date),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddWeight) {
        AlertDialog(
            onDismissRequest = { showAddWeight = false },
            title = { Text("Log Weight") },
            text = {
                NumberTextField(
                    value = newWeight,
                    onValueChange = { newWeight = it },
                    label = "Weight (lbs)"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    newWeight.toDoubleOrNull()?.let {
                        onAddWeight(it)
                        newWeight = ""
                        showAddWeight = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddWeight = false }) { Text("Cancel") }
            }
        )
    }
}
