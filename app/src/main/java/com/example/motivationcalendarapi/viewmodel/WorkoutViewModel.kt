package com.example.motivationcalendarapi.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.motivationcalendarapi.model.DifficultyLevel
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.model.newSupersetGroupId
import com.example.motivationcalendarapi.model.toDefaultClusterSet
import com.example.motivationcalendarapi.model.toDefaultDropSet
import com.example.motivationcalendarapi.model.toNormalSet
import com.example.motivationcalendarapi.model.planning.PlannedWorkout
import com.example.motivationcalendarapi.model.planning.PlannedWorkoutSourceType
import com.example.motivationcalendarapi.model.reward.RewardUiModel
import com.example.motivationcalendarapi.model.reward.RewardUnlockEventEntity
import com.example.motivationcalendarapi.model.getCardType
import com.example.motivationcalendarapi.repositories.MainRepository
import com.example.motivationcalendarapi.repositories.ActiveWorkoutDraft
import com.example.motivationcalendarapi.repositories.TimerDataStore
import com.example.motivationcalendarapi.repositories.WorkoutRepository
import com.example.motivationcalendarapi.repositories.ai.GeminiAiGenerationApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.LocalTime
import java.util.Calendar

class WorkoutViewModel(
    val workoutRepository: WorkoutRepository,
    private val savedStateHandle: SavedStateHandle,
    private val timerDataStore: TimerDataStore,
    mainRepository: MainRepository,
) : ViewModel() {

    private val geminiAiGenerationApi = GeminiAiGenerationApi()

    private var isRestoringActiveWorkout = false

    private val _shouldShowWeeklyRecapStartupLoading = MutableStateFlow(true)
    val shouldShowWeeklyRecapStartupLoading: StateFlow<Boolean> =
        _shouldShowWeeklyRecapStartupLoading.asStateFlow()

    fun markWeeklyRecapStartupLoadingShown() {
        _shouldShowWeeklyRecapStartupLoading.value = false
    }

    private val _plannedWorkouts = MutableStateFlow<List<PlannedWorkout>>(emptyList())
    val plannedWorkouts: StateFlow<List<PlannedWorkout>> = _plannedWorkouts.asStateFlow()

    private var pendingStartedPlannedWorkoutId: String? = null


    val minCardioTime: StateFlow<Float> = mainRepository.minCardioTimeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val maxCardioTime: StateFlow<Float> = mainRepository.maxCardioTimeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 120f)

    val stepCardioTime: StateFlow<Float> = mainRepository.stepCardioTimeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 5f)

    val minResistance: StateFlow<Float> = mainRepository.minResistanceFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val maxResistance: StateFlow<Float> = mainRepository.maxResistanceFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 20f)

    val stepResistance: StateFlow<Float> = mainRepository.stepResistanceFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 1f)

    val minIncline: StateFlow<Float> = mainRepository.minInclineFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val maxIncline: StateFlow<Float> = mainRepository.maxInclineFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 15f)

    val stepIncline: StateFlow<Float> = mainRepository.stepInclineFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.5f)
    private val _notesUpdates = MutableStateFlow<Map<String, String>>(emptyMap())
    val notesUpdates: StateFlow<Map<String, String>> = _notesUpdates.asStateFlow()



    fun updateSetStatus(exerciseIndex: Int, setIndex: Int, newStatus: SetStatus) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val sets = updatedMap[exerciseIndex]?.toMutableList() ?: return
        if (setIndex < sets.size) {
            val currentSet = sets[setIndex]
            sets[setIndex] = currentSet.copy(status = newStatus)
            updatedMap[exerciseIndex] = sets
            _exerciseSetsMap.value = updatedMap
            persistActiveWorkout()
        }
    }


