package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.Workout
import com.motivationcalendar.data.WorkoutDatabase
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(val appDatabase: WorkoutDatabase) {


    suspend fun insertWorkout(workout: Workout) {
        return appDatabase.workoutDao().insert(workout)
    }

    suspend fun delete(workout: Workout) = appDatabase.workoutDao().deleteWorkout(workout.id)

    fun getAllWorkouts(): Flow<List<Workout>> {
        return appDatabase.workoutDao().getAllWorkouts()
    }

    fun getWorkoutById(id: Long): Workout {
        return appDatabase.workoutDao().getWorkoutById(id)
    }

    fun getExerciseNotesUpdates(): Flow<List<Exercise>> =
        appDatabase.exerciseDao().getAllExercisesWithNotes()


    suspend fun getExerciseNoteById(id: String): String? =
        appDatabase.exerciseDao().getExerciseNote(id)

    suspend fun updateExerciseNote(id: String, newNote: String) {
        appDatabase.exerciseDao().updateExerciseNote(id, newNote)
    }

    suspend fun updateWorkout(workout: Workout) =
        appDatabase.workoutDao().updateWorkout(workout)

    fun getWorkoutsToday(): Flow<List<Workout>> {
        return appDatabase.workoutDao().getWorkoutsToday()
    }

}