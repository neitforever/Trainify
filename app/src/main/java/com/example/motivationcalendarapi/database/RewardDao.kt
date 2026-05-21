package com.example.motivationcalendarapi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motivationcalendarapi.model.reward.RewardEntity
import com.example.motivationcalendarapi.model.reward.RewardUnlockEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: RewardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(rewards: List<RewardEntity>)

    @Query("SELECT * FROM reward_table ORDER BY rewardId")
    fun observeRewards(): Flow<List<RewardEntity>>

    @Query("SELECT * FROM reward_table")
    suspend fun getRewardsOnce(): List<RewardEntity>

    @Query("SELECT * FROM reward_table WHERE rewardId = :rewardId LIMIT 1")
    suspend fun getRewardById(rewardId: String): RewardEntity?

    @Query("DELETE FROM reward_table")
    suspend fun deleteAllRewards()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnlockEvent(event: RewardUnlockEventEntity)

    @Query("SELECT * FROM reward_unlock_event_table WHERE shown = 0 ORDER BY createdAt ASC")
    fun observePendingUnlockEvents(): Flow<List<RewardUnlockEventEntity>>

    @Query("UPDATE reward_unlock_event_table SET shown = 1 WHERE eventId = :eventId")
    suspend fun markUnlockEventShown(eventId: String)

    @Query("DELETE FROM reward_unlock_event_table")
    suspend fun deleteAllUnlockEvents()
}
