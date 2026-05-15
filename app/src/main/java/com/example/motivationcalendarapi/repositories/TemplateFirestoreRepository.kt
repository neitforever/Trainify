package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.Template
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TemplateFirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getDefaultTemplatesOnce(): List<Template> {
        return firestore
            .collection("templates")
            .get()
            .await()
            .toObjects(Template::class.java)
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

    suspend fun getAllTemplatesOnce(): List<Template> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return firestore
            .collection("users")
            .document(userId)
            .collection("templates")
            .get()
            .await()
            .toObjects(Template::class.java)
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