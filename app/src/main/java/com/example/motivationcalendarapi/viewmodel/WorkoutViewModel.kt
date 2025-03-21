package com.example.motivationcalendarapi.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.repositories.TimerDataStore
import com.example.motivationcalendarapi.repositories.WorkoutRepository
import com.example.motivationcalendarapi.model.DifficultyLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Collections

class WorkoutViewModel(
    val workoutRepository: WorkoutRepository,
    private val savedStateHandle: SavedStateHandle,
    private val timerDataStore: TimerDataStore
) : ViewModel() {


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
            workoutRepository.getExerciseNotesUpdates().collect { exercises ->
                    val notesMap = exercises.associate { it.id to it.note }
                    _notesUpdates.value = notesMap
                }
        }
    }

    fun updateWarmupTime(newTime: Int) {
        _warmupTime.value = newTime
        viewModelScope.launch {
            timerDataStore.saveWarmupTime(newTime)
        }
    }

    private companion object {
        const val START_TIME_KEY = "start_time"
        const val PAUSED_DURATION_KEY = "paused_duration"
        const val TIMER_RUNNING_KEY = "timer_running"
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
        }
    }

    fun pauseTimer() {
        if (_timerRunning.value) {
            _timerRunning.value = false
            savedStateHandle.set(TIMER_RUNNING_KEY, false)
            totalPausedDuration = System.currentTimeMillis() - startTime
        }
    }

    fun resumeTimer() {
        if (!_timerRunning.value) {
            _timerRunning.value = true
            savedStateHandle.set(TIMER_RUNNING_KEY, true)
            startTime = System.currentTimeMillis() - totalPausedDuration
        }
    }

    fun resetWorkout() {
        _timerRunning.value = false
        savedStateHandle.set(TIMER_RUNNING_KEY, false)
        _timerValue.value = 0
        _workoutName.value = ""
        _isWorkoutStarted.value = false
        startTime = 0L
        totalPausedDuration = 0L
        _selectedExercises.value = emptyList()
        _exerciseSetsMap.value = emptyMap()
    }

    fun getWorkoutById(id: Long): Workout {
        return workoutRepository.getWorkoutById(id)
    }


    private val _allWorkouts = MutableStateFlow<List<Workout>>(emptyList())
    val allWorkouts: StateFlow<List<Workout>> = _allWorkouts.asStateFlow()


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

    private val _isWorkoutStarted = MutableStateFlow(false)
    val isWorkoutStarted: StateFlow<Boolean> = _isWorkoutStarted.asStateFlow()

    private val _currentWorkout = MutableStateFlow<Workout?>(null)
    val currentWorkout: StateFlow<Workout?> get() = _currentWorkout

    private val _isLoadingWorkout = MutableStateFlow(false)
    val isLoadingWorkout: StateFlow<Boolean> = _isLoadingWorkout.asStateFlow()

    private val _selectedExercises = MutableStateFlow<List<ExtendedExercise>>(emptyList())
    val selectedExercises: StateFlow<List<ExtendedExercise>> = _selectedExercises.asStateFlow()

    private val _exerciseSetsMap = MutableStateFlow<Map<Int, List<ExerciseSet>>>(emptyMap())
    val exerciseSetsMap: StateFlow<Map<Int, List<ExerciseSet>>> = _exerciseSetsMap.asStateFlow()

    private val _showOverwriteDialog = MutableStateFlow(false)
    val showOverwriteDialog: StateFlow<Boolean> = _showOverwriteDialog.asStateFlow()

    private val _workoutsToday = MutableStateFlow<List<Workout>>(emptyList())
    val workoutsToday: StateFlow<List<Workout>> = _workoutsToday.asStateFlow()


    init {
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

    fun checkForExistingWorkout() {
        if (workoutsToday.value.isNotEmpty()) {
            _showOverwriteDialog.value = true
        } else {
            startWorkout()
        }
    }

    fun confirmOverwrite() {
        viewModelScope.launch {
            workoutsToday.value.forEach { workout ->
                workoutRepository.delete(workout)
            }
            _showOverwriteDialog.value = false
            startWorkout()
        }
    }

    fun dismissOverwriteDialog() {
        _showOverwriteDialog.value = false
    }

    private fun startWorkout() {
        _isWorkoutStarted.value = true
        startTimer()
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

    fun workoutsByYearMonthAndWeek(): Flow<Map<Int, Map<String, Map<Int, List<Workout>>>>> {
        return allWorkouts.map { workouts ->
            workouts.groupBy { workout ->
                Instant.ofEpochMilli(workout.timestamp)
                    .atZone(ZoneId.systemDefault()).year
            }.mapValues { yearEntry ->
                yearEntry.value.groupBy { workout ->
                    DateTimeFormatter.ofPattern("MMMM")
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.ofEpochMilli(workout.timestamp))
                }.mapValues { monthEntry ->
                    monthEntry.value.groupBy { workout ->
                        calculateWeekOfMonth(workout.timestamp)
                    }
                }
            }
                .toSortedMap(compareBy { it })
                .mapValues { yearEntry ->
                    yearEntry.value.toSortedMap(Comparator { m1, m2 ->
                        Month.valueOf(m1.uppercase()).compareTo(Month.valueOf(m2.uppercase()))
                    })
                        .mapValues { monthEntry ->
                            monthEntry.value.toSortedMap(compareBy { it })
                        }
                }
        }
    }

    fun calculateWorkoutDifficulty(workout: Workout): DifficultyLevel {
        var totalWeight = 0f
        var totalReps = 0
        var totalSets = 0

        workout.exercises.forEach { exercise ->
            exercise.sets.forEach { set ->
                totalWeight += set.weight
                totalReps += set.rep
                totalSets++
            }
        }

        if (totalSets == 0) return DifficultyLevel.EASY

        val avgWeight = totalWeight / totalSets
        val avgReps = totalReps.toFloat() / totalSets

        return when {
            avgWeight > 70f && avgReps > 8 -> DifficultyLevel.HARD
            avgWeight > 40f && avgReps > 5 -> DifficultyLevel.NORMAL
            else -> DifficultyLevel.EASY
        }
    }


    private fun calculateWeekOfMonth(timestamp: Long): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            firstDayOfWeek = Calendar.MONDAY
        }
        return calendar.get(Calendar.WEEK_OF_MONTH)
    }




    fun getWorkoutOrderInMonth(workout: Workout, workouts: List<Workout>): Int {
        val allInMonth = workouts.filter { other ->
            val workoutDate =
                Instant.ofEpochMilli(workout.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            val otherDate =
                Instant.ofEpochMilli(other.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            workoutDate.year == otherDate.year && workoutDate.month == otherDate.month
        }
        return allInMonth.indexOf(workout) + 1
    }


    fun updateWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.updateWorkout(workout)
        }
    }

    fun loadWorkouts() {
        viewModelScope.launch {
            workoutRepository.getAllWorkouts().collect { workouts ->
                _allWorkouts.value = workouts
            }
        }
    }


    fun finishWorkout() {
        _timerRunning.value = false
        _isWorkoutStarted.value = false
    }


    fun setWorkoutName(name: String) {
        _workoutName.value = name
    }


    fun saveWorkout(exercises: List<ExtendedExercise>) {
        viewModelScope.launch {
            val workout = Workout(
                name = workoutName.value,
                duration = timerValue.value,
                timestamp = System.currentTimeMillis(),
                exercises = exercises
            )
            workoutRepository.insertWorkout(workout)
            resetWorkout()
        }
    }


    fun formatWorkoutName(name: String): String {
        return if (name.isBlank()) "Blank" else name
    }

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
        updatedMap[currentSize] = listOf(ExerciseSet(rep = 0, weight = 0f))
        _exerciseSetsMap.value = updatedMap
    }

    fun updateRep(exerciseIndex: Int, setIndex: Int, newRep: Int) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val sets = updatedMap[exerciseIndex]?.toMutableList() ?: return
        if (setIndex < sets.size) {
            sets[setIndex] = sets[setIndex].copy(rep = newRep)
            updatedMap[exerciseIndex] = sets
            _exerciseSetsMap.value = updatedMap
        }
    }

    fun updateWeight(exerciseIndex: Int, setIndex: Int, newWeight: Float) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val sets = updatedMap[exerciseIndex]?.toMutableList() ?: return
        if (setIndex < sets.size) {
            sets[setIndex] = sets[setIndex].copy(weight = newWeight)
            updatedMap[exerciseIndex] = sets
            _exerciseSetsMap.value = updatedMap
        }
    }

    val totalKg: StateFlow<Float> =
        combine(selectedExercises, exerciseSetsMap) { exercises, setsMap ->
            exercises.indices.sumOf { index ->
                setsMap[index]?.sumOf { it.weight.toDouble() } ?: 0.0
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
    }

    fun addExerciseSet(exerciseId: Int) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val sets = updatedMap[exerciseId]?.toMutableList() ?: mutableListOf()

        val newSet = sets.lastOrNull()?.copy() ?: ExerciseSet(rep = 0, weight = 0f)
        sets.add(newSet)

        updatedMap[exerciseId] = sets
        _exerciseSetsMap.value = updatedMap
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
    private val workoutRepository: WorkoutRepository, private val timerDataStore: TimerDataStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()
        return WorkoutViewModel(workoutRepository, savedStateHandle, timerDataStore) as T
    }
}