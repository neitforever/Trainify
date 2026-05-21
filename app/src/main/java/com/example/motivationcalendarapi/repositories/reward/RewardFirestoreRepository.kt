package com.example.motivationcalendarapi.repositories.reward

import com.example.motivationcalendarapi.model.reward.RewardEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RewardFirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getAllRewardsOnce(): List<RewardEntity> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return firestore.collection("users/$userId/rewards")
            .get()
            .await()
            .toObjects(RewardEntity::class.java)
    }

    suspend fun upsertReward(reward: RewardEntity) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/rewards")
            .document(reward.rewardId)
            .set(reward)
            .await()
    }

    suspend fun upsertRewards(rewards: List<RewardEntity>) {
        val userId = auth.currentUser?.uid ?: return
        val batch = firestore.batch()
        rewards.forEach { reward ->
            val ref = firestore.collection("users/$userId/rewards").document(reward.rewardId)
            batch.set(ref, reward)
        }
        batch.commit().await()
    }
}
