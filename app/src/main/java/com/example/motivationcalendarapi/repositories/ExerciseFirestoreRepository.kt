package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.Exercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ExerciseFirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getLocalizedExercises(): List<Exercise> {
        val exercises = mutableListOf<Exercise>()
        firestore.collection("ex1").get().await().forEach { doc ->
            exercises.add(
                Exercise(
                    id = doc.id,
                    bodyPartLocalized = doc.get("bodyPart") as? Map<String, String> ?: emptyMap(),
                    nameLocalized = doc.get("name") as? Map<String, String> ?: emptyMap(),
                    equipmentLocalized = doc.get("equipment") as? Map<String, String> ?: emptyMap(),
                    targetLocalized = doc.get("target") as? Map<String, String> ?: emptyMap(),
                    secondaryMusclesLocalized = doc.get("secondaryMuscles") as? Map<String, List<String>> ?: emptyMap(),
                    instructionsLocalized = doc.get("instructions") as? Map<String, List<String>> ?: emptyMap(),
                    gifUrl = doc.getString("gifUrl") ?: "",
                    favorite = false,
                    note = ""
                )
            )
        }
        return exercises
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
//    fun getAllExercises(): Flow<List<Exercise>> = callbackFlow {
//        val userId = auth.currentUser?.uid ?: run {
//            trySend(emptyList())
//            close()
//            return@callbackFlow
//        }
//
//        val listener = firestore.collection("users/$userId/exercises")
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    close(error)
//                    return@addSnapshotListener
//                }
//                val exercises = snapshot?.toObjects(Exercise::class.java) ?: emptyList()
//                trySend(exercises)
//            }
//
//        awaitClose { listener.remove() }
//    }

    suspend fun getAllExercisesOnce(): List<Exercise> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return firestore.collection("users/$userId/exercises")
            .get()
            .await()
            .documents
            .map { doc ->
                doc.toObject(Exercise::class.java)!!.apply {
                    favorite = doc.getBoolean("favorite") == true
                }
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
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/exercises")
            .document(exercise.id)
            .set(exercise)
            .await()
    }

    suspend fun delete(exerciseId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/exercises")
            .document(exerciseId)
            .delete()
            .await()
    }

    suspend fun update(exercise: Exercise) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/exercises")
            .document(exercise.id)
            .set(exercise)
            .await()
    }
}