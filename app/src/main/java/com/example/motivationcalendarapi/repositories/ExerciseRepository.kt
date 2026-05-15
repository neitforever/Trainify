package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.database.WorkoutDatabase
import com.example.motivationcalendarapi.model.Exercise
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale

class ExerciseRepository(
    val appDatabase: WorkoutDatabase,
    private val firestoreRepo: ExerciseFirestoreRepository,
    private val auth: FirebaseAuth
) {

    fun getBodyPartsLocalized(lang: String): Flow<List<String>> {
        return appDatabase.exerciseDao().getAllExercisesFlow().map { exercises ->
            exercises
                .mapNotNull { it.bodyPartLocalized[lang] }
                .distinct()
                .sortedBy { it.lowercase(Locale.getDefault()) }
        }
    }

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

    fun getAllEquipmentLocalized(lang: String): Flow<List<String>> {
        return appDatabase.exerciseDao().getAllExercisesFlow().map { exercises ->
            exercises
                .mapNotNull { it.equipmentLocalized[lang] }
                .distinct()
                .sortedBy { it.lowercase(Locale.getDefault()) }
        }
    }

    private val currentUser get() = auth.currentUser

    suspend fun initializeExercises() = withContext(Dispatchers.IO) {
        val localCount = appDatabase.exerciseDao().getExerciseCount()

        if (localCount == 0) {
            val localizedExercises = firestoreRepo.getLocalizedExercises()

            if (auth.currentUser != null) {
                firestoreRepo.saveLocalizedExercisesForUser(localizedExercises)
            }

            appDatabase.exerciseDao().insertAllExercises(localizedExercises)
        }
    }

    suspend fun insertExercise(exercise: Exercise) = withContext(Dispatchers.IO) {
        if (currentUser != null) {
            firestoreRepo.insert(exercise)
        }
        appDatabase.exerciseDao().insertExercise(exercise)
    }

    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        val exercise = appDatabase.exerciseDao().getExerciseById(id) ?: return@withContext
        val updatedExercise = exercise.copy(favorite = isFavorite)

        appDatabase.exerciseDao().insertExercise(updatedExercise)

        if (currentUser != null) {
            firestoreRepo.update(updatedExercise)
        }
    }

    suspend fun updateExerciseName(id: String, newName: Map<String, String>) = withContext(Dispatchers.IO) {
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseName START id=$id newName=$newName")

        val exercise = appDatabase.exerciseDao().getExerciseById(id)
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseName loaded exercise=$exercise")

        if (exercise == null) {
            android.util.Log.e("ExerciseDebug", "REPO updateExerciseName ERROR exercise is null")
            return@withContext
        }

        val updatedExercise = exercise.copy(nameLocalized = newName)
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseName updatedExercise=$updatedExercise")

        appDatabase.exerciseDao().insertExercise(updatedExercise)
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseName local insert SUCCESS")

        if (currentUser != null) {
            firestoreRepo.update(updatedExercise)
            android.util.Log.d("ExerciseDebug", "REPO updateExerciseName firestore update SUCCESS")
        }
    }

    suspend fun syncExercisesWithFirestore() = withContext(Dispatchers.IO) {
        if (currentUser == null) return@withContext

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

    suspend fun deleteExercise(id: String) = withContext(Dispatchers.IO) {
        if (currentUser != null) {
            firestoreRepo.delete(id)
        }
        appDatabase.exerciseDao().deleteExercise(id)
    }

    suspend fun getExerciseById(id: String): Exercise? = withContext(Dispatchers.IO) {
        appDatabase.exerciseDao().getExerciseById(id)
    }


    fun getAllExercises(): Flow<List<Exercise>> {
        return appDatabase.exerciseDao().getAllExercisesFlow()
    }

    fun getFavoriteExercises(): Flow<List<Exercise>> {
        return appDatabase.exerciseDao().getAllExercisesFlow().map { exercises ->
            exercises
                .filter { it.favorite }
                .sortedBy { it.nameLocalized["en"].orEmpty().lowercase(Locale.getDefault()) }
        }
    }

    fun searchExercises(query: String, lang: String): Flow<List<Exercise>> {
        return appDatabase.exerciseDao().getAllExercisesFlow().map { exercises ->
            exercises.filter { exercise ->
                exercise.getName(lang).contains(query, ignoreCase = true)
            }.sortedWith(
                compareByDescending<Exercise> { it.favorite }
                    .thenBy { it.getName(lang).lowercase(Locale.getDefault()) }
            )
        }
    }

    suspend fun updateExerciseEquipment(id: String, newEquipment: Map<String, String>) = withContext(Dispatchers.IO) {
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseEquipment START id=$id newEquipment=$newEquipment")

        val exercise = appDatabase.exerciseDao().getExerciseById(id)
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseEquipment loaded exercise=$exercise")

        if (exercise == null) {
            android.util.Log.e("ExerciseDebug", "REPO updateExerciseEquipment ERROR exercise is null")
            return@withContext
        }

        val updatedExercise = exercise.copy(equipmentLocalized = newEquipment)
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseEquipment updatedExercise=$updatedExercise")

        appDatabase.exerciseDao().insertExercise(updatedExercise)
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseEquipment local insert SUCCESS")

        if (currentUser != null) {
            firestoreRepo.update(updatedExercise)
            android.util.Log.d("ExerciseDebug", "REPO updateExerciseEquipment firestore update SUCCESS")
        }
    }

    suspend fun updateExerciseBodyPart(id: String, newBodyPart: Map<String, String>) = withContext(Dispatchers.IO) {
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseBodyPart START id=$id newBodyPart=$newBodyPart")

        val exercise = appDatabase.exerciseDao().getExerciseById(id)
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseBodyPart loaded exercise=$exercise")

        if (exercise == null) {
            android.util.Log.e("ExerciseDebug", "REPO updateExerciseBodyPart ERROR exercise is null")
            return@withContext
        }

        val updatedExercise = exercise.copy(bodyPartLocalized = newBodyPart)
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseBodyPart updatedExercise=$updatedExercise")

        appDatabase.exerciseDao().insertExercise(updatedExercise)
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseBodyPart local insert SUCCESS")

        if (currentUser != null) {
            firestoreRepo.update(updatedExercise)
            android.util.Log.d("ExerciseDebug", "REPO updateExerciseBodyPart firestore update SUCCESS")
        }
    }

    suspend fun updateExerciseInstructions(id: String, newInstructions: Map<String, List<String>>) = withContext(Dispatchers.IO) {
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseInstructions START id=$id newInstructions=$newInstructions")

        val exercise = appDatabase.exerciseDao().getExerciseById(id)
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseInstructions loaded exercise=$exercise")

        if (exercise == null) {
            android.util.Log.e("ExerciseDebug", "REPO updateExerciseInstructions ERROR exercise is null")
            return@withContext
        }

        val updatedExercise = exercise.copy(instructionsLocalized = newInstructions)
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseInstructions updatedExercise=$updatedExercise")

        appDatabase.exerciseDao().insertExercise(updatedExercise)
        android.util.Log.d("ExerciseDebug", "REPO updateExerciseInstructions local insert SUCCESS")

        if (currentUser != null) {
            firestoreRepo.update(updatedExercise)
            android.util.Log.d("ExerciseDebug", "REPO updateExerciseInstructions firestore update SUCCESS")
        }
    }
}