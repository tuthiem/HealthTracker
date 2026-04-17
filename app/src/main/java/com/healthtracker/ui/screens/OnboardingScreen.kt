package com.healthtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.healthtracker.data.model.ActivityLevel
import com.healthtracker.ui.components.NumberTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (
        name: String,
        weight: Double,
        goalWeight: Double,
        heightInches: Int,
        age: Int,
        activityLevel: ActivityLevel
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var goalWeight by remember { mutableStateOf("") }
    var heightFeet by remember { mutableStateOf("") }
    var heightInches by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedActivity by remember { mutableStateOf(ActivityLevel.MODERATE) }
    var activityExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Welcome to HealthTracker",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Let's set up your profile to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        NumberTextField(
            value = weight,
            onValueChange = { weight = it },
            label = "Current Weight (lbs)"
        )

        Spacer(modifier = Modifier.height(16.dp))

        NumberTextField(
            value = goalWeight,
            onValueChange = { goalWeight = it },
            label = "Goal Weight (lbs)"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberTextField(
                value = heightFeet,
                onValueChange = { heightFeet = it },
                label = "Height (ft)",
                modifier = Modifier.weight(1f),
                allowDecimal = false
            )
            NumberTextField(
                value = heightInches,
                onValueChange = { heightInches = it },
                label = "Height (in)",
                modifier = Modifier.weight(1f),
                allowDecimal = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        NumberTextField(
            value = age,
            onValueChange = { age = it },
            label = "Age",
            allowDecimal = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = activityExpanded,
            onExpandedChange = { activityExpanded = !activityExpanded }
        ) {
            OutlinedTextField(
                value = selectedActivity.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Activity Level") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = activityExpanded,
                onDismissRequest = { activityExpanded = false }
            ) {
                ActivityLevel.entries.forEach { level ->
                    DropdownMenuItem(
                        text = { Text(level.label) },
                        onClick = {
                            selectedActivity = level
                            activityExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val w = weight.toDoubleOrNull() ?: return@Button
                val gw = goalWeight.toDoubleOrNull() ?: return@Button
                val hf = heightFeet.toIntOrNull() ?: return@Button
                val hi = heightInches.toIntOrNull() ?: 0
                val a = age.toIntOrNull() ?: return@Button
                val totalInches = hf * 12 + hi
                onComplete(name, w, gw, totalInches, a, selectedActivity)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = name.isNotBlank() && weight.isNotBlank() &&
                    goalWeight.isNotBlank() && heightFeet.isNotBlank() && age.isNotBlank()
        ) {
            Text("Get Started", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
