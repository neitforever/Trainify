package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WorkoutFirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getAllWorkouts(): Flow<List<Workout>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users/$userId/workouts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val workouts = snapshot?.toObjects(Workout::class.java) ?: emptyList()
                trySend(workouts)
            }

        awaitClose { listener.remove() }
    }

    suspend fun getAllWorkoutsOnce(): List<Workout> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return firestore.collection("users/$userId/workouts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Workout::class.java)
    }

    suspend fun insert(workout: Workout) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/workouts")
            .document(workout.id.toString())
            .set(workout)
            .await()
    }

    suspend fun delete(workout: Workout) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/workouts")
            .document(workout.id.toString())
            .delete()
            .await()
    }

    suspend fun update(workout: Workout) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/workouts")
            .document(workout.id.toString())
            .set(workout)
            .await()
    }
}