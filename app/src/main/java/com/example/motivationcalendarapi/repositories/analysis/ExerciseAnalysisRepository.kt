package com.example.motivationcalendarapi.repositories.analysis

import com.example.motivationcalendarapi.model.BodyProgress
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseCardTypeCatalog
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisMetric
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisPeriod
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisPoint
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisRecord
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisResult
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisSummary
import com.example.motivationcalendarapi.model.analysis.ExerciseProjectedProgressPoint
import com.example.motivationcalendarapi.model.analysis.ExerciseWeeklyProgression
import com.example.motivationcalendarapi.model.analysis.ProjectionConfidence
import com.example.motivationcalendarapi.repositories.BodyProgressRepository
import com.example.motivationcalendarapi.repositories.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import kotlin.math.abs

class ExerciseAnalysisRepository(
    private val workoutRepository: WorkoutRepository,
    private val bodyProgressRepository: BodyProgressRepository
) {

    fun observeExerciseAnalysis(
        exerciseId: String,
        period: ExerciseAnalysisPeriod,
        metric: ExerciseAnalysisMetric
    ): Flow<ExerciseAnalysisResult> {
        return combine(
            workoutRepository.getAllWorkouts(),
            bodyProgressRepository.getAllProgress()
        ) { workouts, bodyProgress ->
            buildAnalysis(
                exerciseId = exerciseId,
                period = period,
                metric = metric,
                workouts = workouts,
                bodyProgressList = bodyProgress
            )
        }
    }

    private fun buildAnalysis(
        exerciseId: String,
        period: ExerciseAnalysisPeriod,
        metric: ExerciseAnalysisMetric,
        workouts: List<Workout>,
        bodyProgressList: List<BodyProgress>
    ): ExerciseAnalysisResult {
        val now = System.currentTimeMillis()
        val fromTimestamp = period.toStartTimestamp(now)
        val actualBodyWeight = bodyProgressList
            .filter { it.weight > 0.0 }
            .maxByOrNull { it.timestamp }
            ?.takeIf { now - it.timestamp <= THIRTY_DAYS_MILLIS }
            ?.weight
            ?.toFloat()

        val periodWorkouts = workouts
            .filter { it.timestamp > 0L }
            .filter { it.timestamp >= fromTimestamp }
            .sortedBy { it.timestamp }

        val allExerciseWorkouts = workouts
            .filter { it.timestamp > 0L }
            .sortedBy { it.timestamp }

        val records = buildRecords(exerciseId, periodWorkouts, actualBodyWeight)
        val cardType = detectCardType(exerciseId, periodWorkouts, allExerciseWorkouts)
        val summary = buildSummary(records)
        val weekly = buildWeeklyProgression(records)
        val projected = if (cardType == ExerciseCardType.STRENGTH) {
            buildProjection(
                exerciseId = exerciseId,
                workouts = allExerciseWorkouts,
                actualBodyWeight = actualBodyWeight,
                now = now
            )
        } else {
            emptyList()
        }
        val projectionSourceDays = if (projected.isNotEmpty()) {
            if (buildRecords(exerciseId, allExerciseWorkouts.filter { it.timestamp >= now - THIRTY_DAYS_MILLIS }, actualBodyWeight)
                    .map { it.workoutId }.distinct().size >= MIN_PROJECTION_SESSIONS
            ) 30 else 90
        } else null

        return ExerciseAnalysisResult(
            cardType = cardType,
            summary = summary.takeIf { records.isNotEmpty() },
            chartPoints = buildChartPoints(records, metric, cardType),
            weeklyProgression = weekly,
            projectedProgression = projected,
            records = records.sortedByDescending { it.timestamp },
            hasActualBodyWeight = actualBodyWeight != null,
            projectionWindowDays = 30,
            projectionSourceDays = projectionSourceDays
        )
    }

    private fun buildRecords(
        exerciseId: String,
        workouts: List<Workout>,
        actualBodyWeight: Float?
    ): List<ExerciseAnalysisRecord> {
        val records = mutableListOf<ExerciseAnalysisRecord>()

        workouts.forEach { workout ->
            workout.exercises.forEach { extendedExercise ->
                if (extendedExercise.exercise.id == exerciseId) {
                    extendedExercise.sets.forEachIndexed { index, set ->
                        if (set.isValidForAnalysis()) {
                            val volume = if (set.weight > 0f && set.rep > 0) set.weight * set.rep else 0f
                            val estimatedOneRepMax = if (set.weight > 0f && set.rep > 0) {
                                set.weight * (1f + set.rep / 30f)
                            } else null
                            val relativeStrengthPercent = if (
                                actualBodyWeight != null && actualBodyWeight > 0f && estimatedOneRepMax != null
                            ) {
                                estimatedOneRepMax / actualBodyWeight * 100f
                            } else null

                            records.add(
                                ExerciseAnalysisRecord(
                                    workoutId = workout.id,
                                    workoutName = workout.name,
                                    timestamp = workout.timestamp,
                                    setNumber = index + 1,
                                    reps = set.rep,
                                    weight = set.weight,
                                    time = set.time,
                                    resistance = set.resistance,
                                    incline = set.incline,
                                    volume = volume,
                                    estimatedOneRepMax = estimatedOneRepMax,
                                    relativeStrengthPercent = relativeStrengthPercent,
                                    status = set.status
                                )
                            )
                        }
                    }
                }
            }
        }

        return records
    }

    private fun detectCardType(
        exerciseId: String,
        periodWorkouts: List<Workout>,
        allWorkouts: List<Workout>
    ): ExerciseCardType {
        val exercise = (periodWorkouts + allWorkouts)
            .flatMap { it.exercises }
            .firstOrNull { it.exercise.id == exerciseId }
            ?.exercise
        return ExerciseCardTypeCatalog.parse(exercise?.cardType)
    }

    private fun buildSummary(records: List<ExerciseAnalysisRecord>): ExerciseAnalysisSummary {
        return ExerciseAnalysisSummary(
            sessionsCount = records.map { it.workoutId }.distinct().size,
            completedSetsCount = records.size,
            totalVolume = records.sumOf { it.volume.toDouble() }.toFloat(),
            maxWeight = records.maxOfOrNull { it.weight } ?: 0f,
            maxReps = records.maxOfOrNull { it.reps } ?: 0,
            bestEstimatedOneRepMax = records.mapNotNull { it.estimatedOneRepMax }.maxOrNull(),
            totalTime = records.sumOf { it.time.toDouble() }.toFloat(),
            maxResistance = records.maxOfOrNull { it.resistance } ?: 0f,
            maxIncline = records.maxOfOrNull { it.incline } ?: 0f,
            bestRelativeStrengthPercent = records.mapNotNull { it.relativeStrengthPercent }.maxOrNull()
        )
    }

    private fun buildChartPoints(
        records: List<ExerciseAnalysisRecord>,
        metric: ExerciseAnalysisMetric,
        cardType: ExerciseCardType
    ): List<ExerciseAnalysisPoint> {
        val points = records
            .groupBy { it.workoutId }
            .values
            .mapNotNull { workoutRecords ->
                val first = workoutRecords.firstOrNull() ?: return@mapNotNull null
                val value = when (metric) {
                    ExerciseAnalysisMetric.VOLUME -> workoutRecords.sumOf { it.volume.toDouble() }.toFloat()
                    ExerciseAnalysisMetric.MAX_WEIGHT -> workoutRecords.maxOfOrNull { it.weight } ?: 0f
                    ExerciseAnalysisMetric.ESTIMATED_ONE_REP_MAX -> workoutRecords.mapNotNull { it.estimatedOneRepMax }.maxOrNull() ?: 0f
                    ExerciseAnalysisMetric.RELATIVE_STRENGTH -> workoutRecords.mapNotNull { it.relativeStrengthPercent }.maxOrNull() ?: 0f
                    ExerciseAnalysisMetric.TIME -> workoutRecords.sumOf { it.time.toDouble() }.toFloat()
                    ExerciseAnalysisMetric.RESISTANCE -> workoutRecords.maxOfOrNull { it.resistance } ?: 0f
                    ExerciseAnalysisMetric.INCLINE -> workoutRecords.maxOfOrNull { it.incline } ?: 0f
                }
                if (value <= 0f && cardType == ExerciseCardType.STRENGTH) return@mapNotNull null
                ExerciseAnalysisPoint(
                    timestamp = first.timestamp,
                    value = value,
                    label = first.workoutName.ifBlank { "Workout" }
                )
            }
            .sortedBy { it.timestamp }

        return if (cardType == ExerciseCardType.STRENGTH &&
            (metric == ExerciseAnalysisMetric.ESTIMATED_ONE_REP_MAX || metric == ExerciseAnalysisMetric.MAX_WEIGHT)
        ) {
            removeLargeDropAnomalies(points)
        } else {
            points
        }
    }

    private fun removeLargeDropAnomalies(points: List<ExerciseAnalysisPoint>): List<ExerciseAnalysisPoint> {
        if (points.size < 3) return points
        val indexesToRemove = mutableSetOf<Int>()
        for (index in 1 until points.size) {
            val previous = points[index - 1]
            val current = points[index]
            if (abs(current.value - previous.value) >= CHART_ANOMALY_DELTA_KG) {
                if (current.value < previous.value) indexesToRemove.add(index) else indexesToRemove.add(index - 1)
            }
        }
        val filtered = points.filterIndexed { index, _ -> index !in indexesToRemove }
        return if (filtered.size >= 2) filtered else points
    }

    private fun buildWeeklyProgression(records: List<ExerciseAnalysisRecord>): List<ExerciseWeeklyProgression> {
        return records
            .groupBy { getWeekStartTimestamp(it.timestamp) }
            .map { (weekStart, weekRecords) ->
                ExerciseWeeklyProgression(
                    weekStartTimestamp = weekStart,
                    sessionsCount = weekRecords.map { it.workoutId }.distinct().size,
                    totalVolume = weekRecords.sumOf { it.volume.toDouble() }.toFloat(),
                    bestWeight = weekRecords.maxOfOrNull { it.weight } ?: 0f,
                    bestEstimatedOneRepMax = weekRecords.mapNotNull { it.estimatedOneRepMax }.maxOrNull(),
                    bestRelativeStrengthPercent = weekRecords.mapNotNull { it.relativeStrengthPercent }.maxOrNull(),
                    totalTime = weekRecords.sumOf { it.time.toDouble() }.toFloat(),
                    maxResistance = weekRecords.maxOfOrNull { it.resistance } ?: 0f,
                    maxIncline = weekRecords.maxOfOrNull { it.incline } ?: 0f
                )
            }
            .sortedBy { it.weekStartTimestamp }
    }

    private fun buildProjection(
        exerciseId: String,
        workouts: List<Workout>,
        actualBodyWeight: Float?,
        now: Long
    ): List<ExerciseProjectedProgressPoint> {
        val thirtyDayRecords = buildRecords(
            exerciseId = exerciseId,
            workouts = workouts.filter { it.timestamp >= now - THIRTY_DAYS_MILLIS },
            actualBodyWeight = actualBodyWeight
        )
        val sourceRecords = if (thirtyDayRecords.map { it.workoutId }.distinct().size >= MIN_PROJECTION_SESSIONS) {
            thirtyDayRecords
        } else {
            buildRecords(
                exerciseId = exerciseId,
                workouts = workouts.filter { it.timestamp >= now - NINETY_DAYS_MILLIS },
                actualBodyWeight = actualBodyWeight
            )
        }

        val weeklyValues = buildWeeklyProgression(sourceRecords)
            .mapNotNull { week ->
                week.bestEstimatedOneRepMax?.takeIf { it > 0f }?.let { week.weekStartTimestamp to it }
            }
            .sortedBy { it.first }

        val sessionValues = sourceRecords
            .groupBy { it.workoutId }
            .values
            .mapNotNull { sessionRecords ->
                val timestamp = sessionRecords.firstOrNull()?.timestamp ?: return@mapNotNull null
                val value = sessionRecords.mapNotNull { it.estimatedOneRepMax }.maxOrNull() ?: return@mapNotNull null
                timestamp to value
            }
            .sortedBy { it.first }

        val sessionsCount = sourceRecords.map { it.workoutId }.distinct().size
        if (sessionsCount < MIN_PROJECTION_SESSIONS || sessionValues.size < MIN_PROJECTION_SESSIONS) return emptyList()

        val trendValues = if (weeklyValues.size >= MIN_PROJECTION_WEEKS) weeklyValues else sessionValues
        val deltas = trendValues.zipWithNext { previous, current -> current.second - previous.second }
        val positiveDeltas = deltas.filter { it > 0f }.sorted()
        if (positiveDeltas.isEmpty()) return emptyList()

        val rawDelta = positiveDeltas.median()
        val currentValue = trendValues.last().second
        val maxWeeklyPercentDelta = currentValue * MAX_WEEKLY_INCREASE_PERCENT
        val maxWeeklyDelta = minOf(maxWeeklyPercentDelta, MAX_ABSOLUTE_WEEKLY_INCREASE_KG)
        val dampingFactor = when {
            sessionsCount <= 3 -> 0.40f
            sessionsCount == 4 -> 0.55f
            sessionsCount == 5 -> 0.70f
            else -> 0.85f
        }
        val safeDelta = (rawDelta * dampingFactor).coerceIn(0f, maxWeeklyDelta)
        if (safeDelta <= 0f) return emptyList()

        val volatility = positiveDeltas.map { abs(it - rawDelta) }.average().toFloat()
        val confidence = when {
            sessionsCount >= 5 && volatility <= safeDelta -> ProjectionConfidence.HIGH
            sessionsCount >= MIN_PROJECTION_SESSIONS -> ProjectionConfidence.MEDIUM
            else -> ProjectionConfidence.LOW
        }
        if (confidence == ProjectionConfidence.LOW) return emptyList()

        return (1..4).map { weekOffset ->
            val projected = currentValue + safeDelta * weekOffset
            ExerciseProjectedProgressPoint(
                weekOffset = weekOffset,
                projectedValue = projected,
                projectedRelativeStrengthPercent = actualBodyWeight
                    ?.takeIf { it > 0f }
                    ?.let { projected / it * 100f },
                confidence = confidence
            )
        }
    }

    private fun List<Float>.median(): Float {
        if (isEmpty()) return 0f
        val middle = size / 2
        return if (size % 2 == 0) (this[middle - 1] + this[middle]) / 2f else this[middle]
    }

    private fun SetStatus.isPositiveCompletion(): Boolean = this == SetStatus.COMPLETED || this == SetStatus.NONE

    private fun com.example.motivationcalendarapi.model.ExerciseSet.isValidForAnalysis(): Boolean {
        if (!status.isPositiveCompletion()) return false
        return rep > 0 || weight > 0f || time > 0f || resistance > 0f || incline > 0f
    }

    private fun ExerciseAnalysisPeriod.toStartTimestamp(now: Long): Long {
        return when (this) {
            ExerciseAnalysisPeriod.LAST_7_DAYS -> now - 7L * DAY_MILLIS
            ExerciseAnalysisPeriod.LAST_30_DAYS -> now - THIRTY_DAYS_MILLIS
            ExerciseAnalysisPeriod.LAST_90_DAYS -> now - NINETY_DAYS_MILLIS
        }
    }

    private fun getWeekStartTimestamp(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    companion object {
        private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
        private const val THIRTY_DAYS_MILLIS = 30L * DAY_MILLIS
        private const val NINETY_DAYS_MILLIS = 90L * DAY_MILLIS
        private const val MIN_PROJECTION_SESSIONS = 3
        private const val MIN_PROJECTION_WEEKS = 2
        private const val MAX_WEEKLY_INCREASE_PERCENT = 0.02f
        private const val MAX_ABSOLUTE_WEEKLY_INCREASE_KG = 2.5f
        private const val CHART_ANOMALY_DELTA_KG = 15f
    }
}
