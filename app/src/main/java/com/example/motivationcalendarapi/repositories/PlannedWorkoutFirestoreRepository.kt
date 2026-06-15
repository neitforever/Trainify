package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.planning.PlannedWorkout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PlannedWorkoutFirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getAllPlannedWorkouts(): Flow<List<PlannedWorkout>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users/$userId/planned_workouts")
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(PlannedWorkout::class.java) ?: emptyList())
            }

        awaitClose { listener.remove() }
    }

    suspend fun getAllPlannedWorkoutsOnce(): List<PlannedWorkout> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return firestore.collection("users/$userId/planned_workouts")
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .await()
            .toObjects(PlannedWorkout::class.java)
    }

    suspend fun insert(plannedWorkout: PlannedWorkout) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/planned_workouts")
            .document(plannedWorkout.id)
            .set(plannedWorkout)
            .await()
    }

    suspend fun update(plannedWorkout: PlannedWorkout) = insert(plannedWorkout)

    suspend fun delete(id: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/planned_workouts")
            .document(id)
            .delete()
            .await()
    }
}
