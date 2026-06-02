package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.Exercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class ExerciseFirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun awaitCurrentUserId(timeoutMillis: Long = 15_000L): String? {
        val startedAt = System.currentTimeMillis()
        while (System.currentTimeMillis() - startedAt < timeoutMillis) {
            auth.currentUser?.uid?.let { uid ->
                Log.d("FirestoreFallback", "Auth restored for exercises. uid=$uid")
                return uid
            }
            delay(250L)
        }

        val uid = auth.currentUser?.uid
        Log.d("FirestoreFallback", "Auth wait finished for exercises. uid=$uid")
        return uid
    }

    suspend fun getRootExercisesOnce(): List<Exercise> {
        return firestore.collection("exercises")
            .get(Source.SERVER)
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Exercise::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun getLocalizedExercises(): List<Exercise> = getRootExercisesOnce()

    suspend fun getUserExercisesOnce(waitForAuth: Boolean = false): List<Exercise> {
        val userId = (if (waitForAuth) awaitCurrentUserId() else auth.currentUser?.uid)
            ?: run {
                Log.d("FirestoreFallback", "Skip user exercises read: userId is null")
                return emptyList()
            }

        Log.d("FirestoreFallback", "Read user exercises path=users/$userId/exercises")

        return firestore
            .collection("users")
            .document(userId)
            .collection("exercises")
            .get(Source.SERVER)
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Exercise::class.java)?.copy(
                    id = doc.id,
                    favorite = doc.getBoolean("favorite") == true
                )
            }
    }

    suspend fun saveLocalizedExercisesForUser(exercises: List<Exercise>) {
        val userId = auth.currentUser?.uid ?: return
        val batch = firestore.batch()

        exercises.forEach { exercise ->
            val ref = firestore
                .collection("users")
                .document(userId)
                .collection("exercises")
                .document(exercise.id)
            batch.set(ref, exercise)
        }

        batch.commit().await()
    }

    suspend fun getAllExercisesOnce(): List<Exercise> = getUserExercisesOnce(waitForAuth = false)

    suspend fun updateExerciseNote(exerciseId: String, newNote: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).collection("exercises")
            .document(exerciseId)
            .update("note", newNote)
            .await()
    }

    suspend fun insert(exercise: Exercise) {
        val userId = auth.currentUser?.uid ?: run {
            android.util.Log.e("ExerciseDebug", "FS insert ERROR userId is null")
            return
        }

        android.util.Log.d("ExerciseDebug", "FS insert START userId=$userId exerciseId=${exercise.id}")

        firestore.collection("users").document(userId).collection("exercises")
            .document(exercise.id)
            .set(exercise)
            .await()

        android.util.Log.d("ExerciseDebug", "FS insert SUCCESS exerciseId=${exercise.id}")
    }

    suspend fun delete(exerciseId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).collection("exercises")
            .document(exerciseId)
            .delete()
            .await()
    }

    suspend fun update(exercise: Exercise) {
        val userId = auth.currentUser?.uid ?: run {
            android.util.Log.e("ExerciseDebug", "FS update ERROR userId is null")
            return
        }

        android.util.Log.d("ExerciseDebug", "FS update START userId=$userId exerciseId=${exercise.id}")

        firestore.collection("users").document(userId).collection("exercises")
            .document(exercise.id)
            .set(exercise)
            .await()

        android.util.Log.d("ExerciseDebug", "FS update SUCCESS exerciseId=${exercise.id}")
    }
}