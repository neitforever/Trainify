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
import java.util.Calendar
import java.util.Collections

class WorkoutViewModel(
    val workoutRepository: WorkoutRepository,
    private val savedStateHandle: SavedStateHandle,
    private val timerDataStore: TimerDataStore,
    mainRepository: MainRepository,
) : ViewModel() {

    private val geminiAiGenerationApi = GeminiAiGenerationApi()

    private var isRestoringActiveWorkout = false

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
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        updatedMap[exerciseIndex]?.let { sets ->
            if (setIndex in sets.indices) {
                val newSets = sets.toMutableList().apply {
                    removeAt(setIndex)
                }
                if (newSets.isEmpty()) {
                    updatedMap.remove(exerciseIndex)
                } else {
                    updatedMap[exerciseIndex] = newSets
                }
            }
        }
        _exerciseSetsMap.value = updatedMap
        persistActiveWorkout()
    }

    fun removeTemplateSet(templateId: String, exerciseIndex: Int, setIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.removeTemplateSet(templateId, exerciseIndex, setIndex)
        }
    }

    private val _warmupTime = MutableStateFlow(60)
    val warmupTime: StateFlow<Int> = _warmupTime.asStateFlow()

    init {
        viewModelScope.launch {
            timerDataStore.warmupTimeFlow.collect { time ->
                _warmupTime.value = time
            }
        }

        viewModelScope.launch {
            workoutRepository.initializeDefaultTemplates()
            workoutRepository.initializeRewards()
            workoutRepository.syncTemplatesWithFirestore()
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
                val updated = it.copy(exercises = newExercises)
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
                    exercise.sets.filter { it.status != SetStatus.FAILED }.sumOf { it.weight * it.rep.toDouble() }
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
            .sumOf { (_, set) -> (set.weight * set.rep).toDouble() }
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
                    (set.weight * set.rep).toDouble()
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
                        exerciseSetsMap = _exerciseSetsMap.value
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
            workoutRepository.getAllWorkouts().collect { workouts ->
                _allWorkouts.value = workouts
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

    fun saveWorkout(exercises: List<ExtendedExercise>, averageHeartRate: Long? = null) {
        val savedWorkoutName = workoutName.value
        val savedDuration = timerValue.value
        val savedTimestamp = System.currentTimeMillis()

        viewModelScope.launch {
            val workoutWithoutDifficulty = Workout(
                name = savedWorkoutName,
                duration = savedDuration,
                timestamp = savedTimestamp,
                averageHeartRate = averageHeartRate,
                exercises = exercises
            )
            val workout = workoutWithoutDifficulty.copy(
                difficulty = calculateWorkoutDifficulty(workoutWithoutDifficulty)
            )
            workoutRepository.insertWorkout(workout)
            resetWorkout()
        }
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
            sets[setIndex] = sets[setIndex].copy(rep = newRep)
            updatedMap[exerciseIndex] = sets
            _exerciseSetsMap.value = updatedMap
            persistActiveWorkout()
        }
    }

    fun updateWeight(exerciseIndex: Int, setIndex: Int, newWeight: Float) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val sets = updatedMap[exerciseIndex]?.toMutableList() ?: return
        if (setIndex < sets.size) {
            sets[setIndex] = sets[setIndex].copy(weight = newWeight)
            updatedMap[exerciseIndex] = sets
            _exerciseSetsMap.value = updatedMap
            persistActiveWorkout()
        }
    }

    val totalKg: StateFlow<Float> =
        combine(selectedExercises, exerciseSetsMap) { exercises, setsMap ->
            exercises.indices.sumOf { index ->
                setsMap[index]?.filter { it.status != SetStatus.FAILED }?.sumOf { it.weight.toDouble() * it.rep.toDouble() } ?: 0.0
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


        _selectedExercises.value = updatedExercises
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

    fun addExerciseSet(exerciseIndex: Int) {
        viewModelScope.launch {
            val updatedMap = _exerciseSetsMap.value.toMutableMap()
            val sets = updatedMap[exerciseIndex]?.toMutableList() ?: mutableListOf()

            val exercise = _selectedExercises.value
                .getOrNull(exerciseIndex)
                ?.exercise

            val cardType = exercise?.getCardType() ?: ExerciseCardType.STRENGTH

            val lastSet = sets.lastOrNull()

            val newSet = when (cardType) {
                ExerciseCardType.STRENGTH -> {
                    ExerciseSet(
                        rep = lastSet?.rep ?: minRep.value,
                        weight = lastSet?.weight ?: minWeight.value,
                        status = SetStatus.NONE
                    )
                }

                ExerciseCardType.BIKE -> {
                    ExerciseSet(
                        time = lastSet?.time ?: minCardioTime.value,
                        resistance = lastSet?.resistance ?: minResistance.value,
                        status = SetStatus.NONE
                    )
                }

                ExerciseCardType.TREADMILL -> {
                    ExerciseSet(
                        time = lastSet?.time ?: minCardioTime.value,
                        resistance = lastSet?.resistance ?: minResistance.value,
                        incline = lastSet?.incline ?: minIncline.value,
                        status = SetStatus.NONE
                    )
                }
            }

            sets.add(newSet)
            updatedMap[exerciseIndex] = sets
            _exerciseSetsMap.value = updatedMap
            persistActiveWorkout()
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

    fun addSetToTemplate(templateId: String, exerciseIndex: Int) {
        viewModelScope.launch {
            val template = workoutRepository
                .getTemplateById(templateId)
                .first()

            val extendedExercise = template
                ?.exercises
                ?.getOrNull(exerciseIndex)
                ?: return@launch

            val cardType = extendedExercise.exercise.getCardType()
            val lastSet = extendedExercise.sets.lastOrNull()

            val newSet = when (cardType) {
                ExerciseCardType.STRENGTH -> {
                    ExerciseSet(
                        rep = lastSet?.rep ?: minRep.value,
                        weight = lastSet?.weight ?: minWeight.value,
                        status = SetStatus.NONE
                    )
                }

                ExerciseCardType.BIKE -> {
                    ExerciseSet(
                        time = lastSet?.time ?: minCardioTime.value,
                        resistance = lastSet?.resistance ?: minResistance.value,
                        status = SetStatus.NONE
                    )
                }

                ExerciseCardType.TREADMILL -> {
                    ExerciseSet(
                        time = lastSet?.time ?: minCardioTime.value,
                        resistance = lastSet?.resistance ?: minResistance.value,
                        incline = lastSet?.incline ?: minIncline.value,
                        status = SetStatus.NONE
                    )
                }
            }

            workoutRepository.addSetToTemplate(
                templateId = templateId,
                exerciseIndex = exerciseIndex,
                newSet = newSet
            )
        }
    }
    fun moveExerciseUp(index: Int) {
        if (index <= 0) return
        val exercises = _selectedExercises.value.toMutableList().apply {
            Collections.swap(this, index, index - 1)
        }
        _selectedExercises.value = exercises

        val updatedMap = _exerciseSetsMap.value.toMutableMap().apply {
            val current = get(index)
            val prev = get(index - 1)
            put(index - 1, current ?: emptyList())
            put(index, prev ?: emptyList())
        }
        _exerciseSetsMap.value = updatedMap
    }

    fun moveExerciseDown(index: Int) {
        if (index >= _selectedExercises.value.size - 1) return
        val exercises = _selectedExercises.value.toMutableList().apply {
            Collections.swap(this, index, index + 1)
        }
        _selectedExercises.value = exercises

        val updatedMap = _exerciseSetsMap.value.toMutableMap().apply {
            val current = get(index)
            val next = get(index + 1)
            put(index + 1, current ?: emptyList())
            put(index, next ?: emptyList())
        }
        _exerciseSetsMap.value = updatedMap
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