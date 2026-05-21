package com.example.motivationcalendarapi.repositories.reward

import com.example.motivationcalendarapi.database.WorkoutDatabase
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.model.reward.RewardDefinitions
import com.example.motivationcalendarapi.model.reward.RewardEntity
import com.example.motivationcalendarapi.model.reward.RewardTier
import com.example.motivationcalendarapi.model.reward.RewardType
import com.example.motivationcalendarapi.model.reward.RewardUiModel
import com.example.motivationcalendarapi.model.reward.RewardUnlockEventEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class RewardRepository(
    private val appDatabase: WorkoutDatabase,
    private val firestoreRepository: RewardFirestoreRepository,
    private val auth: FirebaseAuth
) {
    private val rewardDao = appDatabase.rewardDao()

    fun observeRewards(): Flow<List<RewardUiModel>> {
        return rewardDao.observeRewards().map { localRewards ->
            val byId = localRewards.associateBy { it.rewardId }
            RewardDefinitions.all.map { definition ->
                val entity = byId[definition.id] ?: emptyEntity(definition.id, definition.type.name, definition.exerciseId)
                val tier = entity.currentTier?.let { runCatching { RewardTier.valueOf(it) }.getOrNull() }
                val nextTarget = definition.targetForNextTier(tier)
                RewardUiModel(
                    entity = entity,
                    definition = definition,
                    currentTier = tier,
                    progressToNextTier = calculateProgressToNextTier(entity.progress, definition.thresholds[RewardTier.WOOD] ?: 1.0, nextTarget),
                    targetForNextTier = nextTarget
                )
            }
        }
    }

    fun observePendingUnlockEvents(): Flow<List<RewardUnlockEventEntity>> = rewardDao.observePendingUnlockEvents()

    suspend fun initializeRewards() {
        val existing = rewardDao.getRewardsOnce().associateBy { it.rewardId }
        val now = System.currentTimeMillis()
        val missing = RewardDefinitions.all
            .filterNot { existing.containsKey(it.id) }
            .map { emptyEntity(it.id, it.type.name, it.exerciseId, now) }
        if (missing.isNotEmpty()) rewardDao.insertRewards(missing)
    }

    suspend fun syncFromFirestore() {
        if (auth.currentUser == null) return
        initializeRewards()
        val remote = firestoreRepository.getAllRewardsOnce()
        val local = rewardDao.getRewardsOnce()
        val merged = mergeRewards(local, remote)
        rewardDao.insertRewards(merged)
        firestoreRepository.upsertRewards(merged)
    }

    suspend fun evaluateAfterWorkout(workout: Workout) {
        initializeRewards()
        val workouts = appDatabase.workoutDao().getAllWorkoutsOnce()
        evaluateTotalWorkouts(workouts)
        evaluateWorkoutTotalVolume(workout)
        evaluateWorkoutTotalReps(workout)
        evaluateExerciseMaxWeight(workouts)
        evaluateWorkoutStreak(workouts)
        evaluateWeeklyConsistency(workouts)
    }

    suspend fun evaluateDailySteps(steps: Long?) {
        if (steps == null) return
        initializeRewards()
        updateRewardProgress("daily_steps", steps.toDouble())
    }

    suspend fun evaluateBodyProgressEntries(count: Int) {
        initializeRewards()
        updateRewardProgress("body_progress_entries", count.toDouble())
    }

    suspend fun increaseAiExerciseCreated() {
        initializeRewards()
        incrementRewardProgress("ai_exercise_created")
    }

    suspend fun increaseAiTemplateCreated() {
        initializeRewards()
        incrementRewardProgress("ai_template_created")
    }

    suspend fun unlockHealthConnectConnected() {
        initializeRewards()
        updateRewardProgress("health_connect_connected", 1.0)
    }

    suspend fun unlockEquipmentRecognizerUsed() {
        initializeRewards()
        updateRewardProgress("equipment_recognizer_used", 1.0)
    }

    suspend fun markUnlockEventShown(eventId: String) = rewardDao.markUnlockEventShown(eventId)

    private suspend fun evaluateTotalWorkouts(workouts: List<Workout>) {
        updateRewardProgress("total_workouts", workouts.size.toDouble())
    }

    private suspend fun evaluateWorkoutTotalVolume(workout: Workout) {
        val totalVolume = workout.exercises.sumOf { exercise ->
            exercise.sets
                .filter { it.status != SetStatus.FAILED }
                .sumOf { (it.weight * it.rep).toDouble() }
        }
        updateRewardProgress("workout_total_volume", totalVolume)
    }

    private suspend fun evaluateWorkoutTotalReps(workout: Workout) {
        val totalReps = workout.exercises.sumOf { exercise ->
            exercise.sets
                .filter { it.status != SetStatus.FAILED }
                .sumOf { it.rep }
        }
        updateRewardProgress("workout_total_reps", totalReps.toDouble())
    }

    private suspend fun evaluateExerciseMaxWeight(workouts: List<Workout>) {
        RewardDefinitions.all
            .filter { it.type == RewardType.EXERCISE_MAX_WEIGHT && it.exerciseId != null }
            .forEach { definition ->
                val maxWeight = workouts
                    .flatMap { it.exercises }
                    .filter { it.exercise.id == definition.exerciseId }
                    .flatMap { it.sets }
                    .filter { it.status != SetStatus.FAILED }
                    .maxOfOrNull { it.weight.toDouble() } ?: 0.0
                updateRewardProgress(definition.id, maxWeight)
            }
    }

    private suspend fun evaluateWorkoutStreak(workouts: List<Workout>) {
        val dates = workouts.map { timestampToDate(it.timestamp) }.toSet()
        var streak = 0
        var cursor = LocalDate.now()
        while (dates.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }
        updateRewardProgress("workout_streak", streak.toDouble())
    }

    private suspend fun evaluateWeeklyConsistency(workouts: List<Workout>) {
        val today = LocalDate.now()
        val start = today.with(DayOfWeek.MONDAY)
        val end = start.plusDays(6)
        val weeklyCount = workouts.count { timestampToDate(it.timestamp) in start..end }
        updateRewardProgress("weekly_consistency", weeklyCount.toDouble())
    }

    private suspend fun incrementRewardProgress(rewardId: String, delta: Double = 1.0) {
        val old = rewardDao.getRewardById(rewardId)
        updateRewardProgress(rewardId, (old?.progress ?: 0.0) + delta, keepMaximumProgress = false)
    }

    private suspend fun updateRewardProgress(rewardId: String, newProgress: Double, keepMaximumProgress: Boolean = true) {
        val definition = RewardDefinitions.byId(rewardId) ?: return
        val old = rewardDao.getRewardById(rewardId) ?: emptyEntity(rewardId, definition.type.name, definition.exerciseId)
        val progress = if (keepMaximumProgress) maxOf(old.progress, newProgress) else newProgress
        val newTier = definition.resolveTier(progress)
        val oldTier = old.currentTier?.let { runCatching { RewardTier.valueOf(it) }.getOrNull() }
        val now = System.currentTimeMillis()
        val target = definition.targetForNextTier(newTier) ?: (definition.thresholds[RewardTier.PLATINUM] ?: progress)
        val updated = old.copy(
            type = definition.type.name,
            exerciseId = definition.exerciseId,
            currentTier = newTier?.name,
            progress = progress,
            target = target,
            unlockedAt = if (old.unlockedAt == null && newTier != null) now else old.unlockedAt,
            updatedAt = now,
            isUnlocked = newTier != null
        )
        rewardDao.insertReward(updated)
        if (newTier != null && newTier.order > (oldTier?.order ?: 0)) {
            rewardDao.insertUnlockEvent(
                RewardUnlockEventEntity(
                    eventId = UUID.randomUUID().toString(),
                    rewardId = rewardId,
                    tier = newTier.name,
                    createdAt = now,
                    shown = false
                )
            )
        }
        if (auth.currentUser != null) firestoreRepository.upsertReward(updated)
    }

    private fun mergeRewards(local: List<RewardEntity>, remote: List<RewardEntity>): List<RewardEntity> {
        val localById = local.associateBy { it.rewardId }
        val remoteById = remote.associateBy { it.rewardId }
        return RewardDefinitions.all.map { definition ->
            val localReward = localById[definition.id]
            val remoteReward = remoteById[definition.id]
            when {
                localReward == null && remoteReward == null -> emptyEntity(definition.id, definition.type.name, definition.exerciseId)
                localReward == null -> remoteReward!!
                remoteReward == null -> localReward
                else -> if (localReward.progress >= remoteReward.progress) localReward else remoteReward
            }
        }
    }

    private fun emptyEntity(
        rewardId: String,
        type: String,
        exerciseId: String?,
        updatedAt: Long = System.currentTimeMillis()
    ): RewardEntity = RewardEntity(
        rewardId = rewardId,
        type = type,
        exerciseId = exerciseId,
        currentTier = null,
        progress = 0.0,
        target = 0.0,
        unlockedAt = null,
        updatedAt = updatedAt,
        isUnlocked = false
    )

    private fun calculateProgressToNextTier(progress: Double, firstTarget: Double, nextTarget: Double?): Float {
        val target = nextTarget ?: return 1f
        return (progress / target).coerceIn(0.0, 1.0).toFloat()
    }

    private fun timestampToDate(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}
