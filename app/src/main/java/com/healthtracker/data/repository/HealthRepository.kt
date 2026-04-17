package com.healthtracker.data.repository

import com.healthtracker.data.database.AppDatabase
import com.healthtracker.data.model.ActivityLevel
import com.healthtracker.data.model.DietEntry
import com.healthtracker.data.model.Exercise
import com.healthtracker.data.model.LiftEntry
import com.healthtracker.data.model.UserProfile
import com.healthtracker.data.model.WeightEntry
import com.healthtracker.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HealthRepository(private val db: AppDatabase) {

    // User Profile
    fun getProfile(): Flow<UserProfile?> = db.userProfileDao().getProfile()
    suspend fun getProfileOnce(): UserProfile? = db.userProfileDao().getProfileOnce()
    suspend fun saveProfile(profile: UserProfile) = db.userProfileDao().insertProfile(profile)
    suspend fun updateProfile(profile: UserProfile) = db.userProfileDao().updateProfile(profile)

    // Weight
    fun getWeightEntries(): Flow<List<WeightEntry>> = db.weightDao().getAllEntries()
    fun getWeightEntriesAsc(): Flow<List<WeightEntry>> = db.weightDao().getAllEntriesAsc()
    fun getRecentWeightEntries(limit: Int = 30): Flow<List<WeightEntry>> =
        db.weightDao().getRecentEntries(limit)

    suspend fun addWeightEntry(weight: Double) {
        db.weightDao().insert(WeightEntry(weight = weight))
        val profile = db.userProfileDao().getProfileOnce()
        if (profile != null) {
            db.userProfileDao().updateProfile(profile.copy(currentWeight = weight))
        }
    }

    suspend fun deleteWeightEntry(entry: WeightEntry) = db.weightDao().delete(entry)

    // Diet
    fun getTodayDietEntries(): Flow<List<DietEntry>> {
        val (start, end) = DateUtils.todayRange()
        return db.dietDao().getEntriesForDay(start, end)
    }

    fun getTodayDietSummary() = db.dietDao().getDailySummary(
        DateUtils.todayRange().first,
        DateUtils.todayRange().second
    )

    fun getRecentDietEntries(limit: Int = 50): Flow<List<DietEntry>> =
        db.dietDao().getRecentEntries(limit)

    suspend fun addDietEntry(entry: DietEntry) = db.dietDao().insert(entry)
    suspend fun deleteDietEntry(entry: DietEntry) = db.dietDao().delete(entry)

    // Exercises
    fun getAllExercises(): Flow<List<Exercise>> = db.exerciseDao().getAllExercises()
    suspend fun addExercise(exercise: Exercise): Long = db.exerciseDao().insert(exercise)
    suspend fun deleteExercise(exercise: Exercise) = db.exerciseDao().delete(exercise)

    // Lifts
    fun getAllLiftEntries(): Flow<List<LiftEntry>> = db.liftDao().getAllEntries()
    fun getLiftEntriesForExercise(name: String): Flow<List<LiftEntry>> =
        db.liftDao().getEntriesForExercise(name)

    fun getTrackedExerciseNames(): Flow<List<String>> = db.liftDao().getTrackedExerciseNames()
    fun getAllPersonalRecords() = db.liftDao().getAllPersonalRecords()

    suspend fun addLiftEntry(entry: LiftEntry): LiftEntry {
        val currentPR = db.liftDao().getPersonalRecord(entry.exerciseName)
        val isPR = currentPR == null || entry.weight > currentPR.weight
        val finalEntry = entry.copy(isPersonalRecord = isPR)
        db.liftDao().insert(finalEntry)
        return finalEntry
    }

    suspend fun deleteLiftEntry(entry: LiftEntry) = db.liftDao().delete(entry)

    // Predictions
    fun calculateBMR(profile: UserProfile): Double {
        val weightKg = profile.currentWeight * 0.453592
        val heightCm = profile.heightInches * 2.54
        return 10 * weightKg + 6.25 * heightCm - 5 * profile.age + 5
    }

    fun calculateTDEE(profile: UserProfile): Double {
        return calculateBMR(profile) * profile.activityLevel.multiplier
    }

    fun calculateWeeklyDeficit(profile: UserProfile, avgDailyCalories: Int): Double {
        val tdee = calculateTDEE(profile)
        val dailyDeficit = tdee - avgDailyCalories
        return dailyDeficit * 7
    }

    fun predictWeightInWeeks(
        currentWeight: Double,
        weeklyDeficit: Double,
        weeks: Int
    ): Double {
        val weeklyWeightLoss = weeklyDeficit / 3500.0
        return currentWeight - (weeklyWeightLoss * weeks)
    }

    fun generateDietTips(
        profile: UserProfile,
        avgCalories: Int,
        avgProtein: Double
    ): List<String> {
        val tips = mutableListOf<String>()
        val tdee = calculateTDEE(profile)
        val weightKg = profile.currentWeight * 0.453592

        if (avgCalories > tdee) {
            val excess = (avgCalories - tdee).toInt()
            tips.add("You're eating about $excess calories above your TDEE. Consider reducing portion sizes or swapping high-calorie snacks.")
        } else if (avgCalories < tdee * 0.6) {
            tips.add("Your calorie intake seems very low. Extreme deficits can slow metabolism and lead to muscle loss. Aim for a moderate deficit of 300-500 calories below TDEE.")
        } else if (avgCalories < tdee) {
            val deficit = (tdee - avgCalories).toInt()
            tips.add("You're in a $deficit calorie deficit — on track for steady weight loss.")
        }

        val recommendedProtein = weightKg * 1.6
        if (avgProtein < recommendedProtein) {
            tips.add("Aim for at least ${recommendedProtein.toInt()}g of protein daily (1.6g/kg body weight) to preserve muscle while losing weight.")
        } else {
            tips.add("Great protein intake! You're hitting your target for muscle preservation.")
        }

        if (profile.currentWeight > profile.goalWeight) {
            val remaining = profile.currentWeight - profile.goalWeight
            tips.add("You have ${String.format("%.1f", remaining)} lbs to go. At a healthy rate of 1-2 lbs/week, you could reach your goal in ${(remaining / 1.5).toInt()}-${(remaining / 1.0).toInt()} weeks.")
        }

        tips.add("Stay hydrated — aim for at least half your body weight in ounces of water daily (${(profile.currentWeight / 2).toInt()} oz).")
        tips.add("Prioritize whole foods: lean proteins, vegetables, fruits, and whole grains for better satiety and nutrition.")

        return tips
    }
}
