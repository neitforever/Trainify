package com.example.motivationcalendarapi.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.motivationcalendarapi.model.BodyProgress
import com.example.motivationcalendarapi.model.Converters
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.Workout


@Database(
    entities = [Workout::class, Exercise::class, BodyProgress::class, Template::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun bodyProgressDao(): BodyProgressDao
    abstract fun templateDao(): TemplateDao
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
