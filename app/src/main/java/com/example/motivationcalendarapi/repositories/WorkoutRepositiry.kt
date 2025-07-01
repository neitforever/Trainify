package com.example.motivationcalendarapi.repositories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.Workout
import com.google.firebase.auth.FirebaseAuth
import com.motivationcalendar.data.WorkoutDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class WorkoutRepository(
    private val appDatabase: WorkoutDatabase,
    private val firestoreRepo: WorkoutFirestoreRepository,
    private val templateFirestoreRepo: TemplateFirestoreRepository,
    private val exerciseFirestoreRepo: ExerciseFirestoreRepository,
    private val auth: FirebaseAuth
): ViewModel() {

    private val currentUser get() = auth.currentUser


    suspend fun insertTemplate(template: Template) {
        if (currentUser != null) {
            templateFirestoreRepo.insert(template)
            appDatabase.templateDao().insert(template)
        } else {
            appDatabase.templateDao().insert(template)
        }
    }



    suspend fun updateTemplate(template: Template) {
        if (currentUser != null) {
            templateFirestoreRepo.update(template)
            appDatabase.templateDao().insert(template)
        } else {
            appDatabase.templateDao().insert(template)
        }
    }

    suspend fun deleteTemplate(template: Template) {
        if (currentUser != null) {
            templateFirestoreRepo.delete(template.id)
        }
        appDatabase.templateDao().deleteTemplate(template.id)
    }

    fun getAllTemplates(): Flow<List<Template>> {
        return appDatabase.templateDao().getAllTemplates()
    }


    suspend fun syncTemplatesWithFirestore() {
        try {
            if (currentUser == null) {
                println("Sync skipped: user not authenticated")
                return
            }

            println("Starting sync...")
            val remote = templateFirestoreRepo.getAllTemplatesOnce()
            println("Remote templates: ${remote.size}")

            val local = appDatabase.templateDao().getAllTemplates().first()
            println("Local templates: ${local.size}")

            val merged = mergeTemplates(remote, local)
            println("Merged templates: ${merged.size}")

            appDatabase.templateDao().deleteAllTemplates()
            appDatabase.templateDao().insertAll(merged)

            println("Local DB updated")

            merged.forEach {
                templateFirestoreRepo.insert(it)
            }
            println("Firestore updated")
        } catch (e: Exception) {
            println("Sync error: ${e.message}")
        }
    }


    private fun mergeTemplates(remote: List<Template>, local: List<Template>): List<Template> {
        val merged = mutableListOf<Template>()
        val allIds = (remote.map { it.id } + local.map { it.id }).toSet()

        allIds.forEach { id ->
            val remoteTemplate = remote.find { it.id == id }
            val localTemplate = local.find { it.id == id }

            when {
                remoteTemplate == null -> localTemplate?.let { merged.add(it) }
                localTemplate == null -> merged.add(remoteTemplate)
                else -> {
                    // Выбираем шаблон с более новым timestamp
                    if (remoteTemplate.timestamp > localTemplate.timestamp) {
                        merged.add(remoteTemplate)
                    } else {
                        merged.add(localTemplate)
                    }
                }
            }
        }
        return merged
    }

    suspend fun updateTemplateSet(templateId: String, exerciseIndex: Int, setIndex: Int, newSet: ExerciseSet) {
        val template = appDatabase.templateDao().getTemplateById(templateId).first()
        template?.let {
            val updatedExercises = it.exercises.toMutableList().apply {
                if (exerciseIndex < size) {
                    val exercise = this[exerciseIndex]
                    val updatedSets = exercise.sets.toMutableList().apply {
                        if (setIndex < size) {
                            this[setIndex] = newSet
                        }
                    }
                    this[exerciseIndex] = exercise.copy(sets = updatedSets)
                }
            }

            appDatabase.templateDao().insert(it.copy(exercises = updatedExercises))

            if (currentUser != null) {
                templateFirestoreRepo.update(it.copy(exercises = updatedExercises))
            }
        }
    }

    suspend fun addSetToTemplate(templateId: String, exerciseIndex: Int, newSet: ExerciseSet) {
        val template = appDatabase.templateDao().getTemplateById(templateId).first()
        template?.let {
            val updatedExercises = it.exercises.toMutableList().apply {
                if (exerciseIndex < size) {
                    val exercise = this[exerciseIndex]
                    val updatedSets = exercise.sets.toMutableList() + newSet
                    this[exerciseIndex] = exercise.copy(sets = updatedSets)
                }
            }

            appDatabase.templateDao().insert(it.copy(exercises = updatedExercises))

            if (currentUser != null) {
                templateFirestoreRepo.update(it.copy(exercises = updatedExercises))
            }
        }
    }
    suspend fun updateTemplateName(templateId: String, newName: String) {
        appDatabase.templateDao().updateTemplateName(templateId, newName)
    }


    fun getTemplateById(id: String): Flow<Template?> {
        return appDatabase.templateDao().getTemplateById(id)
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

        if (currentUser != null) {
                exerciseFirestoreRepo.updateExerciseNote(id, newNote)
        }
    }


    fun getWorkoutsToday(): Flow<List<Workout>> {
        return appDatabase.workoutDao().getWorkoutsToday()
    }

}