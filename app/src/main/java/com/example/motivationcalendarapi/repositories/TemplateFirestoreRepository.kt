package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.Template
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class TemplateFirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun awaitCurrentUserId(timeoutMillis: Long = 15_000L): String? {
        val startedAt = System.currentTimeMillis()
        while (System.currentTimeMillis() - startedAt < timeoutMillis) {
            auth.currentUser?.uid?.let { uid ->
                Log.d("FirestoreFallback", "Auth restored for templates. uid=$uid")
                return uid
            }
            delay(250L)
        }

        val uid = auth.currentUser?.uid
        Log.d("FirestoreFallback", "Auth wait finished for templates. uid=$uid")
        return uid
    }

    suspend fun getDefaultTemplatesOnce(): List<Template> {
        return firestore
            .collection("templates")
            .get(Source.SERVER)
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Template::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun saveDefaultTemplatesForUser(templates: List<Template>) {
        val userId = auth.currentUser?.uid ?: return

        val batch = firestore.batch()

        templates.forEach { template ->
            val ref = firestore
                .collection("users")
                .document(userId)
                .collection("templates")
                .document(template.id)

            batch.set(ref, template)
        }

        batch.commit().await()
    }

    suspend fun getAllTemplatesOnce(waitForAuth: Boolean = false): List<Template> {
        val userId = (if (waitForAuth) awaitCurrentUserId() else auth.currentUser?.uid)
            ?: run {
                Log.d("FirestoreFallback", "Skip user templates read: userId is null")
                return emptyList()
            }

        Log.d("FirestoreFallback", "Read user templates path=users/$userId/templates")

        return firestore
            .collection("users")
            .document(userId)
            .collection("templates")
            .get(Source.SERVER)
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Template::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun insert(template: Template) {
        val userId = auth.currentUser?.uid ?: return

        firestore
            .collection("users")
            .document(userId)
            .collection("templates")
            .document(template.id)
            .set(template)
            .await()
    }

    suspend fun delete(templateId: String) {
        val userId = auth.currentUser?.uid ?: return

        firestore
            .collection("users")
            .document(userId)
            .collection("templates")
            .document(templateId)
            .delete()
            .await()
    }

    suspend fun update(template: Template) {
        val userId = auth.currentUser?.uid ?: return

        firestore
            .collection("users")
            .document(userId)
            .collection("templates")
            .document(template.id)
            .set(template)
            .await()
    }
}
