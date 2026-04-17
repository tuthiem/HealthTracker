package com.healthtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val startingWeight: Double,
    val currentWeight: Double,
    val goalWeight: Double,
    val heightInches: Int,
    val age: Int,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ActivityLevel(val label: String, val multiplier: Double) {
    SEDENTARY("Sedentary", 1.2),
    LIGHT("Lightly Active", 1.375),
    MODERATE("Moderately Active", 1.55),
    ACTIVE("Very Active", 1.725),
    EXTRA_ACTIVE("Extra Active", 1.9)
}

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weight: Double,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "diet_entries")
data class DietEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val calories: Int,
    val proteinGrams: Double = 0.0,
    val carbsGrams: Double = 0.0,
    val fatGrams: Double = 0.0,
    val mealType: MealType = MealType.OTHER,
    val date: Long = System.currentTimeMillis()
)

enum class MealType(val label: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack"),
    OTHER("Other")
}

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: ExerciseCategory = ExerciseCategory.OTHER
)

enum class ExerciseCategory(val label: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    ARMS("Arms"),
    LEGS("Legs"),
    CORE("Core"),
    CARDIO("Cardio"),
    OTHER("Other")
}

@Entity(tableName = "lift_entries")
data class LiftEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val exerciseName: String,
    val weight: Double,
    val reps: Int,
    val sets: Int,
    val isPersonalRecord: Boolean = false,
    val date: Long = System.currentTimeMillis()
)

data class PersonalRecord(
    val exerciseName: String,
    val maxWeight: Double,
    val reps: Int,
    val date: Long
)

data class DailySummary(
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double
)
