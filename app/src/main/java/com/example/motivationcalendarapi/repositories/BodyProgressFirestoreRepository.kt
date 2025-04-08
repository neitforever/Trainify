package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.BodyProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BodyProgressFirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getAllProgress(): Flow<List<BodyProgress>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users/$userId/bodyProgress")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val progresses = snapshot?.toObjects(BodyProgress::class.java) ?: emptyList()
                trySend(progresses)
            }

        awaitClose { listener.remove() }
    }



    suspend fun getAllProgressOnce(): List<BodyProgress> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return firestore.collection("users/$userId/bodyProgress")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(BodyProgress::class.java)
    }

    suspend fun insert(progress: BodyProgress) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/bodyProgress")
            .document(progress.id)
            .set(progress)
            .await()
    }

    suspend fun delete(progress: BodyProgress) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/bodyProgress")
            .document(progress.id)
            .delete()
            .await()
    }
}