package com.example.motivationcalendarapi.model.analysis

import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.SetStatus

enum class ExerciseAnalysisPeriod {
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_90_DAYS
}

enum class ExerciseAnalysisViewMode {
    CHART,
    TABLE
}

enum class ExerciseAnalysisMetric {
    VOLUME,
    MAX_WEIGHT,
    ESTIMATED_ONE_REP_MAX,
    RELATIVE_STRENGTH,
    TIME,
    RESISTANCE,
    INCLINE
}

enum class ProjectionConfidence {
    LOW,
    MEDIUM,
    HIGH
}

data class ExerciseAnalysisSummary(
    val sessionsCount: Int = 0,
    val completedSetsCount: Int = 0,
    val totalVolume: Float = 0f,
    val maxWeight: Float = 0f,
    val maxReps: Int = 0,
    val bestEstimatedOneRepMax: Float? = null,
    val totalTime: Float = 0f,
    val maxResistance: Float = 0f,
    val maxIncline: Float = 0f,
    val bestRelativeStrengthPercent: Float? = null
)

data class ExerciseAnalysisRecord(
    val workoutId: String,
    val workoutName: String,
    val timestamp: Long,
    val setNumber: Int,
    val reps: Int,
    val weight: Float,
    val time: Float,
    val resistance: Float,
    val incline: Float,
    val volume: Float,
    val estimatedOneRepMax: Float?,
    val relativeStrengthPercent: Float?,
    val status: SetStatus
)

data class ExerciseAnalysisPoint(
    val timestamp: Long,
    val value: Float,
    val label: String,
    val isProjected: Boolean = false
)

data class ExerciseWeeklyProgression(
    val weekStartTimestamp: Long,
    val sessionsCount: Int,
    val totalVolume: Float,
    val bestWeight: Float,
    val bestEstimatedOneRepMax: Float?,
    val bestRelativeStrengthPercent: Float?,
    val totalTime: Float,
    val maxResistance: Float,
    val maxIncline: Float
)

data class ExerciseProjectedProgressPoint(
    val weekOffset: Int,
    val projectedValue: Float,
    val projectedRelativeStrengthPercent: Float?,
    val confidence: ProjectionConfidence
)

data class ExerciseAnalysisResult(
    val cardType: ExerciseCardType = ExerciseCardType.STRENGTH,
    val summary: ExerciseAnalysisSummary? = null,
    val chartPoints: List<ExerciseAnalysisPoint> = emptyList(),
    val weeklyProgression: List<ExerciseWeeklyProgression> = emptyList(),
    val projectedProgression: List<ExerciseProjectedProgressPoint> = emptyList(),
    val records: List<ExerciseAnalysisRecord> = emptyList(),
    val hasActualBodyWeight: Boolean = false,
    val projectionWindowDays: Int = 30,
    val projectionSourceDays: Int? = null
)

data class ExerciseAnalysisUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: ExerciseAnalysisPeriod = ExerciseAnalysisPeriod.LAST_30_DAYS,
    val selectedMetric: ExerciseAnalysisMetric = ExerciseAnalysisMetric.ESTIMATED_ONE_REP_MAX,
    val isProjectionExpanded: Boolean = false,
    val isOneRepMaxInfoExpanded: Boolean = false,
    val isWeeklyProgressionExpanded: Boolean = false,
    val showAllRecords: Boolean = false,
    val selectedViewMode: ExerciseAnalysisViewMode = ExerciseAnalysisViewMode.CHART,
    val result: ExerciseAnalysisResult = ExerciseAnalysisResult(),
    val errorMessage: String? = null
)
