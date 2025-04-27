package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.mapper.toEntity
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExerciseResponse
import com.example.motivationcalendarapi.network.ApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.motivationcalendar.data.WorkoutDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepository(
    val appDatabase: WorkoutDatabase,
    private val firestoreRepo: ExerciseFirestoreRepository,
    private val auth: FirebaseAuth
) {

    private val currentUser get() = auth.currentUser

    suspend fun insertExercise(exercise: Exercise) {
        if (currentUser != null) {
            firestoreRepo.insert(exercise)
            appDatabase.exerciseDao().insertExercise(exercise)
        } else {
            appDatabase.exerciseDao().insertExercise(exercise)
        }
    }

    internal suspend fun getExerciseFromApi(): List<Exercise> {
        return try {
            ApiClient.apiService.getExercises()
                .map { it.toEntity() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun syncExercisesWithFirestore() {
        if (currentUser == null) return

        val remoteExercises = firestoreRepo.getAllExercisesOnce()
        if (remoteExercises.isNotEmpty()) {
            appDatabase.exerciseDao().deleteAllExercises()
            remoteExercises.forEach { appDatabase.exerciseDao().insertExercise(it) }
        } else {
            val apiExercises = getExerciseFromApi()
            apiExercises.forEach { exercise ->
                insertExercise(exercise)
                firestoreRepo.insert(exercise)
            }
        }
    }



    suspend fun deleteExercise(id: String) {
        if (currentUser != null) {
            firestoreRepo.delete(id)
        }
        appDatabase.exerciseDao().deleteExercise(id)
    }
    fun getExercisesByBodyPart(bodyPart: String): Flow<List<Exercise>> {
        return appDatabase.exerciseDao().getExercisesByBodyPart(bodyPart)
    }


    suspend fun getAllExercisesOnce(): List<Exercise> {
        return appDatabase.exerciseDao().getAllExercisesOnce()
    }

    fun getAllBodyParts() = appDatabase.exerciseDao().getAllBodyParts()

    suspend fun getExerciseCount() = appDatabase.exerciseDao().getExerciseCount()


    fun getExerciseById(id: String) = appDatabase.exerciseDao().getExerciseById(id)

    fun getFavoriteExercises() = appDatabase.exerciseDao().getFavoriteExercises()

    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean) {
        appDatabase.exerciseDao().updateFavoriteStatus(id, isFavorite)
    }


    fun searchExercises(query: String): Flow<List<Exercise>> {
        return appDatabase.exerciseDao().searchExercises(query)
    }

    suspend fun updateExerciseName(id: String, newName: String) {
        appDatabase.exerciseDao().updateExerciseName(id, newName)
    }


    fun getAllEquipment() = appDatabase.exerciseDao().getAllEquipment()

    suspend fun updateExerciseEquipment(id: String, newEquipment: String) {
        appDatabase.exerciseDao().updateExerciseEquipment(id, newEquipment)
    }

    suspend fun updateExerciseBodyPart(id: String, newBodyPart: String) {
        appDatabase.exerciseDao().updateExerciseBodyPart(id, newBodyPart)
    }


    suspend fun updateExerciseSecondaryMuscles(id: String, newSecondaryMuscles: String) {
        appDatabase.exerciseDao().updateExerciseSecondaryMuscles(id, newSecondaryMuscles)
    }

    fun getAllSecondaryMuscles() = appDatabase.exerciseDao().getAllSecondaryMuscles()

    private val gson = Gson()

    suspend fun updateExerciseInstructions(id: String, newInstructions: List<String>) {
        val instructionsJson = gson.toJson(newInstructions)
        appDatabase.exerciseDao().updateExerciseInstructions(id, instructionsJson)
    }



}