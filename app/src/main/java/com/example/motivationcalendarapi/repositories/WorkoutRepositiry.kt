package com.example.motivationcalendarapi.repositories

import androidx.lifecycle.ViewModel
import com.example.motivationcalendarapi.database.WorkoutDatabase
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.Workout
import com.google.firebase.auth.FirebaseAuth
import com.example.motivationcalendarapi.model.reward.RewardUiModel
import com.example.motivationcalendarapi.model.reward.RewardUnlockEventEntity
import com.example.motivationcalendarapi.repositories.reward.RewardFirestoreRepository
import com.example.motivationcalendarapi.repositories.reward.RewardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import android.util.Log


class WorkoutRepository(
    private val appDatabase: WorkoutDatabase,
    private val firestoreRepo: WorkoutFirestoreRepository,
    private val templateFirestoreRepo: TemplateFirestoreRepository,
    private val exerciseFirestoreRepo: ExerciseFirestoreRepository,
    private val auth: FirebaseAuth
): ViewModel() {

    private val currentUser get() = auth.currentUser
    val plannedWorkoutRepository = PlannedWorkoutRepository(appDatabase, PlannedWorkoutFirestoreRepository(), auth)

    private val rewardRepository = RewardRepository(appDatabase, RewardFirestoreRepository(), auth)


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
            val remote = templateFirestoreRepo.getAllTemplatesOnce(waitForAuth = true)
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
    private suspend fun getUserTemplatesWithRetries(): List<Template> {
        val delays = listOf(0L, 1_500L, 3_000L, 5_000L)

        delays.forEachIndexed { index, delayMillis ->
            if (delayMillis > 0L) delay(delayMillis)

            val userTemplates = templateFirestoreRepo.getAllTemplatesOnce(waitForAuth = true)
            Log.d(
                "FirestoreFallback",
                "User templates attempt=${index + 1} count=${userTemplates.size} uid=${currentUser?.uid}"
            )

            if (userTemplates.isNotEmpty()) return userTemplates
        }

        return emptyList()
    }

    suspend fun initializeDefaultTemplates() {
        try {
            val localTemplates = appDatabase.templateDao()
                .getAllTemplates()
                .first()

            if (localTemplates.isNotEmpty()) return

            val remoteUserTemplates = getUserTemplatesWithRetries()

            if (remoteUserTemplates.isNotEmpty()) {
                appDatabase.templateDao().insertAll(remoteUserTemplates)
                return
            }

            Log.d(
                "FirestoreFallback",
                "User templates are still empty after retries. Root fallback will run after extra delay. uid=${currentUser?.uid}"
            )
            delay(5_000L)

            val finalUserTemplates = templateFirestoreRepo.getAllTemplatesOnce(waitForAuth = true)
            Log.d(
                "FirestoreFallback",
                "Final user templates check before root. count=${finalUserTemplates.size} uid=${currentUser?.uid}"
            )
            if (finalUserTemplates.isNotEmpty()) {
                appDatabase.templateDao().insertAll(finalUserTemplates)
                return
            }

            val defaultTemplates = templateFirestoreRepo.getDefaultTemplatesOnce()
            Log.d("FirestoreFallback", "Load root templates count=${defaultTemplates.size}")

            if (defaultTemplates.isEmpty()) return

            appDatabase.templateDao().insertAll(defaultTemplates)

        } catch (e: Exception) {
            println("initializeDefaultTemplates error: ${e.message}")
        }
    }

    suspend fun reloadTemplatesFromFirestoreIfRoomEmpty() = initializeDefaultTemplates()

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
                    val updatedSets = exercise.sets.toMutableList().apply {
                        add(newSet)
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

    suspend fun removeTemplateSet(templateId: String, exerciseIndex: Int, setIndex: Int) {
        val template = appDatabase.templateDao().getTemplateById(templateId).first()
        template?.let {
            val updatedExercises = it.exercises.toMutableList()
            if (exerciseIndex < updatedExercises.size) {
                val exercise = updatedExercises[exerciseIndex]
                val updatedSets = exercise.sets.toMutableList().apply {
                    removeAt(setIndex)
                }
                updatedExercises[exerciseIndex] = exercise.copy(sets = updatedSets)

                updateTemplate(it.copy(exercises = updatedExercises))
            }
        }
    }

    suspend fun updateTemplateNameLocalized(
        templateId: String,
        newNameLocalized: Map<String, String>
    ) {
        val template = appDatabase.templateDao()
            .getTemplateById(templateId)
            .first()
            ?: return

        val updatedTemplate = template.copy(
            nameLocalized = newNameLocalized,
            timestamp = System.currentTimeMillis()
        )

        updateTemplate(updatedTemplate)
    }


    fun getTemplateById(id: String): Flow<Template?> {
        return appDatabase.templateDao().getTemplateById(id)
    }



    fun observeRewards(): Flow<List<RewardUiModel>> = rewardRepository.observeRewards()

    fun observePendingRewardUnlockEvents(): Flow<List<RewardUnlockEventEntity>> = rewardRepository.observePendingUnlockEvents()

    suspend fun initializeRewards() = rewardRepository.initializeRewards()

    suspend fun syncRewardsWithFirestore() = rewardRepository.syncFromFirestore()

    suspend fun evaluateDailyStepsForRewards(steps: Long?) = rewardRepository.evaluateDailySteps(steps)

    suspend fun evaluateBodyProgressEntriesForRewards(count: Int) = rewardRepository.evaluateBodyProgressEntries(count)

    suspend fun increaseAiExerciseCreatedForRewards() = rewardRepository.increaseAiExerciseCreated()

    suspend fun increaseAiTemplateCreatedForRewards() = rewardRepository.increaseAiTemplateCreated()

    suspend fun unlockHealthConnectConnectedForRewards() = rewardRepository.unlockHealthConnectConnected()

    suspend fun unlockEquipmentRecognizerUsedForRewards() = rewardRepository.unlockEquipmentRecognizerUsed()

    suspend fun markRewardUnlockEventShown(eventId: String) = rewardRepository.markUnlockEventShown(eventId)

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
        rewardRepository.evaluateAfterWorkout(workout)
    }

    suspend fun delete(workout: Workout) {
        if (currentUser != null) {
            firestoreRepo.delete(workout)
            appDatabase.workoutDao().deleteWorkout(workout.id)
        } else {
            appDatabase.workoutDao().deleteWorkout(workout.id)
        }
    }

//    suspend fun updateWorkout(workout: Workout) {
//        if (currentUser != null) {
//            firestoreRepo.update(workout)
//            appDatabase.workoutDao().updateWorkout(workout)
//        } else {
//            appDatabase.workoutDao().updateWorkout(workout)
//        }
//    }

    suspend fun syncWithFirestore() {
        if (currentUser == null) return

        val remoteData = firestoreRepo.getAllWorkoutsOnce()
        appDatabase.workoutDao().deleteAll()
        remoteData.forEach { appDatabase.workoutDao().insert(it) }

        rewardRepository.syncFromFirestore()
        plannedWorkoutRepository.syncWithFirestore()
        val localData = appDatabase.workoutDao().getAllWorkouts().first()
        localData.forEach { firestoreRepo.insert(it) }
    }

    fun getWorkoutById(id: String): Workout {
        return appDatabase.workoutDao().getWorkoutById(id)
    }

    fun getExerciseNotesUpdates(): Flow<List<Exercise>> =
        appDatabase.exerciseDao().getAllExercisesWithNotes()

//
//    suspend fun getExerciseNoteById(id: String): String? =
//        appDatabase.exerciseDao().getExerciseNote(id)

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