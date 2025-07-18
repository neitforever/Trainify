package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.database.WorkoutDatabase
import com.example.motivationcalendarapi.model.Exercise
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class ExerciseRepository(
    val appDatabase: WorkoutDatabase,
    private val firestoreRepo: ExerciseFirestoreRepository,
    private val auth: FirebaseAuth
) {
    //Надо
    fun getBodyPartsLocalized(lang: String): Flow<List<String>> {
        return appDatabase.exerciseDao().getAllExercisesFlow().map { exercises ->
            exercises
                .mapNotNull { it.bodyPartLocalized[lang] }
                .distinct()
                .sortedBy { it.lowercase(Locale.getDefault()) }
        }
    }
    //Надо
    fun getExercisesLocalizedByBodyPart(bodyPart: String, lang: String): Flow<List<Exercise>> {
        return appDatabase.exerciseDao().getAllExercisesFlow().map { exercises ->
            exercises.filter {
                it.bodyPartLocalized[lang] == bodyPart
            }.sortedWith(
                compareByDescending<Exercise> { it.favorite }
                    .thenBy { it.nameLocalized[lang] ?: "" }
            )
        }
    }



    private val currentUser get() = auth.currentUser

    suspend fun initializeExercises() {
        val localCount = appDatabase.exerciseDao().getExerciseCount()
        if (localCount == 0) {
            // Fetch localized exercises from Firestore
            val localizedExercises = firestoreRepo.getLocalizedExercises()

            // Save to user's collection if authenticated
            if (auth.currentUser != null) {
                firestoreRepo.saveLocalizedExercisesForUser(localizedExercises)
            }

            // Save to local DB
            appDatabase.exerciseDao().insertAllExercises(localizedExercises)
        }
    }

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

    suspend fun updateExerciseName(id: String, newName: Map<String, String>) {
        val exercise = appDatabase.exerciseDao().getExerciseById(id) ?: return
        val updatedExercise = exercise.copy(nameLocalized = newName)

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
//    fun getExercisesByBodyPart(bodyPart: String): Flow<List<Exercise>> {
//        return appDatabase.exerciseDao().getExercisesByBodyPart(bodyPart)
//    }


//    suspend fun getAllExercisesOnce(): List<Exercise> {
//        return appDatabase.exerciseDao().getAllExercisesOnce()
//    }

    fun getAllBodyParts() = appDatabase.exerciseDao().getAllBodyParts()

//    suspend fun getExerciseCount() = appDatabase.exerciseDao().getExerciseCount()


    fun getExerciseById(id: String) = appDatabase.exerciseDao().getExerciseById(id)

    fun getFavoriteExercises() = appDatabase.exerciseDao().getFavoriteExercises()




    fun searchExercises(query: String): Flow<List<Exercise>> {
        return appDatabase.exerciseDao().searchExercises(query)
    }

    fun getAllEquipment() = appDatabase.exerciseDao().getAllEquipment()

    suspend fun updateExerciseEquipment(id: String, newEquipment: Map<String, String>) {
        val exercise = appDatabase.exerciseDao().getExerciseById(id) ?: return
        val updatedExercise = exercise.copy(equipmentLocalized = newEquipment)
        insertExercise(updatedExercise)
    }

    suspend fun updateExerciseBodyPart(id: String, newBodyPart: Map<String, String>) {
        val exercise = appDatabase.exerciseDao().getExerciseById(id) ?: return
        val updatedExercise = exercise.copy(bodyPartLocalized = newBodyPart)
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

    suspend fun updateExerciseInstructions(id: String, newInstructions: Map<String,List<String>>) {
        val exercise = appDatabase.exerciseDao().getExerciseById(id) ?: return
        val updatedExercise = exercise.copy(instructionsLocalized = newInstructions)
        insertExercise(updatedExercise)
    }



}