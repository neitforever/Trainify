package com.motivationcalendar.data

import android.content.Context
import androidx.room.*
import com.example.motivationcalendarapi.database.ExerciseDao
import com.example.motivationcalendarapi.model.Converters
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.tryy.BodyProgress
import com.example.motivationcalendarapi.tryy.BodyProgressDao


@Database(
    entities = [Workout::class, Exercise::class, BodyProgress::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun bodyProgressDao(): BodyProgressDao


    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getDatabase(context: Context): WorkoutDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }



    }
}
