package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExerciseResponse
import com.example.motivationcalendarapi.network.ApiClient
import com.google.gson.Gson
import com.motivationcalendar.data.WorkoutDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepository(val appDatabase: WorkoutDatabase){

    suspend fun getExerciseFromApi(): List<ExerciseResponse> = ApiClient.apiService.getExercises()

    suspend fun insertExercise(exercise: Exercise) {
        appDatabase.exerciseDao().insertExercise(exercise)
    }

    fun getExercisesByBodyPart(bodyPart: String): Flow<List<Exercise>>{
        return appDatabase.exerciseDao().getExercisesByBodyPart(bodyPart)
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

    suspend fun deleteExercise(id: String) {
        appDatabase.exerciseDao().deleteExercise(id)
    }

}