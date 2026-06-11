package com.example.motivationcalendarapi.model.technique_analysis

import android.net.Uri
import com.example.motivationcalendarapi.model.Exercise

data class ExerciseTechniqueAnalysisUiState(
    val activeExerciseId: String? = null,
    val selectedVideoUri: Uri? = null,
    val isPreparingVideo: Boolean = false,
    val isAnalyzing: Boolean = false,
    val loadingDots: String = ".",
    val result: ExerciseTechniqueAnalysisResult? = null,
    val matchedExercise: Exercise? = null,
    val errorMessage: String? = null
)

data class ExerciseTechniqueAnalysisResult(
    val isExpectedExercise: Boolean,
    val detectedExerciseName: String,
    val matchedExerciseId: String?,
    val confidence: Float,
    val overallVerdict: TechniqueVerdict,
    val score: Int,
    val summary: LocalizedText,
    val correctPoints: List<LocalizedText>,
    val mistakes: List<TechniqueIssue>,
    val safetyWarnings: List<LocalizedText>,
    val recommendations: List<LocalizedText>
)

data class TechniqueIssue(
    val title: LocalizedText,
    val description: LocalizedText,
    val severity: TechniqueIssueSeverity,
    val timeHint: String?
)

data class LocalizedText(
    val en: String = "",
    val ru: String = "",
    val be: String = ""
) {
    fun get(lang: String): String = when (lang) {
        "ru" -> ru.ifBlank { en }
        "be" -> be.ifBlank { en }
        else -> en.ifBlank { ru.ifBlank { be } }
    }
}

enum class TechniqueVerdict {
    GOOD,
    NEEDS_IMPROVEMENT,
    UNSAFE,
    WRONG_EXERCISE,
    UNCLEAR_VIDEO
}

enum class TechniqueIssueSeverity {
    LOW,
    MEDIUM,
    HIGH
}
