package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.database.WorkoutDatabase
import com.example.motivationcalendarapi.model.Exercise
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow

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


    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean) {
        val exercise = appDatabase.exerciseDao().getExerciseById(id) ?: return
        val updatedExercise = exercise.copy(favorite = isFavorite)

        appDatabase.exerciseDao().insertExercise(updatedExercise)

        if (currentUser != null) {
            firestoreRepo.update(updatedExercise.copy(
                favorite = isFavorite
            ))
        }
    }

    suspend fun updateExerciseName(id: String, newName: String) {
        val exercise = appDatabase.exerciseDao().getExerciseById(id) ?: return
        val updatedExercise = exercise.copy(name = newName)

        appDatabase.exerciseDao().insertExercise(updatedExercise)

        if (currentUser != null) {
            firestoreRepo.update(updatedExercise)
        }
    }

//    suspend fun getExerciseFromApi(): List<Exercise> {
//        return try {
//            val localExercises = appDatabase.exerciseDao().getAllExercisesOnce()
//            ApiClient.apiService.getExercises()
//                .map { response ->
//                    val localExercise = localExercises.find { it.id == response.id }
//                    response.toEntity().copy(
//                        favorite = localExercise?.favorite ?: false
//                    )
//                }
//        } catch (_: Exception) {
//            emptyList()
//        }
//    }

    suspend fun syncExercisesWithFirestore() {
        if (currentUser == null) return

        val remoteExercises = firestoreRepo.getAllExercisesOnce()
        val localExercises = appDatabase.exerciseDao().getAllExercisesOnce()

        val updatedExercises = remoteExercises.map { remote ->
            val local = localExercises.find { it.id == remote.id }
            remote.copy(
                favorite = local?.favorite ?: remote.favorite
            )
        }

        appDatabase.exerciseDao().insertAllExercises(updatedExercises)
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


//    suspend fun getAllExercisesOnce(): List<Exercise> {
//        return appDatabase.exerciseDao().getAllExercisesOnce()
//    }

    fun getAllBodyParts() = appDatabase.exerciseDao().getAllBodyParts()

    suspend fun getExerciseCount() = appDatabase.exerciseDao().getExerciseCount()


    fun getExerciseById(id: String) = appDatabase.exerciseDao().getExerciseById(id)

    fun getFavoriteExercises() = appDatabase.exerciseDao().getFavoriteExercises()




    fun searchExercises(query: String): Flow<List<Exercise>> {
        return appDatabase.exerciseDao().searchExercises(query)
    }

    fun getAllEquipment() = appDatabase.exerciseDao().getAllEquipment()

    suspend fun updateExerciseEquipment(id: String, newEquipment: String) {
        val exercise = appDatabase.exerciseDao().getExerciseById(id) ?: return
        val updatedExercise = exercise.copy(equipment = newEquipment)
        insertExercise(updatedExercise)
    }

    suspend fun updateExerciseBodyPart(id: String, newBodyPart: String) {
        val exercise = appDatabase.exerciseDao().getExerciseById(id) ?: return
        val updatedExercise = exercise.copy(bodyPart = newBodyPart)
        insertExercise(updatedExercise)
    }


//    suspend fun updateExerciseSecondaryMuscles(id: String, newSecondaryMuscles: String) {
//        val exercise = appDatabase.exerciseDao().getExerciseById(id) ?: return
//        val updatedExercise = exercise.copy(secondaryMuscles = parseSecondaryMuscles(newSecondaryMuscles))
//        insertExercise(updatedExercise)
//    }

//    private fun parseSecondaryMuscles(input: String): List<String> {
//        return input.split(",").map { it.trim() }.filter { it.isNotEmpty() }
//    }

//    fun getAllSecondaryMuscles() = appDatabase.exerciseDao().getAllSecondaryMuscles()

//    private val gson = Gson()

    suspend fun updateExerciseInstructions(id: String, newInstructions: List<String>) {
        val exercise = appDatabase.exerciseDao().getExerciseById(id) ?: return
        val updatedExercise = exercise.copy(instructions = newInstructions)
        insertExercise(updatedExercise)
    }



}