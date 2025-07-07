package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.model.BodyProgress
import com.google.firebase.auth.FirebaseAuth
import com.example.motivationcalendarapi.database.WorkoutDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BodyProgressRepository(
    private val database: WorkoutDatabase,
    private val firestoreRepo: BodyProgressFirestoreRepository,
    private val auth: FirebaseAuth
) {
    private val currentUser get() = auth.currentUser

    fun getAllProgress(): Flow<List<BodyProgress>> {
        return if (currentUser != null) {
            firestoreRepo.getAllProgress()
        } else {
            database.bodyProgressDao().getAllProgress()
        }
    }

    suspend fun insert(progress: BodyProgress) {
        if (currentUser != null) {
            firestoreRepo.insert(progress)
            database.bodyProgressDao().insert(progress)
        } else {
            database.bodyProgressDao().insert(progress)
        }
    }

    suspend fun delete(progress: BodyProgress) {
        if (currentUser != null) {
            firestoreRepo.delete(progress)
            database.bodyProgressDao().delete(progress)
        } else {
            database.bodyProgressDao().delete(progress)
        }
    }
    suspend fun syncWithFirestore() {
        if (currentUser == null) return

        val remoteData = firestoreRepo.getAllProgressOnce()

        database.bodyProgressDao().deleteAll()

        remoteData.forEach {
            database.bodyProgressDao().insert(it)
        }

        val localData = database.bodyProgressDao().getAllProgress().first()
        localData.forEach {
            firestoreRepo.insert(it)
        }
    }



    suspend fun deleteAll() {
        database.bodyProgressDao().deleteAll()
    }
}