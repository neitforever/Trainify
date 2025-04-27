package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.Workout
import com.google.firebase.auth.FirebaseAuth
import com.motivationcalendar.data.WorkoutDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first


class WorkoutRepository(
    private val appDatabase: WorkoutDatabase,
    private val firestoreRepo: WorkoutFirestoreRepository,
    private val auth: FirebaseAuth
) {

    private val currentUser get() = auth.currentUser


    suspend fun insertTemplate(template: Template) {
        appDatabase.templateDao().insert(template)
    }

    suspend fun updateTemplateName(templateId: String, newName: String) {
        appDatabase.templateDao().updateTemplateName(templateId, newName)
    }

    suspend fun updateTemplate(template: Template) {
        appDatabase.templateDao().insert(template)
    }

    fun getTemplateById(id: String): Flow<Template?> {
        return appDatabase.templateDao().getTemplateById(id)
    }

    fun getAllTemplates(): Flow<List<Template>> {
        return appDatabase.templateDao().getAllTemplates()
    }

    suspend fun deleteTemplate(template: Template) {
        appDatabase.templateDao().deleteTemplate(template.id)
    }

    fun getAllWorkouts(): Flow<List<Workout>> {
        return if (currentUser != null) {
            firestoreRepo.getAllWorkouts()
        } else {
            appDatabase.workoutDao().getAllWorkouts()
        }
    }

    suspend fun insertWorkout(workout: Workout) {
        if (currentUser != null) {
            firestoreRepo.insert(workout)
            appDatabase.workoutDao().insert(workout)
        } else {
            appDatabase.workoutDao().insert(workout)
        }
    }

    suspend fun delete(workout: Workout) {
        if (currentUser != null) {
            firestoreRepo.delete(workout)
            appDatabase.workoutDao().deleteWorkout(workout.id)
        } else {
            appDatabase.workoutDao().deleteWorkout(workout.id)
        }
    }

    suspend fun updateWorkout(workout: Workout) {
        if (currentUser != null) {
            firestoreRepo.update(workout)
            appDatabase.workoutDao().updateWorkout(workout)
        } else {
            appDatabase.workoutDao().updateWorkout(workout)
        }
    }

    suspend fun syncWithFirestore() {
        if (currentUser == null) return

        val remoteData = firestoreRepo.getAllWorkoutsOnce()
        appDatabase.workoutDao().deleteAll()
        remoteData.forEach { appDatabase.workoutDao().insert(it) }

        val localData = appDatabase.workoutDao().getAllWorkouts().first()
        localData.forEach { firestoreRepo.insert(it) }
    }

    fun getWorkoutById(id: String): Workout {
        return appDatabase.workoutDao().getWorkoutById(id)
    }

    fun getExerciseNotesUpdates(): Flow<List<Exercise>> =
        appDatabase.exerciseDao().getAllExercisesWithNotes()


    suspend fun getExerciseNoteById(id: String): String? =
        appDatabase.exerciseDao().getExerciseNote(id)

    suspend fun updateExerciseNote(id: String, newNote: String) {
        appDatabase.exerciseDao().updateExerciseNote(id, newNote)
    }



    fun getWorkoutsToday(): Flow<List<Workout>> {
        return appDatabase.workoutDao().getWorkoutsToday()
    }

}