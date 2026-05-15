package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.Exercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ExerciseFirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getLocalizedExercises(): List<Exercise> {
        return firestore.collection("exercises")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Exercise::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun saveLocalizedExercisesForUser(exercises: List<Exercise>) {
        val userId = auth.currentUser?.uid ?: return
        val batch = firestore.batch()

        exercises.forEach { exercise ->
            val ref = firestore.collection("users/$userId/exercises").document(exercise.id)
            batch.set(ref, exercise)
        }

        batch.commit().await()
    }

    suspend fun getAllExercisesOnce(): List<Exercise> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return firestore.collection("users/$userId/exercises")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Exercise::class.java)?.copy(
                    id = doc.id,
                    favorite = doc.getBoolean("favorite") == true
                )
            }
    }

    suspend fun updateExerciseNote(exerciseId: String, newNote: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/exercises")
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

        firestore.collection("users/$userId/exercises")
            .document(exercise.id)
            .set(exercise)
            .await()

        android.util.Log.d("ExerciseDebug", "FS insert SUCCESS exerciseId=${exercise.id}")
    }

    suspend fun delete(exerciseId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/exercises")
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

        firestore.collection("users/$userId/exercises")
            .document(exercise.id)
            .set(exercise)
            .await()

        android.util.Log.d("ExerciseDebug", "FS update SUCCESS exerciseId=${exercise.id}")
    }
}