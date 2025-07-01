package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Template
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TemplateFirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getAllTemplates(): Flow<List<Template>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users/$userId/templates")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val templates = snapshot?.toObjects(Template::class.java) ?: emptyList()
                trySend(templates)
            }

        awaitClose { listener.remove() }
    }

    suspend fun getAllTemplatesOnce(): List<Template> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return firestore.collection("users/$userId/templates")
            .get()
            .await()
            .toObjects(Template::class.java)
    }

    suspend fun insert(template: Template) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/templates")
            .document(template.id)
            .set(template)
            .await()
    }

    suspend fun delete(templateId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/templates")
            .document(templateId)
            .delete()
            .await()
    }


    suspend fun update(template: Template) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/templates")
            .document(template.id)
            .set(template)
            .await()
    }
}