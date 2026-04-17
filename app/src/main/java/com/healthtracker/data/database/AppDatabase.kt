package com.healthtracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.healthtracker.data.model.DietEntry
import com.healthtracker.data.model.Exercise
import com.healthtracker.data.model.LiftEntry
import com.healthtracker.data.model.UserProfile
import com.healthtracker.data.model.WeightEntry

@Database(
    entities = [
        UserProfile::class,
        WeightEntry::class,
        DietEntry::class,
        Exercise::class,
        LiftEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weightDao(): WeightDao
    abstract fun dietDao(): DietDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun liftDao(): LiftDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
