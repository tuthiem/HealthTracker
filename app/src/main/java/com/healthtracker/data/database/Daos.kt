package com.healthtracker.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.healthtracker.data.model.DietEntry
import com.healthtracker.data.model.Exercise
import com.healthtracker.data.model.LiftEntry
import com.healthtracker.data.model.UserProfile
import com.healthtracker.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT :limit")
    fun getRecentEntries(limit: Int): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries ORDER BY date ASC")
    fun getAllEntriesAsc(): Flow<List<WeightEntry>>

    @Insert
    suspend fun insert(entry: WeightEntry)

    @Delete
    suspend fun delete(entry: WeightEntry)
}

@Dao
interface DietDao {
    @Query("SELECT * FROM diet_entries WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date DESC")
    fun getEntriesForDay(startOfDay: Long, endOfDay: Long): Flow<List<DietEntry>>

    @Query("SELECT * FROM diet_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DietEntry>>

    @Query("SELECT * FROM diet_entries ORDER BY date DESC LIMIT :limit")
    fun getRecentEntries(limit: Int): Flow<List<DietEntry>>

    @Query("""
        SELECT COALESCE(SUM(calories), 0) as totalCalories,
               COALESCE(SUM(proteinGrams), 0.0) as totalProtein,
               COALESCE(SUM(carbsGrams), 0.0) as totalCarbs,
               COALESCE(SUM(fatGrams), 0.0) as totalFat
        FROM diet_entries
        WHERE date >= :startOfDay AND date < :endOfDay
    """)
    fun getDailySummary(startOfDay: Long, endOfDay: Long): Flow<DailySummaryResult>

    @Insert
    suspend fun insert(entry: DietEntry)

    @Delete
    suspend fun delete(entry: DietEntry)
}

data class DailySummaryResult(
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double
)

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE category = :category ORDER BY name ASC")
    fun getExercisesByCategory(category: String): Flow<List<Exercise>>

    @Insert
    suspend fun insert(exercise: Exercise): Long

    @Delete
    suspend fun delete(exercise: Exercise)
}

@Dao
interface LiftDao {
    @Query("SELECT * FROM lift_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<LiftEntry>>

    @Query("SELECT * FROM lift_entries WHERE exerciseName = :exerciseName ORDER BY date DESC")
    fun getEntriesForExercise(exerciseName: String): Flow<List<LiftEntry>>

    @Query("""
        SELECT * FROM lift_entries
        WHERE exerciseName = :exerciseName
        ORDER BY weight DESC
        LIMIT 1
    """)
    suspend fun getPersonalRecord(exerciseName: String): LiftEntry?

    @Query("""
        SELECT exerciseName, MAX(weight) as maxWeight
        FROM lift_entries
        GROUP BY exerciseName
        ORDER BY exerciseName ASC
    """)
    fun getAllPersonalRecords(): Flow<List<PRResult>>

    @Query("SELECT DISTINCT exerciseName FROM lift_entries ORDER BY exerciseName ASC")
    fun getTrackedExerciseNames(): Flow<List<String>>

    @Insert
    suspend fun insert(entry: LiftEntry)

    @Delete
    suspend fun delete(entry: LiftEntry)
}

data class PRResult(
    val exerciseName: String,
    val maxWeight: Double
)
