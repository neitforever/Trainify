package com.example.motivationcalendarapi.notifications

import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.repositories.ai.GeneratedTemplateDraft
import kotlinx.coroutines.flow.MutableSharedFlow

sealed class AiGenerationBackgroundEvent {
    data object ExerciseStarted : AiGenerationBackgroundEvent()
    data class ExerciseSuccess(val exercise: Exercise) : AiGenerationBackgroundEvent()
    data class ExerciseFailure(val message: String, val isNetworkError: Boolean, val isHighDemandError: Boolean) : AiGenerationBackgroundEvent()

    data object TemplateStarted : AiGenerationBackgroundEvent()
    data class TemplateSuccess(val draft: GeneratedTemplateDraft) : AiGenerationBackgroundEvent()
    data class TemplateFailure(val message: String, val isNetworkError: Boolean, val isHighDemandError: Boolean) : AiGenerationBackgroundEvent()
}

object AiGenerationBackgroundState {
    val events = MutableSharedFlow<AiGenerationBackgroundEvent>(extraBufferCapacity = 16)
}
