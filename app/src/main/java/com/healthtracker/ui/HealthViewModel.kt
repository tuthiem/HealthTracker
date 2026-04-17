package com.healthtracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthtracker.data.database.AppDatabase
import com.healthtracker.data.model.ActivityLevel
import com.healthtracker.data.model.DietEntry
import com.healthtracker.data.model.LiftEntry
import com.healthtracker.data.model.UserProfile
import com.healthtracker.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val repository = HealthRepository(db)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val profile = repository.getProfile().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val weightEntries = repository.getWeightEntries()
    val recentWeightEntries = repository.getRecentWeightEntries()

    val todayDietEntries = repository.getTodayDietEntries()
    val todayDietSummary = repository.getTodayDietSummary().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val allLiftEntries = repository.getAllLiftEntries()
    val personalRecords = repository.getAllPersonalRecords()
    val trackedExercises = repository.getTrackedExerciseNames()

    private val _avgDailyCalories = MutableStateFlow(0)
    val avgDailyCalories: StateFlow<Int> = _avgDailyCalories.asStateFlow()

    private val _avgDailyProtein = MutableStateFlow(0.0)
    val avgDailyProtein: StateFlow<Double> = _avgDailyProtein.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = false
        }
        viewModelScope.launch {
            repository.getRecentDietEntries(100).collect { entries ->
                if (entries.isNotEmpty()) {
                    val dayGroups = entries.groupBy { entry ->
                        val cal = java.util.Calendar.getInstance()
                        cal.timeInMillis = entry.date
                        "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.DAY_OF_YEAR)}"
                    }
                    val totalCals = entries.sumOf { it.calories }
                    val totalProtein = entries.sumOf { it.proteinGrams }
                    val days = dayGroups.size.coerceAtLeast(1)
                    _avgDailyCalories.value = totalCals / days
                    _avgDailyProtein.value = totalProtein / days
                }
            }
        }
    }

    fun createProfile(
        name: String,
        weight: Double,
        goalWeight: Double,
        heightInches: Int,
        age: Int,
        activityLevel: ActivityLevel
    ) {
        viewModelScope.launch {
            repository.saveProfile(
                UserProfile(
                    name = name,
                    startingWeight = weight,
                    currentWeight = weight,
                    goalWeight = goalWeight,
                    heightInches = heightInches,
                    age = age,
                    activityLevel = activityLevel
                )
            )
        }
    }

    fun addWeight(weight: Double) {
        viewModelScope.launch {
            repository.addWeightEntry(weight)
        }
    }

    fun addDietEntry(entry: DietEntry) {
        viewModelScope.launch {
            repository.addDietEntry(entry)
        }
    }

    fun deleteDietEntry(entry: DietEntry) {
        viewModelScope.launch {
            repository.deleteDietEntry(entry)
        }
    }

    fun addLiftEntry(entry: LiftEntry) {
        viewModelScope.launch {
            repository.addLiftEntry(entry)
        }
    }

    fun deleteLiftEntry(entry: LiftEntry) {
        viewModelScope.launch {
            repository.deleteLiftEntry(entry)
        }
    }
}