//    fun addExercisesToTemplate(templateId: String, newExercises: List<ExtendedExercise>) {
//        viewModelScope.launch {
//            val template = workoutRepository.getTemplateById(templateId).first()
//            template?.let {
//                val updatedExercises = it.exercises + newExercises
//                workoutRepository.updateTemplate(it.copy(exercises = updatedExercises))
//            }
//        }
//    }

    fun syncAllData() {
        viewModelScope.launch {
            workoutRepository.syncWithFirestore()
            workoutRepository.syncTemplatesWithFirestore()
            workoutRepository.plannedWorkoutRepository.syncWithFirestore()
            workoutRepository.syncRewardsWithFirestore()
        }
    }

    val rewards: StateFlow<List<RewardUiModel>> = workoutRepository.observeRewards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRewardUnlockEvents: StateFlow<List<RewardUnlockEventEntity>> = workoutRepository.observePendingRewardUnlockEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun evaluateDailyStepsForRewards(steps: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.evaluateDailyStepsForRewards(steps)
        }
    }

    fun markRewardUnlockEventShown(eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.markRewardUnlockEventShown(eventId)
        }
    }

    fun evaluateBodyProgressEntriesForRewards(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.evaluateBodyProgressEntriesForRewards(count)
        }
    }

    fun increaseAiExerciseCreatedForRewards() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.increaseAiExerciseCreatedForRewards()
        }
    }

    fun increaseAiTemplateCreatedForRewards() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.increaseAiTemplateCreatedForRewards()
        }
    }

    fun unlockHealthConnectConnectedForRewards() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.unlockHealthConnectConnectedForRewards()
        }
    }

    fun unlockEquipmentRecognizerUsedForRewards() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.unlockEquipmentRecognizerUsedForRewards()
        }
    }


    fun removeExerciseSet(exerciseIndex: Int, setIndex: Int) {
        val exercises = _selectedExercises.value
        val targetExercise = exercises.getOrNull(exerciseIndex) ?: return
        val targetGroupId = targetExercise.supersetGroupId
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val targetIndices = if (targetGroupId != null) {
            exercises.indices.filter { exercises[it].supersetGroupId == targetGroupId }
        } else {
            listOf(exerciseIndex)
        }

        targetIndices.forEach { index ->
            val sets = updatedMap[index]?.toMutableList() ?: return@forEach
            if (setIndex in sets.indices && sets.size > 1) {
                sets.removeAt(setIndex)
                updatedMap[index] = sets
            }
        }

        _exerciseSetsMap.value = updatedMap
        if (targetGroupId != null) normalizeActiveSupersetSetCounts(targetGroupId) else persistActiveWorkout()
    }

    fun removeTemplateSet(templateId: String, exerciseIndex: Int, setIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val template = workoutRepository.getTemplateById(templateId).first() ?: return@launch
            val exercises = template.exercises.toMutableList()
            val groupId = exercises.getOrNull(exerciseIndex)?.supersetGroupId
            val targetIndices = if (groupId != null) {
                exercises.indices.filter { exercises[it].supersetGroupId == groupId }
            } else {
                listOf(exerciseIndex)
            }

            targetIndices.forEach { index ->
                val exercise = exercises.getOrNull(index) ?: return@forEach
                if (setIndex in exercise.sets.indices && exercise.sets.size > 1) {
                    exercises[index] = exercise.copy(sets = exercise.sets.toMutableList().apply { removeAt(setIndex) })
                }
            }

            val normalized = if (groupId != null) normalizeTemplateSupersetSetCounts(exercises, groupId) else exercises
            workoutRepository.updateTemplate(
                template.copy(
                    exercises = normalizeSupersetMetadata(normalized),
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private val _warmupTime = MutableStateFlow(60)
    val warmupTime: StateFlow<Int> = _warmupTime.asStateFlow()

    init {
        viewModelScope.launch {
            workoutRepository.plannedWorkoutRepository.observePlannedWorkouts().collect { planned ->
                _plannedWorkouts.value = planned
            }
        }

        viewModelScope.launch {
            timerDataStore.warmupTimeFlow.collect { time ->
                _warmupTime.value = time
            }
        }

        viewModelScope.launch {
            workoutRepository.initializeDefaultTemplates()
            workoutRepository.initializeRewards()
            workoutRepository.syncTemplatesWithFirestore()
            workoutRepository.plannedWorkoutRepository.syncWithFirestore()
            workoutRepository.syncRewardsWithFirestore()
        }
        viewModelScope.launch {
            workoutRepository.getExerciseNotesUpdates().collect { exercises ->
                    val notesMap = exercises.associate { it.id to it.note }
                    _notesUpdates.value = notesMap
                }
        }
    }


    fun updateTemplateExercises(templateId: String, newExercises: List<ExtendedExercise>) {
        viewModelScope.launch {
            val template = workoutRepository.getTemplateById(templateId).first()
            template?.let {
                val updated = it.copy(
                    exercises = normalizeSupersetMetadata(normalizeTemplateAllSupersetSetCounts(newExercises)),
                    timestamp = System.currentTimeMillis()
                )
                workoutRepository.updateTemplate(updated)
            }
        }
    }

//    suspend fun updateTemplate(template: Template) {
//        workoutRepository.updateTemplate(template)
//    }

    fun deleteTemplate(template: Template) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.deleteTemplate(template)
        }
    }
    fun updateTemplateNameLocalized(
        templateId: String,
        lang: String,
        newName: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val template = workoutRepository.getTemplateById(templateId).first() ?: return@launch
            val normalizedLang = normalizeLanguageCode(lang)
            val trimmedName = newName.trim()

            val updatedNameLocalized = try {
                geminiAiGenerationApi.translateTemplateName(trimmedName, normalizedLang)
            } catch (e: Exception) {
                android.util.Log.e("WorkoutDebug", "translate template name failed", e)
                template.nameLocalized.toMutableMap().apply {
                    this[normalizedLang] = trimmedName
                }
            }

            workoutRepository.updateTemplate(
                template.copy(nameLocalized = updatedNameLocalized)
            )
        }
    }

    fun getTemplateById(id: String): Flow<Template?> {
        return workoutRepository.getTemplateById(id)
    }


    fun updateWarmupTime(newTime: Int) {
        _warmupTime.value = newTime
        viewModelScope.launch {
            timerDataStore.saveWarmupTime(newTime)
        }
    }

    val templates: Flow<List<Template>>
        get() = workoutRepository.getAllTemplates()

    private val _isRefreshingTemplatesFromFirestore = MutableStateFlow(false)
    val isRefreshingTemplatesFromFirestore: StateFlow<Boolean> = _isRefreshingTemplatesFromFirestore.asStateFlow()

    fun refreshTemplatesFromFirestore() {
        if (_isRefreshingTemplatesFromFirestore.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshingTemplatesFromFirestore.value = true
            try {
                workoutRepository.syncTemplatesWithFirestore()
            } catch (e: Exception) {
                android.util.Log.e("WorkoutDebug", "refreshTemplatesFromFirestore ERROR", e)
            } finally {
                _isRefreshingTemplatesFromFirestore.value = false
            }
        }
    }

    fun loadTemplates() {
        viewModelScope.launch {
            workoutRepository.syncTemplatesWithFirestore()
        }
    }


    fun prepareManualPlannedWorkoutDraft() {
        _showOverwriteDialog.value = false
        _timerRunning.value = false
        savedStateHandle.set(TIMER_RUNNING_KEY, false)
        _timerValue.value = 0
        _isWorkoutStarted.value = false
        savedStateHandle.set(WORKOUT_STARTED_KEY, false)
        _workoutName.value = ""
        startTime = 0L
        totalPausedDuration = 0L
        _selectedExercises.value = emptyList()
        _exerciseSetsMap.value = emptyMap()
        clearActiveWorkout()
    }

    fun loadPlannedWorkoutForEditor(plannedWorkout: PlannedWorkout) {
        loadPlannedWorkoutAsDraft(plannedWorkout)
        _isWorkoutStarted.value = false
        savedStateHandle.set(WORKOUT_STARTED_KEY, false)
        _timerRunning.value = false
        savedStateHandle.set(TIMER_RUNNING_KEY, false)
    }

    fun saveDraftAsPlannedWorkout(date: LocalDate, existingPlanId: String? = null, lang: String = "en") {
        viewModelScope.launch(Dispatchers.IO) {
            val normalizedLang = normalizeLanguageCode(lang)
            val trimmedName = _workoutName.value.trim().ifBlank {
                when (normalizedLang) {
                    "ru" -> "Запланированная тренировка"
                    "be" -> "Запланаваная трэніроўка"
                    else -> "Planned workout"
                }
            }
            val millis = date.atTime(LocalTime.of(18, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val localized = fallbackTemplateNameLocalization(trimmedName, normalizedLang)
            val planned = PlannedWorkout(
                id = existingPlanId ?: java.util.UUID.randomUUID().toString(),
                date = millis,
                name = trimmedName,
                nameLocalized = localized,
                exercises = _selectedExercises.value.mapIndexed { index, exercise ->
                    exercise.copy(sets = _exerciseSetsMap.value[index] ?: exercise.sets)
                },
                sourceType = PlannedWorkoutSourceType.MANUAL,
                status = com.example.motivationcalendarapi.model.planning.PlannedWorkoutStatus.PLANNED
            )
            workoutRepository.plannedWorkoutRepository.update(planned)
            pendingStartedPlannedWorkoutId = null
            resetWorkout()
        }
    }

    fun createManualPlannedWorkout(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.plannedWorkoutRepository.createManual(date)
        }
    }

    fun createPlannedWorkoutFromTemplate(date: LocalDate, template: Template) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.plannedWorkoutRepository.createFromTemplate(
                date = date,
                template = template,
                sourceType = PlannedWorkoutSourceType.TEMPLATE
            )
        }
    }

    fun createAiSuggestedWorkoutForDate(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            val template = workoutRepository.getAllTemplates().first().firstOrNull() ?: return@launch
            workoutRepository.plannedWorkoutRepository.createFromTemplate(
                date = date,
                template = template,
                sourceType = PlannedWorkoutSourceType.AI_RECOMMENDED
            )
        }
    }


    fun createAiGeneratedWorkoutForDate(
        date: LocalDate,
        prompt: String,
        selectedBodyParts: List<String>,
        selectedEquipment: List<String>,
        difficulty: String,
        minExercises: Int,
        maxExercises: Int,
        durationMinutes: Int = 45,
        lang: String,
        localExercises: List<com.example.motivationcalendarapi.model.Exercise>,
        aiReason: String? = null,
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val draft = geminiAiGenerationApi.generateWorkout(
                    prompt = prompt,
                    selectedBodyParts = selectedBodyParts,
                    selectedEquipment = selectedEquipment,
                    difficulty = difficulty,
                    exerciseCount = ((minExercises + maxExercises) / 2).coerceIn(1, 15),
                    durationMinutes = durationMinutes,
                    lang = lang,
                    localExercises = localExercises
                )
                val millis = date.atTime(LocalTime.of(18, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                workoutRepository.plannedWorkoutRepository.insert(
                    PlannedWorkout(
                        date = millis,
                        name = draft.nameLocalized["en"] ?: draft.nameLocalized["ru"] ?: "AI workout",
                        nameLocalized = draft.nameLocalized,
                        exercises = draft.exercises,
                        sourceType = PlannedWorkoutSourceType.AI_GENERATED,
                        aiReason = aiReason?.takeIf { it.isNotBlank() }
                            ?: prompt.lineSequence().firstOrNull()?.takeIf { it.isNotBlank() }
                            ?: "Generated by Gemini from selected workout parameters."
                    )
                )
                launch(Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { onError(e.message ?: "AI generation failed") }
            }
        }
    }


    fun createAiGeneratedTrainingPlanForDates(
        dates: List<LocalDate>,
        prompt: String,
        selectedBodyParts: List<String>,
        selectedEquipment: List<String>,
        difficulty: String,
        exerciseCount: Int,
        durationMinutes: Int,
        lang: String,
        localExercises: List<com.example.motivationcalendarapi.model.Exercise>,
        aiReasonBuilder: (LocalDate) -> String = { "Generated by Gemini from selected plan parameters." },
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val safeDates = dates.distinct().sorted()
                if (safeDates.isEmpty()) error("No dates selected")

                safeDates.forEachIndexed { index, date ->
                    val draft = geminiAiGenerationApi.generateWorkout(
                        prompt = "$prompt\nPlan session ${index + 1} of ${safeDates.size}. Date: $date. Keep load distribution realistic across the whole plan.",
                        selectedBodyParts = selectedBodyParts,
                        selectedEquipment = selectedEquipment,
                        difficulty = difficulty,
                        exerciseCount = exerciseCount,
                        durationMinutes = durationMinutes,
                        lang = lang,
                        localExercises = localExercises
                    )
                    val millis = date.atTime(LocalTime.of(18, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    workoutRepository.plannedWorkoutRepository.insert(
                        PlannedWorkout(
                            date = millis,
                            name = draft.nameLocalized["en"] ?: draft.nameLocalized["ru"] ?: "AI plan workout",
                            nameLocalized = draft.nameLocalized,
                            exercises = draft.exercises,
                            sourceType = PlannedWorkoutSourceType.AI_GENERATED,
                            aiReason = aiReasonBuilder(date)
                        )
                    )
                }
                launch(Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { onError(e.message ?: "AI plan generation failed") }
            }
        }
    }


    fun createAiTrainingPlanForWeek() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.plannedWorkoutRepository.createAiWeekPlan(
                templates = workoutRepository.getAllTemplates().first(),
                workouts = _allWorkouts.value
            )
        }
    }

    fun createAiTrainingPlanForDates(dates: List<LocalDate>) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.plannedWorkoutRepository.createAiPlanForDates(
                dates = dates,
                templates = workoutRepository.getAllTemplates().first(),
                workouts = _allWorkouts.value
            )
        }
    }

    fun createTemplatePlanForDates(dates: List<LocalDate>, template: Template) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.plannedWorkoutRepository.createTemplatePlanForDates(dates, template)
        }
    }

    fun skipPlannedWorkout(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.plannedWorkoutRepository.markSkipped(id)
        }
    }

    fun deletePlannedWorkout(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.plannedWorkoutRepository.delete(id)
        }
    }

    fun restoreSkippedPlannedWorkout(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.plannedWorkoutRepository.restoreSkipped(id)
        }
    }

    fun movePlannedWorkout(id: String, date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.plannedWorkoutRepository.moveToDate(id, date)
        }
    }

    fun startWorkoutFromPlan(plannedWorkout: PlannedWorkout) {
        loadPlannedWorkoutAsDraft(plannedWorkout)
        startWorkout()
    }

    fun editPlannedWorkout(plannedWorkout: PlannedWorkout) {
        loadPlannedWorkoutAsDraft(plannedWorkout)
        _isWorkoutStarted.value = true
        savedStateHandle.set(WORKOUT_STARTED_KEY, true)
        _timerRunning.value = false
        savedStateHandle.set(TIMER_RUNNING_KEY, false)
        persistActiveWorkout()
    }

    private fun loadPlannedWorkoutAsDraft(plannedWorkout: PlannedWorkout) {
        _showOverwriteDialog.value = false
        _timerRunning.value = false
        savedStateHandle.set(TIMER_RUNNING_KEY, false)
        _timerValue.value = 0
        _workoutName.value = plannedWorkout.name.ifBlank { plannedWorkout.nameLocalized["en"] ?: "Planned workout" }
        startTime = 0L
        totalPausedDuration = 0L
        _selectedExercises.value = emptyList()
        _exerciseSetsMap.value = emptyMap()
        plannedWorkout.exercises.forEachIndexed { index, exercise ->
            _selectedExercises.value = _selectedExercises.value + exercise
            _exerciseSetsMap.value = _exerciseSetsMap.value.toMutableMap().apply {
                put(index, exercise.sets)
            }
        }
        pendingStartedPlannedWorkoutId = plannedWorkout.id
    }


    fun saveAsTemplate(
        workout: Workout,
        templateName: String,
        lang: String
    ) {
        val normalizedLang = normalizeLanguageCode(lang)
        val trimmedName = templateName.trim()

        val templateExercises = workout.exercises.map { extendedExercise ->
            extendedExercise.copy(
                sets = extendedExercise.sets.map { set ->
                    set.copy(status = SetStatus.NONE)
                }
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val nameLocalized = try {
                geminiAiGenerationApi.translateTemplateName(trimmedName, normalizedLang)
            } catch (e: Exception) {
                android.util.Log.e("WorkoutDebug", "translate template name failed", e)
                fallbackTemplateNameLocalization(trimmedName, normalizedLang)
            }

            val template = Template(
                name = "",
                nameLocalized = nameLocalized,
                exercises = templateExercises,
                timestamp = System.currentTimeMillis()
            )

            workoutRepository.insertTemplate(template)
        }
    }

    private fun normalizeLanguageCode(lang: String): String {
        return when (lang.lowercase()) {
            "ru" -> "ru"
            "be", "by" -> "be"
            "en" -> "en"
            else -> "en"
        }
    }

    private fun fallbackTemplateNameLocalization(name: String, lang: String): Map<String, String> {
        val normalizedLang = normalizeLanguageCode(lang)
        return mapOf(
            "en" to name,
            "ru" to name,
            "be" to name
        ).toMutableMap().apply {
            this[normalizedLang] = name
        }
    }

    private fun normalizeWorkoutNameLocalization(
        source: Map<String, String>,
        originalName: String,
        lang: String
    ): Map<String, String> {
        val normalizedLang = normalizeLanguageCode(lang)
        val safeOriginal = originalName.trim()
        val firstTranslated = listOf("en", "ru", "be")
            .mapNotNull { source[it]?.trim()?.takeIf(String::isNotBlank) }
            .firstOrNull()
            ?: safeOriginal

        return mapOf(
            "en" to (source["en"]?.trim()?.takeIf(String::isNotBlank) ?: firstTranslated),
            "ru" to (source["ru"]?.trim()?.takeIf(String::isNotBlank) ?: firstTranslated),
            "be" to (source["be"]?.trim()?.takeIf(String::isNotBlank) ?: firstTranslated)
        )
    }

    private companion object {
        const val START_TIME_KEY = "start_time"
        const val PAUSED_DURATION_KEY = "paused_duration"
        const val TIMER_RUNNING_KEY = "timer_running"
        const val WORKOUT_STARTED_KEY = "workout_started"
    }

    private var startTime: Long
        get() = savedStateHandle.get<Long>(START_TIME_KEY) ?: 0L
        set(value) = savedStateHandle.set(START_TIME_KEY, value)

    private var totalPausedDuration: Long
        get() = savedStateHandle.get<Long>(PAUSED_DURATION_KEY) ?: 0L
        set(value) = savedStateHandle.set(PAUSED_DURATION_KEY, value)

    private val _timerRunning =
        MutableStateFlow(savedStateHandle.get<Boolean>(TIMER_RUNNING_KEY) ?: false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timerValue = MutableStateFlow(0)
    val timerValue: StateFlow<Int> = _timerValue.asStateFlow()


    fun startTimer() {
        if (!_timerRunning.value) {
            _timerRunning.value = true
            savedStateHandle.set(TIMER_RUNNING_KEY, true)
            startTime = System.currentTimeMillis() - totalPausedDuration
            persistActiveWorkout()
        }
    }

    fun pauseTimer() {
        if (_timerRunning.value) {
            _timerRunning.value = false
            savedStateHandle.set(TIMER_RUNNING_KEY, false)
            totalPausedDuration = System.currentTimeMillis() - startTime
            persistActiveWorkout()
        }
    }

    fun resumeTimer() {
        if (!_timerRunning.value) {
            _timerRunning.value = true
            savedStateHandle.set(TIMER_RUNNING_KEY, true)
            startTime = System.currentTimeMillis() - totalPausedDuration
            persistActiveWorkout()
        }
    }


    fun resetWorkout() {
        _timerRunning.value = false
        savedStateHandle.set(TIMER_RUNNING_KEY, false)
        _timerValue.value = 0
        _workoutName.value = ""
        _isWorkoutStarted.value = false
        savedStateHandle.set(WORKOUT_STARTED_KEY, false)
        startTime = 0L
        totalPausedDuration = 0L
        _selectedExercises.value = emptyList()
        _exerciseSetsMap.value = emptyMap()
        clearActiveWorkout()
    }

    fun getWorkoutById(id: String): Workout {
        return workoutRepository.getWorkoutById(id)
    }


    private val _allWorkouts = MutableStateFlow<List<Workout>>(emptyList())
    val allWorkouts: StateFlow<List<Workout>> = _allWorkouts.asStateFlow()

    private val _areWorkoutsLoaded = MutableStateFlow(false)
    val areWorkoutsLoaded: StateFlow<Boolean> = _areWorkoutsLoaded.asStateFlow()


    private fun isInCurrentWeek(timestamp: Long): Boolean {
        val workoutDate = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        val endOfWeek = startOfWeek.plusDays(6)
        return workoutDate in startOfWeek..endOfWeek
    }

    val weekReps: StateFlow<Int> = allWorkouts.map { workouts ->
        workouts.filter { isInCurrentWeek(it.timestamp) }
            .sumOf { workout ->
                workout.exercises.sumOf { exercise ->
                    exercise.sets.sumOf { it.rep }
                }
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val weekWeight: StateFlow<Float> = allWorkouts.map { workouts ->
        workouts.filter { isInCurrentWeek(it.timestamp) }
            .sumOf { workout ->
                workout.exercises.sumOf { exercise ->
                    exercise.sets.filter { it.status != SetStatus.FAILED }.sumOf { it.strengthVolume() }
                }
            }.toFloat()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    fun findMaxSetForExercise(exerciseId: String): ExerciseSet? {
        return allWorkouts.value
            .flatMap { it.exercises }
            .filter { it.exercise.id == exerciseId }
            .flatMap { it.sets }
            .maxByOrNull { it.weight }
    }

    fun calculateWorkoutDifficulty(workout: Workout): DifficultyLevel {
        val validSets = workout.exercises.flatMap { exercise ->
            exercise.sets.filter { set -> set.status != SetStatus.FAILED }
                .map { set -> exercise.exercise.getCardType() to set }
        }

        if (validSets.isEmpty()) return DifficultyLevel.EASY

        val failedSetsCount = workout.exercises.sumOf { exercise ->
            exercise.sets.count { set -> set.status == SetStatus.FAILED }
        }

        val exerciseCount = workout.exercises.count { exercise ->
            exercise.sets.any { set -> set.status != SetStatus.FAILED }
        }

        val durationMinutes = (workout.duration / 60f).coerceAtLeast(
            validSets.sumOf { (_, set) -> set.time.toDouble() }.toFloat()
        )

        val strengthVolume = validSets
            .filter { (cardType, set) ->
                cardType == ExerciseCardType.STRENGTH && set.rep > 0 && set.weight > 0f
            }
            .sumOf { (_, set) -> set.strengthVolume() }
            .toFloat()

        val strengthWorkingSets = validSets.count { (cardType, set) ->
            cardType == ExerciseCardType.STRENGTH && set.rep > 0
        }

        val cardioLoadMinutes = validSets.sumOf { (cardType, set) ->
            when (cardType) {
                ExerciseCardType.BIKE -> {
                    val resistanceMultiplier = 1.0 + (set.resistance.coerceAtLeast(0f) / 10f)
                    set.time.coerceAtLeast(0f).toDouble() * resistanceMultiplier
                }
                ExerciseCardType.TREADMILL -> {
                    val speedMultiplier = 1.0 + (set.resistance.coerceAtLeast(0f) / 8f)
                    val inclineMultiplier = 1.0 + (set.incline.coerceAtLeast(0f) / 20f)
                    set.time.coerceAtLeast(0f).toDouble() * speedMultiplier * inclineMultiplier
                }
                ExerciseCardType.STRENGTH -> 0.0
            }
        }.toFloat()

        val durationScore = when {
            durationMinutes >= 75f -> 3
            durationMinutes >= 45f -> 2
            durationMinutes >= 20f -> 1
            else -> 0
        }

        val exerciseScore = when {
            exerciseCount >= 7 -> 2
            exerciseCount >= 4 -> 1
            else -> 0
        }

        val strengthScore = when {
            strengthVolume >= 8_000f || strengthWorkingSets >= 18 -> 3
            strengthVolume >= 3_500f || strengthWorkingSets >= 10 -> 2
            strengthVolume >= 800f || strengthWorkingSets >= 4 -> 1
            else -> 0
        }

        val cardioScore = when {
            cardioLoadMinutes >= 55f -> 3
            cardioLoadMinutes >= 30f -> 2
            cardioLoadMinutes >= 12f -> 1
            else -> 0
        }

        val heartRateScore = when {
            workout.averageHeartRate == null -> 0
            workout.averageHeartRate >= 160L -> 3
            workout.averageHeartRate >= 135L -> 2
            workout.averageHeartRate >= 110L -> 1
            else -> 0
        }

        val failedSetsScore = when {
            failedSetsCount >= 5 -> 2
            failedSetsCount >= 2 -> 1
            else -> 0
        }

        val mixedWorkoutBonus = if (strengthScore > 0 && cardioScore > 0) 1 else 0
        val loadScore = if (strengthScore > cardioScore) strengthScore else cardioScore
        val totalScore = durationScore + exerciseScore + loadScore + heartRateScore + failedSetsScore + mixedWorkoutBonus

        return when {
            strengthVolume >= 10_000f -> DifficultyLevel.HARD
            cardioLoadMinutes >= 70f -> DifficultyLevel.HARD
            workout.averageHeartRate != null && workout.averageHeartRate >= 165L && durationMinutes >= 20f -> DifficultyLevel.HARD
            totalScore >= 7 -> DifficultyLevel.HARD
            totalScore >= 3 -> DifficultyLevel.NORMAL
            else -> DifficultyLevel.EASY
        }
    }

    fun calculateTotalKg(workout: Workout): Float {
        return workout.exercises.sumOf { exercise ->
            exercise.sets
                .filter { set -> set.status != SetStatus.FAILED }
                .sumOf { set ->
                    set.strengthVolume()
                }
        }.toFloat()
    }

    fun isEmptySet(exercise: ExtendedExercise, set: ExerciseSet, lang: String): Boolean {
        return when (exercise.exercise.getCardType(lang)) {
            ExerciseCardType.STRENGTH -> set.rep <= 0
            ExerciseCardType.BIKE -> set.time <= 0f
            ExerciseCardType.TREADMILL -> set.time <= 0f
        }
    }

    fun hasEmptySets(lang: String): Boolean {
        return _selectedExercises.value.withIndex().any { indexedExercise ->
            val sets = _exerciseSetsMap.value[indexedExercise.index] ?: emptyList()
            sets.any { set -> isEmptySet(indexedExercise.value, set, lang) }
        }
    }

    fun buildWorkoutExercisesWithoutEmptySets(lang: String): List<ExtendedExercise> {
        return _selectedExercises.value.mapIndexedNotNull { index, exercise ->
            val cleanedSets = (_exerciseSetsMap.value[index] ?: emptyList())
                .filterNot { set -> isEmptySet(exercise, set, lang) }

            if (cleanedSets.isEmpty()) {
                null
            } else {
                exercise.copy(sets = cleanedSets)
            }
        }
    }


    fun updateExerciseNote(exerciseId: String, newNote: String) {
        viewModelScope.launch {
            workoutRepository.updateExerciseNote(exerciseId, newNote)
            _selectedExercises.value = _selectedExercises.value.map { ex ->
                if (ex.exercise.id == exerciseId) ex.copy(
                    exercise = ex.exercise.copy(note = newNote)
                ) else ex
            }
        }
    }

    private val _workoutName = MutableStateFlow("")
    val workoutName: StateFlow<String> = _workoutName.asStateFlow()

    private val _isWorkoutStarted = MutableStateFlow(savedStateHandle.get<Boolean>(WORKOUT_STARTED_KEY) ?: false)
    val isWorkoutStarted: StateFlow<Boolean> = _isWorkoutStarted.asStateFlow()

    private val _currentWorkout = MutableStateFlow<Workout?>(null)
//    val currentWorkout: StateFlow<Workout?> get() = _currentWorkout

//    private val _isLoadingWorkout = MutableStateFlow(false)
//    val isLoadingWorkout: StateFlow<Boolean> = _isLoadingWorkout.asStateFlow()

    private val _selectedExercises = MutableStateFlow<List<ExtendedExercise>>(emptyList())
    val selectedExercises: StateFlow<List<ExtendedExercise>> = _selectedExercises.asStateFlow()

    private val _exerciseSetsMap = MutableStateFlow<Map<Int, List<ExerciseSet>>>(emptyMap())
    val exerciseSetsMap: StateFlow<Map<Int, List<ExerciseSet>>> = _exerciseSetsMap.asStateFlow()

    private val _showOverwriteDialog = MutableStateFlow(false)
    val showOverwriteDialog: StateFlow<Boolean> = _showOverwriteDialog.asStateFlow()

    private val _workoutsToday = MutableStateFlow<List<Workout>>(emptyList())
    val workoutsToday: StateFlow<List<Workout>> = _workoutsToday.asStateFlow()


    init {
        viewModelScope.launch {
            val draft = timerDataStore.activeWorkoutFlow.first()
            if (draft.isStarted) {
                isRestoringActiveWorkout = true
                _isWorkoutStarted.value = true
                savedStateHandle.set(WORKOUT_STARTED_KEY, true)
                _timerRunning.value = draft.isRunning
                savedStateHandle.set(TIMER_RUNNING_KEY, draft.isRunning)
                startTime = draft.startTime
                totalPausedDuration = draft.totalPausedDuration
                _workoutName.value = draft.workoutName
                _selectedExercises.value = draft.selectedExercises
                _exerciseSetsMap.value = draft.exerciseSetsMap
                pendingStartedPlannedWorkoutId = draft.plannedWorkoutId

                _timerValue.value = if (draft.isRunning && draft.startTime > 0L) {
                    ((System.currentTimeMillis() - draft.startTime) / 1000L).toInt().coerceAtLeast(0)
                } else {
                    (draft.totalPausedDuration / 1000L).toInt().coerceAtLeast(0)
                }
                isRestoringActiveWorkout = false
            }
        }

        if (_timerRunning.value) {
            startTime = System.currentTimeMillis() - totalPausedDuration
        }

        viewModelScope.launch {
            timerDataStore.warmupTimeFlow.collect { time ->
                _warmupTime.value = time
            }
        }

        viewModelScope.launch {
            workoutRepository.getWorkoutsToday().collect {
                _workoutsToday.value = it
            }
        }

        viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (_timerRunning.value) {
                    val currentTime = System.currentTimeMillis()
                    val elapsedSeconds = ((currentTime - startTime) / 1000).toInt()
                    _timerValue.value = elapsedSeconds
                }
            }
        }

        loadWorkouts()
    }

    private fun persistActiveWorkout() {
        if (isRestoringActiveWorkout) return
        viewModelScope.launch(Dispatchers.IO) {
            if (_isWorkoutStarted.value) {
                timerDataStore.saveActiveWorkoutDraft(
                    ActiveWorkoutDraft(
                        isStarted = _isWorkoutStarted.value,
                        isRunning = _timerRunning.value,
                        startTime = startTime,
                        totalPausedDuration = totalPausedDuration,
                        workoutName = _workoutName.value,
                        selectedExercises = _selectedExercises.value,
                        exerciseSetsMap = _exerciseSetsMap.value,
                        plannedWorkoutId = pendingStartedPlannedWorkoutId
                    )
                )
            }
        }
    }

    private fun clearActiveWorkout() {
        viewModelScope.launch(Dispatchers.IO) {
            timerDataStore.clearActiveWorkoutDraft()
        }
    }

    fun checkForExistingWorkout() {
        startNewWorkoutForToday()
    }

    fun confirmOverwrite() {
        _showOverwriteDialog.value = false
        startNewWorkoutForToday()
    }

    private fun startNewWorkoutForToday() {
        _showOverwriteDialog.value = false
        _timerRunning.value = false
        savedStateHandle.set(TIMER_RUNNING_KEY, false)
        _timerValue.value = 0
        _workoutName.value = ""
        startTime = 0L
        totalPausedDuration = 0L
        _selectedExercises.value = emptyList()
        _exerciseSetsMap.value = emptyMap()
        startWorkout()
    }

    fun dismissOverwriteDialog() {
        _showOverwriteDialog.value = false
    }

    private fun startWorkout() {
        _isWorkoutStarted.value = true
        savedStateHandle.set(WORKOUT_STARTED_KEY, true)
        startTimer()
        persistActiveWorkout()
    }

//    val testWorkouts = listOf(
//        Workout(
//            name = "Full Body Strength",
//            duration = 3600,
//            timestamp = Instant.parse("2023-01-15T10:15:30.00Z").toEpochMilli(),
//            exercises = listOf(
//                ExtendedExercise(
//                    exercise = Exercise(id = "1", name = "Приседания", note = "Спина прямая",bodyPart = "", equipment = "", target = "", secondaryMuscles = listOf("",""), instructions = listOf("",""), gifUrl = ""),
//                    sets = listOf(
//                        ExerciseSet(rep = 8, weight = 70f, status = SetStatus.COMPLETED),
//                        ExerciseSet(rep = 8, weight = 70f, status = SetStatus.COMPLETED),
//                        ExerciseSet(rep = 6, weight = 70f, status = SetStatus.FAILED)
//                    )
//                ),
//                ExtendedExercise(
//                    exercise = Exercise(id = "2", name = "Жим лежа", note = "",bodyPart = "", equipment = "", target = "", secondaryMuscles = listOf("",""), instructions = listOf("",""), gifUrl = ""),
//                    sets = List(5) { ExerciseSet(rep = 10, weight = 50f) }
//                )
//            )
//        ),
//
//        Workout(
//            name = "Full Body Strength",
//            duration = 3600,
//            timestamp = Instant.parse("2023-01-16T10:15:30.00Z").toEpochMilli(),
//            exercises = listOf(
//                ExtendedExercise(
//                    exercise = Exercise(id = "1", name = "Приседания", note = "Спина прямая",bodyPart = "", equipment = "", target = "", secondaryMuscles = listOf("",""), instructions = listOf("",""), gifUrl = ""),
//                    sets = listOf(
//                        ExerciseSet(rep = 80, weight = 700f, status = SetStatus.COMPLETED),
//                        ExerciseSet(rep = 80, weight = 700f, status = SetStatus.COMPLETED),
//                        ExerciseSet(rep = 60, weight = 700f, status = SetStatus.FAILED)
//                    )
//                ),
//                ExtendedExercise(
//                    exercise = Exercise(id = "2", name = "Жим лежа", note = "",bodyPart = "", equipment = "", target = "", secondaryMuscles = listOf("",""), instructions = listOf("",""), gifUrl = ""),
//                    sets = List(5) { ExerciseSet(rep = 10, weight = 50f) }
//                )
//            )
//        ),
//        Workout(
//            name = "Upper Body",
//            duration = 2700,
//            timestamp = Instant.parse("2023-01-25T18:30:00.00Z").toEpochMilli(),
//            exercises = listOf(
//                ExtendedExercise(
//                    exercise = Exercise(id = "3", name = "Тяга штанги", note = "Контроль техники", bodyPart = "", equipment = "", target = "", secondaryMuscles = listOf("",""), instructions = listOf("",""), gifUrl = ""),
//                    sets = listOf(
//                        ExerciseSet(rep = 12, weight = 45f),
//                        ExerciseSet(rep = 10, weight = 50f),
//                        ExerciseSet(rep = 8, weight = 55f)
//                    )
//                )
//            )
//        ),
//    )

    fun workoutsByYearMonthAndWeek(): Flow<Map<Int, Map<Month, Map<Int, List<Workout>>>>> {
        return allWorkouts.map { workouts ->
            workouts.sortedBy { it.timestamp }.groupBy { workout ->
                Instant.ofEpochMilli(workout.timestamp)
                    .atZone(ZoneId.systemDefault()).year
            }.mapValues { yearEntry ->
                yearEntry.value.groupBy { workout ->
                    Instant.ofEpochMilli(workout.timestamp)
                        .atZone(ZoneId.systemDefault()).month
                }.mapValues { monthEntry ->
                    monthEntry.value.groupBy { workout ->
                        calculateWeekOfMonth(workout.timestamp)
                    }.toSortedMap()
                }.toSortedMap()
            }.toSortedMap()
        }
    }




    private fun calculateWeekOfMonth(timestamp: Long): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            firstDayOfWeek = Calendar.MONDAY
        }
        return calendar.get(Calendar.WEEK_OF_MONTH)
    }




//    fun getWorkoutOrderInMonth(workout: Workout, workouts: List<Workout>): Int {
//        val allInMonth = workouts.filter { other ->
//            val workoutDate =
//                Instant.ofEpochMilli(workout.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
//            val otherDate =
//                Instant.ofEpochMilli(other.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
//            workoutDate.year == otherDate.year && workoutDate.month == otherDate.month
//        }
//        return allInMonth.indexOf(workout) + 1
//    }


//    fun updateWorkout(workout: Workout) {
//        viewModelScope.launch(Dispatchers.IO) {
//            workoutRepository.updateWorkout(workout)
//        }
//    }

    fun loadWorkouts() {
        viewModelScope.launch {
            _areWorkoutsLoaded.value = false
            workoutRepository.getAllWorkouts().collect { workouts ->
                _allWorkouts.value = workouts
                _areWorkoutsLoaded.value = true
            }
        }
    }


//    fun finishWorkout() {
//        _timerRunning.value = false
//        _isWorkoutStarted.value = false
//    }



    fun setWorkoutName(name: String) {
        _workoutName.value = name
        persistActiveWorkout()
    }


    fun getWorkoutStartTime(): Long = startTime

    suspend fun saveWorkout(exercises: List<ExtendedExercise>, averageHeartRate: Long? = null, lang: String = "en") {
        val savedWorkoutName = workoutName.value.trim()
        val savedDuration = timerValue.value
        val savedTimestamp = System.currentTimeMillis()
        val normalizedLang = normalizeLanguageCode(lang)

        kotlinx.coroutines.withContext(Dispatchers.IO) {
            val nameLocalized = try {
                geminiAiGenerationApi.translateTemplateName(savedWorkoutName, normalizedLang)
            } catch (e: Exception) {
                android.util.Log.e("WorkoutDebug", "translate workout name failed", e)
                fallbackTemplateNameLocalization(savedWorkoutName, normalizedLang)
            }

            val safeNameLocalized = normalizeWorkoutNameLocalization(
                source = nameLocalized,
                originalName = savedWorkoutName,
                lang = normalizedLang
            )

            val workoutWithoutDifficulty = Workout(
                name = savedWorkoutName,
                nameLocalized = safeNameLocalized,
                duration = savedDuration,
                timestamp = savedTimestamp,
                averageHeartRate = averageHeartRate,
                exercises = exercises
            )
            val workout = workoutWithoutDifficulty.copy(
                difficulty = calculateWorkoutDifficulty(workoutWithoutDifficulty)
            )
            workoutRepository.insertWorkout(workout)
            val plannedId = pendingStartedPlannedWorkoutId
            if (plannedId != null) {
                workoutRepository.plannedWorkoutRepository.markCompleted(plannedId, workout.id)
            }
        }
        pendingStartedPlannedWorkoutId = null
        resetWorkout()
    }


//    fun formatWorkoutName(name: String): String {
//        return if (name.isBlank()) "Blank" else name
//    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            workoutRepository.delete(workout)
            loadWorkouts()
            _currentWorkout.value = null
        }

    }



    fun addExercise(exercise: ExtendedExercise) {
        val currentSize = _selectedExercises.value.size
        _selectedExercises.value = _selectedExercises.value + exercise

        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        updatedMap[currentSize] = if (exercise.sets.isNotEmpty() && exercise.sets.all { it.rep >= 0 }) {
            exercise.sets
        } else {
            listOf(ExerciseSet(rep = 0, weight = 0f))
        }
        _exerciseSetsMap.value = updatedMap
        persistActiveWorkout()
    }

    fun updateRep(exerciseIndex: Int, setIndex: Int, newRep: Int) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val sets = updatedMap[exerciseIndex]?.toMutableList() ?: return
        if (setIndex < sets.size) {
            val current = sets[setIndex]
            val syncedDropParts = if (current.dropSetParts.isNotEmpty()) {
                current.dropSetParts.toMutableList().also { parts ->
                    parts[0] = parts[0].copy(rep = newRep)
                }
            } else current.dropSetParts
            val syncedCluster = current.clusterSetData?.copy(repsPerCluster = newRep.coerceAtLeast(1), clusterCount = 1)
            sets[setIndex] = current.copy(rep = newRep, dropSetParts = syncedDropParts, clusterSetData = syncedCluster)
            updatedMap[exerciseIndex] = sets
            _exerciseSetsMap.value = updatedMap
            persistActiveWorkout()
        }
    }

    fun updateWeight(exerciseIndex: Int, setIndex: Int, newWeight: Float) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val sets = updatedMap[exerciseIndex]?.toMutableList() ?: return
        if (setIndex < sets.size) {
            val current = sets[setIndex]
            val syncedDropParts = if (current.dropSetParts.isNotEmpty()) {
                current.dropSetParts.toMutableList().also { parts ->
                    parts[0] = parts[0].copy(weight = newWeight)
                }
            } else current.dropSetParts
            val syncedCluster = current.clusterSetData?.copy(weight = newWeight)
            sets[setIndex] = current.copy(weight = newWeight, dropSetParts = syncedDropParts, clusterSetData = syncedCluster)
            updatedMap[exerciseIndex] = sets
            _exerciseSetsMap.value = updatedMap
            persistActiveWorkout()
        }
    }

    val totalKg: StateFlow<Float> =
        combine(selectedExercises, exerciseSetsMap) { exercises, setsMap ->
            exercises.indices.sumOf { index ->
                setsMap[index]?.filter { it.status != SetStatus.FAILED }?.sumOf { it.strengthVolume() } ?: 0.0
            }.toFloat()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    fun removeExercise(exerciseIndex: Int) {
        val updatedExercises = _selectedExercises.value.toMutableList().apply {
            removeAt(exerciseIndex)
        }

        val updatedMap = _exerciseSetsMap.value.toMutableMap().filterKeys { it != exerciseIndex }
            .mapKeys { (key, _) ->
                if (key > exerciseIndex) key - 1 else key
            }


        _selectedExercises.value = normalizeSupersetMetadata(updatedExercises)
        _exerciseSetsMap.value = updatedMap
        persistActiveWorkout()
    }

    val minRep: StateFlow<Int> = mainRepository.minRepFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val maxRep: StateFlow<Int> = mainRepository.maxRepFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 24)

    val stepRep: StateFlow<Int> = mainRepository.stepRepFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 4)

    val minWeight: StateFlow<Float> = mainRepository.minWeightFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val maxWeight: StateFlow<Float> = mainRepository.maxWeightFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 200f)

    val stepWeight: StateFlow<Float> = mainRepository.stepWeightFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 10f)

    fun updateTime(exerciseIndex: Int, setIndex: Int, newTime: Float) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val sets = updatedMap[exerciseIndex]?.toMutableList() ?: return

        if (setIndex < sets.size) {
            sets[setIndex] = sets[setIndex].copy(time = newTime)
            updatedMap[exerciseIndex] = sets
            _exerciseSetsMap.value = updatedMap
            persistActiveWorkout()
        }
    }

    fun updateResistance(exerciseIndex: Int, setIndex: Int, newResistance: Float) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val sets = updatedMap[exerciseIndex]?.toMutableList() ?: return

        if (setIndex < sets.size) {
            sets[setIndex] = sets[setIndex].copy(resistance = newResistance)
            updatedMap[exerciseIndex] = sets
            _exerciseSetsMap.value = updatedMap
            persistActiveWorkout()
        }
    }

    fun updateIncline(exerciseIndex: Int, setIndex: Int, newIncline: Float) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val sets = updatedMap[exerciseIndex]?.toMutableList() ?: return

        if (setIndex < sets.size) {
            sets[setIndex] = sets[setIndex].copy(incline = newIncline)
            updatedMap[exerciseIndex] = sets
            _exerciseSetsMap.value = updatedMap
            persistActiveWorkout()
        }
    }

    private fun ExerciseSet.strengthVolume(): Double = when (type) {
        com.example.motivationcalendarapi.model.ExerciseSetType.DROP_SET -> dropSetParts.sumOf { it.weight.toDouble() * it.rep.toDouble() }
        com.example.motivationcalendarapi.model.ExerciseSetType.CLUSTER_SET -> {
            val cluster = clusterSetData
            if (cluster != null) cluster.weight.toDouble() * cluster.clusterCount.toDouble() * cluster.repsPerCluster.toDouble()
            else weight.toDouble() * rep.toDouble()
        }
        com.example.motivationcalendarapi.model.ExerciseSetType.NORMAL -> weight.toDouble() * rep.toDouble()
    }

    fun updateActiveWorkoutSet(exerciseIndex: Int, setIndex: Int, newSet: ExerciseSet) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val sets = updatedMap[exerciseIndex]?.toMutableList() ?: return
        if (setIndex >= sets.size) return
        sets[setIndex] = newSet
        updatedMap[exerciseIndex] = sets
        _exerciseSetsMap.value = updatedMap
        persistActiveWorkout()
    }

    fun convertActiveWorkoutSetToNormal(exerciseIndex: Int, setIndex: Int) {
        _exerciseSetsMap.value[exerciseIndex]?.getOrNull(setIndex)?.let {
            updateActiveWorkoutSet(exerciseIndex, setIndex, it.toNormalSet())
        }
    }

    fun convertActiveWorkoutSetToDropSet(exerciseIndex: Int, setIndex: Int) {
        _exerciseSetsMap.value[exerciseIndex]?.getOrNull(setIndex)?.let {
            updateActiveWorkoutSet(exerciseIndex, setIndex, it.toDefaultDropSet())
        }
    }

    fun convertActiveWorkoutSetToClusterSet(exerciseIndex: Int, setIndex: Int) {
        _exerciseSetsMap.value[exerciseIndex]?.getOrNull(setIndex)?.let {
            updateActiveWorkoutSet(exerciseIndex, setIndex, it.toDefaultClusterSet())
        }
    }

    private data class ExerciseWithSets(
        val exercise: ExtendedExercise,
        val sets: List<ExerciseSet>
    )

    private fun defaultSetForExercise(exercise: ExtendedExercise, lastSet: ExerciseSet? = null): ExerciseSet {
        return when (exercise.exercise.getCardType()) {
            ExerciseCardType.STRENGTH -> ExerciseSet(
                rep = lastSet?.rep ?: minRep.value,
                weight = lastSet?.weight ?: minWeight.value,
                status = SetStatus.NONE
            )
            ExerciseCardType.BIKE -> ExerciseSet(
                time = lastSet?.time ?: minCardioTime.value,
                resistance = lastSet?.resistance ?: minResistance.value,
                status = SetStatus.NONE
            )
            ExerciseCardType.TREADMILL -> ExerciseSet(
                time = lastSet?.time ?: minCardioTime.value,
                resistance = lastSet?.resistance ?: minResistance.value,
                incline = lastSet?.incline ?: minIncline.value,
                status = SetStatus.NONE
            )
        }
    }

    private fun normalizeSupersetMetadata(exercises: List<ExtendedExercise>): List<ExtendedExercise> {
        val counts = exercises.mapNotNull { it.supersetGroupId }.groupingBy { it }.eachCount()
        val orderByGroup = mutableMapOf<String, Int>()
        return exercises.map { exercise ->
            val groupId = exercise.supersetGroupId
            if (groupId == null || (counts[groupId] ?: 0) < 2) {
                exercise.copy(supersetGroupId = null, supersetOrder = null)
            } else {
                val order = orderByGroup.getOrDefault(groupId, 0)
                orderByGroup[groupId] = order + 1
                exercise.copy(supersetGroupId = groupId, supersetOrder = order)
            }
        }
    }

    private fun activePairs(): List<ExerciseWithSets> {
        val map = _exerciseSetsMap.value
        return _selectedExercises.value.mapIndexed { index, exercise ->
            ExerciseWithSets(exercise, map[index] ?: exercise.sets)
        }
    }

    private fun applyActivePairs(pairs: List<ExerciseWithSets>, persist: Boolean = true) {
        _selectedExercises.value = normalizeSupersetMetadata(pairs.map { it.exercise })
        _exerciseSetsMap.value = pairs.mapIndexed { index, pair -> index to pair.sets }.toMap()
        if (persist) persistActiveWorkout()
    }

    private fun normalizeActiveSupersetSetCounts(groupId: String) {
        val pairs = activePairs()
        val targetCount = pairs
            .filter { it.exercise.supersetGroupId == groupId }
            .maxOfOrNull { it.sets.size }
            ?.coerceAtLeast(1)
            ?: return
        val normalized = pairs.map { pair ->
            if (pair.exercise.supersetGroupId != groupId) return@map pair
            val result = pair.sets.toMutableList()
            while (result.size < targetCount) {
                result.add(defaultSetForExercise(pair.exercise, result.lastOrNull()))
            }
            ExerciseWithSets(pair.exercise, result)
        }
        applyActivePairs(normalized)
    }

    private fun mergeActiveExercisesIntoSuperset(firstIndex: Int, secondIndex: Int) {
        val pairs = activePairs().toMutableList()
        if (firstIndex !in pairs.indices || secondIndex !in pairs.indices) return
        val firstGroup = pairs[firstIndex].exercise.supersetGroupId
        val secondGroup = pairs[secondIndex].exercise.supersetGroupId
        val groupId = firstGroup ?: secondGroup ?: newSupersetGroupId()
        val firstExerciseId = pairs[firstIndex].exercise.exercise.id
        val secondExerciseId = pairs[secondIndex].exercise.exercise.id
        val merged = pairs.map { pair ->
            val currentGroup = pair.exercise.supersetGroupId
            if ((firstGroup != null && currentGroup == firstGroup) || (secondGroup != null && currentGroup == secondGroup) || pair.exercise.exercise.id == firstExerciseId || pair.exercise.exercise.id == secondExerciseId) {
                pair.copy(exercise = pair.exercise.copy(supersetGroupId = groupId))
            } else pair
        }
        applyActivePairs(merged)
        normalizeActiveSupersetSetCounts(groupId)
    }

    fun createSupersetWithNext(exerciseIndex: Int) {
        mergeActiveExercisesIntoSuperset(exerciseIndex, exerciseIndex + 1)
    }

    fun createSupersetWithPrevious(exerciseIndex: Int) {
        mergeActiveExercisesIntoSuperset(exerciseIndex - 1, exerciseIndex)
    }

    fun removeExerciseFromSuperset(exerciseIndex: Int) {
        val pairs = activePairs().toMutableList()
        if (exerciseIndex !in pairs.indices) return
        val groupId = pairs[exerciseIndex].exercise.supersetGroupId ?: return
        val groupIndices = pairs.indices.filter { pairs[it].exercise.supersetGroupId == groupId }
        if (groupIndices.size <= 2) {
            groupIndices.forEach { index ->
                pairs[index] = pairs[index].copy(
                    exercise = pairs[index].exercise.copy(supersetGroupId = null, supersetOrder = null)
                )
            }
            applyActivePairs(pairs)
            return
        }

        val detached = pairs.removeAt(exerciseIndex).let { pair ->
            pair.copy(exercise = pair.exercise.copy(supersetGroupId = null, supersetOrder = null))
        }
        val remainingGroupIndices = pairs.indices.filter { pairs[it].exercise.supersetGroupId == groupId }
        val insertAfterGroup = (remainingGroupIndices.maxOrNull()?.plus(1) ?: pairs.size).coerceIn(0, pairs.size)
        pairs.add(insertAfterGroup, detached)
        applyActivePairs(pairs)
    }

    fun addExerciseSet(exerciseIndex: Int) {
        viewModelScope.launch {
            val exercises = _selectedExercises.value
            val targetExercise = exercises.getOrNull(exerciseIndex) ?: return@launch
            val targetGroupId = targetExercise.supersetGroupId
            val updatedMap = _exerciseSetsMap.value.toMutableMap()
            val targetIndices = if (targetGroupId != null) {
                exercises.indices.filter { exercises[it].supersetGroupId == targetGroupId }
            } else {
                listOf(exerciseIndex)
            }

            targetIndices.forEach { index ->
                val exercise = exercises[index]
                val sets = updatedMap[index]?.toMutableList() ?: mutableListOf()
                sets.add(defaultSetForExercise(exercise, sets.lastOrNull()))
                updatedMap[index] = sets
            }

            _exerciseSetsMap.value = updatedMap
            if (targetGroupId != null) normalizeActiveSupersetSetCounts(targetGroupId) else persistActiveWorkout()
        }
    }

    fun addExercisesFromTemplate(template: Template) {
        viewModelScope.launch {
            _selectedExercises.value = emptyList()
            _exerciseSetsMap.value = emptyMap()

            template.exercises.forEachIndexed { index, exercise ->
                addExercise(exercise)

                if (exercise.sets.isNotEmpty()) {
                    val updatedMap = _exerciseSetsMap.value.toMutableMap()
                    updatedMap[index] = exercise.sets
                    _exerciseSetsMap.value = updatedMap
                }
            }
            persistActiveWorkout()
        }
    }

    fun updateTemplateSet(templateId: String, exerciseIndex: Int, setIndex: Int, newSet: ExerciseSet) {
        viewModelScope.launch {
            workoutRepository.updateTemplateSet(templateId, exerciseIndex, setIndex, newSet)
        }
    }

    private fun normalizeTemplateSupersetSetCounts(exercises: List<ExtendedExercise>, groupId: String): List<ExtendedExercise> {
        val targetCount = exercises
            .filter { it.supersetGroupId == groupId }
            .maxOfOrNull { it.sets.size }
            ?.coerceAtLeast(1)
            ?: return exercises
        return exercises.map { exercise ->
            if (exercise.supersetGroupId != groupId) return@map exercise
            val result = exercise.sets.toMutableList()
            while (result.size < targetCount) {
                result.add(defaultSetForExercise(exercise, result.lastOrNull()))
            }
            exercise.copy(sets = result)
        }
    }

    private fun normalizeTemplateAllSupersetSetCounts(exercises: List<ExtendedExercise>): List<ExtendedExercise> {
        return exercises.mapNotNull { it.supersetGroupId }.distinct().fold(exercises) { current, groupId ->
            normalizeTemplateSupersetSetCounts(current, groupId)
        }
    }

    private suspend fun mergeTemplateExercisesIntoSuperset(templateId: String, firstIndex: Int, secondIndex: Int) {
        val template = workoutRepository.getTemplateById(templateId).first() ?: return
        val exercises = template.exercises.toMutableList()
        if (firstIndex !in exercises.indices || secondIndex !in exercises.indices) return
        val firstGroup = exercises[firstIndex].supersetGroupId
        val secondGroup = exercises[secondIndex].supersetGroupId
        val groupId = firstGroup ?: secondGroup ?: newSupersetGroupId()
        val firstExerciseId = exercises[firstIndex].exercise.id
        val secondExerciseId = exercises[secondIndex].exercise.id
        val merged = exercises.map { exercise ->
            val currentGroup = exercise.supersetGroupId
            if ((firstGroup != null && currentGroup == firstGroup) || (secondGroup != null && currentGroup == secondGroup) || exercise.exercise.id == firstExerciseId || exercise.exercise.id == secondExerciseId) {
                exercise.copy(supersetGroupId = groupId)
            } else exercise
        }
        val normalized = normalizeTemplateSupersetSetCounts(merged, groupId)
        workoutRepository.updateTemplate(
            template.copy(
                exercises = normalizeSupersetMetadata(normalized),
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun createTemplateSupersetWithNext(templateId: String, exerciseIndex: Int) {
        viewModelScope.launch {
            mergeTemplateExercisesIntoSuperset(templateId, exerciseIndex, exerciseIndex + 1)
        }
    }

    fun createTemplateSupersetWithPrevious(templateId: String, exerciseIndex: Int) {
        viewModelScope.launch {
            mergeTemplateExercisesIntoSuperset(templateId, exerciseIndex - 1, exerciseIndex)
        }
    }

    fun removeTemplateExerciseFromSuperset(templateId: String, exerciseIndex: Int) {
        viewModelScope.launch {
            val template = workoutRepository.getTemplateById(templateId).first() ?: return@launch
            val exercises = template.exercises.toMutableList()
            if (exerciseIndex !in exercises.indices) return@launch
            val groupId = exercises[exerciseIndex].supersetGroupId ?: return@launch
            val groupIndices = exercises.indices.filter { exercises[it].supersetGroupId == groupId }
            if (groupIndices.size <= 2) {
                groupIndices.forEach { index ->
                    exercises[index] = exercises[index].copy(supersetGroupId = null, supersetOrder = null)
                }
            } else {
                val detached = exercises.removeAt(exerciseIndex)
                    .copy(supersetGroupId = null, supersetOrder = null)
                val remainingGroupIndices = exercises.indices.filter { exercises[it].supersetGroupId == groupId }
                val insertAfterGroup = (remainingGroupIndices.maxOrNull()?.plus(1) ?: exercises.size).coerceIn(0, exercises.size)
                exercises.add(insertAfterGroup, detached)
            }
            workoutRepository.updateTemplate(
                template.copy(
                    exercises = normalizeSupersetMetadata(normalizeTemplateAllSupersetSetCounts(exercises)),
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun addSetToTemplate(templateId: String, exerciseIndex: Int) {
        viewModelScope.launch {
            val template = workoutRepository.getTemplateById(templateId).first() ?: return@launch
            val exercises = template.exercises.toMutableList()
            val targetExercise = exercises.getOrNull(exerciseIndex) ?: return@launch
            val groupId = targetExercise.supersetGroupId
            val targetIndices = if (groupId != null) {
                exercises.indices.filter { exercises[it].supersetGroupId == groupId }
            } else {
                listOf(exerciseIndex)
            }

            targetIndices.forEach { index ->
                val exercise = exercises[index]
                exercises[index] = exercise.copy(
                    sets = exercise.sets + defaultSetForExercise(exercise, exercise.sets.lastOrNull())
                )
            }

            val normalized = if (groupId != null) normalizeTemplateSupersetSetCounts(exercises, groupId) else exercises
            workoutRepository.updateTemplate(
                template.copy(
                    exercises = normalizeSupersetMetadata(normalized),
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
    private fun groupBoundsFor(exercises: List<ExtendedExercise>, index: Int): IntRange {
        if (index !in exercises.indices) return 1..0
        val groupId = exercises[index].supersetGroupId ?: return index..index
        var start = index
        var end = index
        while (start > 0 && exercises[start - 1].supersetGroupId == groupId) start--
        while (end < exercises.lastIndex && exercises[end + 1].supersetGroupId == groupId) end++
        return start..end
    }

    private fun moveActiveBlock(index: Int, direction: Int) {
        val pairs = activePairs()
        if (index !in pairs.indices) return
        val exercises = pairs.map { it.exercise }
        val bounds = groupBoundsFor(exercises, index)
        if (bounds.isEmpty()) return

        val groupId = exercises[index].supersetGroupId
        if (groupId != null) {
            val canMoveInsideUp = direction < 0 && index > bounds.first
            val canMoveInsideDown = direction > 0 && index < bounds.last
            if (canMoveInsideUp || canMoveInsideDown) {
                val reordered = pairs.toMutableList()
                val swapIndex = index + if (direction < 0) -1 else 1
                val current = reordered[index]
                reordered[index] = reordered[swapIndex]
                reordered[swapIndex] = current
                applyActivePairs(reordered)
                return
            }
        }

        val insertIndex = if (direction < 0) {
            if (bounds.first == 0) return
            val targetBounds = groupBoundsFor(exercises, bounds.first - 1)
            targetBounds.first
        } else {
            if (bounds.last == pairs.lastIndex) return
            val targetBounds = groupBoundsFor(exercises, bounds.last + 1)
            targetBounds.last + 1
        }

        val block = pairs.subList(bounds.first, bounds.last + 1)
        val remaining = pairs.filterIndexed { pairIndex, _ -> pairIndex !in bounds }
        val adjustedInsertIndex = if (direction < 0) insertIndex else insertIndex - block.size
        val reordered = remaining.toMutableList().apply {
            addAll(adjustedInsertIndex, block)
        }
        applyActivePairs(reordered)
    }

    fun moveExerciseUp(index: Int) {
        moveActiveBlock(index, direction = -1)
    }

    fun moveExerciseDown(index: Int) {
        moveActiveBlock(index, direction = 1)
    }

    fun moveTemplateExerciseUp(templateId: String, index: Int) {
        viewModelScope.launch {
            moveTemplateExerciseBlock(templateId, index, direction = -1)
        }
    }

    fun moveTemplateExerciseDown(templateId: String, index: Int) {
        viewModelScope.launch {
            moveTemplateExerciseBlock(templateId, index, direction = 1)
        }
    }

    private suspend fun moveTemplateExerciseBlock(templateId: String, index: Int, direction: Int) {
        val template = workoutRepository.getTemplateById(templateId).first() ?: return
        val exercises = template.exercises
        if (index !in exercises.indices) return
        val bounds = groupBoundsFor(exercises, index)
        if (bounds.isEmpty()) return

        val groupId = exercises[index].supersetGroupId
        if (groupId != null) {
            val canMoveInsideUp = direction < 0 && index > bounds.first
            val canMoveInsideDown = direction > 0 && index < bounds.last
            if (canMoveInsideUp || canMoveInsideDown) {
                val reordered = exercises.toMutableList()
                val swapIndex = index + if (direction < 0) -1 else 1
                val current = reordered[index]
                reordered[index] = reordered[swapIndex]
                reordered[swapIndex] = current
                workoutRepository.updateTemplate(
                    template.copy(
                        exercises = normalizeSupersetMetadata(normalizeTemplateAllSupersetSetCounts(reordered)),
                        timestamp = System.currentTimeMillis()
                    )
                )
                return
            }
        }

        val insertIndex = if (direction < 0) {
            if (bounds.first == 0) return
            val targetBounds = groupBoundsFor(exercises, bounds.first - 1)
            targetBounds.first
        } else {
            if (bounds.last == exercises.lastIndex) return
            val targetBounds = groupBoundsFor(exercises, bounds.last + 1)
            targetBounds.last + 1
        }
        val block = exercises.subList(bounds.first, bounds.last + 1)
        val remaining = exercises.filterIndexed { exerciseIndex, _ -> exerciseIndex !in bounds }
        val adjustedInsertIndex = if (direction < 0) insertIndex else insertIndex - block.size
        val reordered = remaining.toMutableList().apply { addAll(adjustedInsertIndex, block) }
        workoutRepository.updateTemplate(
            template.copy(
                exercises = normalizeSupersetMetadata(normalizeTemplateAllSupersetSetCounts(reordered)),
                timestamp = System.currentTimeMillis()
            )
        )
    }


    private fun normalizeInsertionIndex(insertIndex: Int, size: Int): Int = insertIndex.coerceIn(0, size)

    private fun moveBlockToIndex(pairs: List<ExerciseWithSets>, sourceBounds: IntRange, insertIndex: Int): List<ExerciseWithSets> {
        if (sourceBounds.isEmpty()) return pairs
        val block = pairs.subList(sourceBounds.first, sourceBounds.last + 1)
        val remaining = pairs.filterIndexed { index, _ -> index !in sourceBounds }
        val adjustedInsertIndex = if (insertIndex > sourceBounds.last) insertIndex - block.size else insertIndex
        return remaining.toMutableList().apply {
            addAll(normalizeInsertionIndex(adjustedInsertIndex, remaining.size), block)
        }
    }

    private fun mergeActiveBlocksIntoSuperset(sourceIndex: Int, targetIndex: Int) {
        val pairs = activePairs()
        if (sourceIndex !in pairs.indices || targetIndex !in pairs.indices) return
        val sourceBounds = groupBoundsFor(pairs.map { it.exercise }, sourceIndex)
        val targetBounds = groupBoundsFor(pairs.map { it.exercise }, targetIndex)
        if (sourceBounds.isEmpty() || targetBounds.isEmpty() || sourceBounds == targetBounds) return

        val sourceGroupId = pairs[sourceIndex].exercise.supersetGroupId
        val targetGroupId = pairs[targetIndex].exercise.supersetGroupId
        val groupId = targetGroupId ?: sourceGroupId ?: newSupersetGroupId()
        val sourceBlock = pairs.subList(sourceBounds.first, sourceBounds.last + 1)
        val targetBlock = pairs.subList(targetBounds.first, targetBounds.last + 1)
        val combined = if (sourceBounds.first < targetBounds.first) sourceBlock + targetBlock else targetBlock + sourceBlock
        val mergedBlock = combined.map { pair ->
            pair.copy(exercise = pair.exercise.copy(supersetGroupId = groupId))
        }
        val remaining = pairs.filterIndexed { index, _ -> index !in sourceBounds && index !in targetBounds }.toMutableList()
        val insertAt = minOf(sourceBounds.first, targetBounds.first).coerceIn(0, remaining.size)
        val reordered = remaining.apply { addAll(insertAt, mergedBlock) }
        applyActivePairs(reordered, persist = false)
        normalizeActiveSupersetSetCounts(groupId)
    }

    fun handleActiveExerciseDragDrop(sourceIndex: Int, targetIndex: Int?, insertIndex: Int) {
        val pairs = activePairs()
        if (sourceIndex !in pairs.indices) return
        if (targetIndex != null && targetIndex in pairs.indices) {
            mergeActiveBlocksIntoSuperset(sourceIndex, targetIndex)
            return
        }
        val sourceBounds = groupBoundsFor(pairs.map { it.exercise }, sourceIndex)
        if (insertIndex in sourceBounds || insertIndex == sourceBounds.last + 1) return
        applyActivePairs(moveBlockToIndex(pairs, sourceBounds, insertIndex))
    }

    private fun moveTemplateBlockToIndex(exercises: List<ExtendedExercise>, sourceBounds: IntRange, insertIndex: Int): List<ExtendedExercise> {
        if (sourceBounds.isEmpty()) return exercises
        val block = exercises.subList(sourceBounds.first, sourceBounds.last + 1)
        val remaining = exercises.filterIndexed { index, _ -> index !in sourceBounds }
        val adjustedInsertIndex = if (insertIndex > sourceBounds.last) insertIndex - block.size else insertIndex
        return remaining.toMutableList().apply {
            addAll(normalizeInsertionIndex(adjustedInsertIndex, remaining.size), block)
        }
    }

    private fun mergeTemplateBlocksIntoSuperset(template: Template, sourceIndex: Int, targetIndex: Int): List<ExtendedExercise> {
        val exercises = template.exercises
        if (sourceIndex !in exercises.indices || targetIndex !in exercises.indices) return exercises
        val sourceBounds = groupBoundsFor(exercises, sourceIndex)
        val targetBounds = groupBoundsFor(exercises, targetIndex)
        if (sourceBounds.isEmpty() || targetBounds.isEmpty() || sourceBounds == targetBounds) return exercises

        val sourceGroupId = exercises[sourceIndex].supersetGroupId
        val targetGroupId = exercises[targetIndex].supersetGroupId
        val groupId = targetGroupId ?: sourceGroupId ?: newSupersetGroupId()
        val sourceBlock = exercises.subList(sourceBounds.first, sourceBounds.last + 1)
        val targetBlock = exercises.subList(targetBounds.first, targetBounds.last + 1)
        val combined = if (sourceBounds.first < targetBounds.first) sourceBlock + targetBlock else targetBlock + sourceBlock
        val mergedBlock = combined.map { exercise -> exercise.copy(supersetGroupId = groupId) }
        val remaining = exercises.filterIndexed { index, _ -> index !in sourceBounds && index !in targetBounds }.toMutableList()
        val insertAt = minOf(sourceBounds.first, targetBounds.first).coerceIn(0, remaining.size)
        val reordered = remaining.apply { addAll(insertAt, mergedBlock) }
        return normalizeTemplateSupersetSetCounts(reordered, groupId)
    }

    fun handleTemplateExerciseDragDrop(templateId: String, sourceIndex: Int, targetIndex: Int?, insertIndex: Int) {
        viewModelScope.launch {
            val template = workoutRepository.getTemplateById(templateId).first() ?: return@launch
            val exercises = template.exercises
            if (sourceIndex !in exercises.indices) return@launch
            val updated = if (targetIndex != null && targetIndex in exercises.indices) {
                mergeTemplateBlocksIntoSuperset(template, sourceIndex, targetIndex)
            } else {
                val sourceBounds = groupBoundsFor(exercises, sourceIndex)
                if (insertIndex in sourceBounds || insertIndex == sourceBounds.last + 1) return@launch
                moveTemplateBlockToIndex(exercises, sourceBounds, insertIndex)
            }
            workoutRepository.updateTemplate(
                template.copy(
                    exercises = normalizeSupersetMetadata(normalizeTemplateAllSupersetSetCounts(updated)),
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

}

class WorkoutViewModelFactory(
    private val workoutRepository: WorkoutRepository, private val timerDataStore: TimerDataStore,private val mainRepository: MainRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()
        return WorkoutViewModel(workoutRepository, savedStateHandle, timerDataStore, mainRepository) as T
    }
}