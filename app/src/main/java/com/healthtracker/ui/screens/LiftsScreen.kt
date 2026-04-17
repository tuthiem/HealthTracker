package com.healthtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthtracker.data.database.PRResult
import com.healthtracker.data.model.ExerciseCategory
import com.healthtracker.data.model.LiftEntry
import com.healthtracker.ui.components.NumberTextField
import com.healthtracker.ui.components.PRBadge
import com.healthtracker.util.DateUtils
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LiftsScreen(
    liftEntries: Flow<List<LiftEntry>>,
    personalRecords: Flow<List<PRResult>>,
    trackedExercises: Flow<List<String>>,
    onAddLift: (LiftEntry) -> Unit,
    onDeleteLift: (LiftEntry) -> Unit
) {
    val entries by liftEntries.collectAsState(initial = emptyList())
    val prs by personalRecords.collectAsState(initial = emptyList())
    val exercises by trackedExercises.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var filterExercise by remember { mutableStateOf<String?>(null) }

    val filteredEntries = if (filterExercise != null) {
        entries.filter { it.exerciseName == filterExercise }
    } else {
        entries
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Log Lift")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Lift Tracker",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (prs.isNotEmpty()) {
                item {
                    Text(
                        text = "Personal Records",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        prs.forEach { pr ->
                            PRBadge(exerciseName = pr.exerciseName, weight = pr.maxWeight)
                        }
                    }
                }
            }

            if (exercises.isNotEmpty()) {
                item {
                    Text(
                        text = "Filter by Exercise",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = filterExercise == null,
                            onClick = { filterExercise = null },
                            label = { Text("All") }
                        )
                        exercises.forEach { name ->
                            FilterChip(
                                selected = filterExercise == name,
                                onClick = {
                                    filterExercise = if (filterExercise == name) null else name
                                },
                                label = { Text(name) }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Workout Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (filteredEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "No lifts logged yet. Hit the + button to start tracking!",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(filteredEntries) { entry ->
                LiftEntryCard(entry = entry, onDelete = { onDeleteLift(entry) })
            }
        }
    }

    if (showAddDialog) {
        AddLiftDialog(
            existingExercises = exercises,
            onDismiss = { showAddDialog = false },
            onAdd = { entry ->
                onAddLift(entry)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun LiftEntryCard(entry: LiftEntry, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isPersonalRecord)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.exerciseName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (entry.isPersonalRecord) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = "PR",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.height(18.dp)
                        )
                    }
                }
                Text(
                    text = "${entry.weight.toInt()} lbs x ${entry.reps} reps x ${entry.sets} sets",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = DateUtils.formatDate(entry.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLiftDialog(
    existingExercises: List<String>,
    onDismiss: () -> Unit,
    onAdd: (LiftEntry) -> Unit
) {
    var exerciseName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var exerciseExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(ExerciseCategory.OTHER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Lift") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (existingExercises.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = exerciseExpanded,
                        onExpandedChange = { exerciseExpanded = !exerciseExpanded }
                    ) {
                        OutlinedTextField(
                            value = exerciseName,
                            onValueChange = { exerciseName = it },
                            label = { Text("Exercise Name") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exerciseExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = exerciseExpanded,
                            onDismissRequest = { exerciseExpanded = false }
                        ) {
                            existingExercises.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        exerciseName = name
                                        exerciseExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        label = { Text("Exercise Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                NumberTextField(value = weight, onValueChange = { weight = it }, label = "Weight (lbs)")
                NumberTextField(value = reps, onValueChange = { reps = it }, label = "Reps", allowDecimal = false)
                NumberTextField(value = sets, onValueChange = { sets = it }, label = "Sets", allowDecimal = false)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val w = weight.toDoubleOrNull() ?: return@TextButton
                    val r = reps.toIntOrNull() ?: return@TextButton
                    val s = sets.toIntOrNull() ?: return@TextButton
                    onAdd(
                        LiftEntry(
                            exerciseId = 0,
                            exerciseName = exerciseName.ifBlank { "Unknown" },
                            weight = w,
                            reps = r,
                            sets = s
                        )
                    )
                },
                enabled = exerciseName.isNotBlank() && weight.isNotBlank() &&
                        reps.isNotBlank() && sets.isNotBlank()
            ) { Text("Log It") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
