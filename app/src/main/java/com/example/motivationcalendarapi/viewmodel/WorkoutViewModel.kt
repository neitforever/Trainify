package com.example.motivationcalendarapi.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.repositories.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class WorkoutViewModel(
    val workoutRepository: WorkoutRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {


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

    private val _timerRunning = MutableStateFlow(savedStateHandle.get<Boolean>(TIMER_RUNNING_KEY) ?: false)
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
    }

    fun getWorkoutById(id: Long): Workout {
        return workoutRepository.getWorkoutById(id)
    }


    // Список всех тренировок
    private val _allWorkouts = MutableStateFlow<List<Workout>>(emptyList())
    val allWorkouts: StateFlow<List<Workout>> = _allWorkouts.asStateFlow()



    // Название тренировки
    private val _workoutName = MutableStateFlow("")
    val workoutName: StateFlow<String> = _workoutName.asStateFlow()

    // Состояние тренировки (идёт или нет)
    private val _isWorkoutStarted = MutableStateFlow(false)
    val isWorkoutStarted: StateFlow<Boolean> = _isWorkoutStarted.asStateFlow()

    // Состояние для хранения тренировки по ID
    private val _currentWorkout = MutableStateFlow<Workout?>(null)
    val currentWorkout: StateFlow<Workout?> get() = _currentWorkout

    // Состояние загрузки тренировки
    private val _isLoadingWorkout = MutableStateFlow(false)
    val isLoadingWorkout: StateFlow<Boolean> = _isLoadingWorkout.asStateFlow()

    // Список выбранных упражнений
    private val _selectedExercises = MutableStateFlow<List<ExtendedExercise>>(emptyList())
    val selectedExercises: StateFlow<List<ExtendedExercise>> = _selectedExercises.asStateFlow()

    // Карта подходов для упражнений
    private val _exerciseSetsMap = MutableStateFlow<Map<Int, List<ExerciseSet>>>(emptyMap())
    val exerciseSetsMap: StateFlow<Map<Int, List<ExerciseSet>>> = _exerciseSetsMap.asStateFlow()

    private val _showOverwriteDialog = MutableStateFlow(false)
    val showOverwriteDialog: StateFlow<Boolean> = _showOverwriteDialog.asStateFlow()

    private val _workoutsToday = MutableStateFlow<List<Workout>>(emptyList())
    val workoutsToday: StateFlow<List<Workout>> = _workoutsToday.asStateFlow()



    init {
        // Восстановление таймера при создании ViewModel
        if (_timerRunning.value) {
            startTime = System.currentTimeMillis() - totalPausedDuration
        }

        // Запуск отдельной корутины для сбора данных о тренировках
        viewModelScope.launch {
            workoutRepository.getWorkoutsToday().collect {
                _workoutsToday.value = it
            }
        }

        // Запуск отдельной корутины для таймера
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


    fun workoutsByMonthAndWeek(): Flow<Map<String, Map<Int, List<Workout>>>> {
        return allWorkouts.map { workouts ->
            workouts.groupBy { workout ->
                // Группировка по месяцам
                val instant = Instant.ofEpochMilli(workout.timestamp)
                val monthFormatter =
                    DateTimeFormatter.ofPattern("MMMM yyyy").withZone(ZoneId.systemDefault())
                monthFormatter.format(instant)
            }.mapValues { (_, monthWorkouts) ->
                // Внутри месяца группировка по неделям, где неделя - каждые 7 дней
                monthWorkouts.groupBy { workout ->
                    calculateWeekOfMonth(workout.timestamp)
                }
            }
        }
    }

    private fun calculateWeekOfMonth(timestamp: Long): Int {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        return (dayOfMonth - 1) / 7 + 1
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

    // Загрузка всех тренировок из базы данных
    fun loadWorkouts() {
        viewModelScope.launch {
            // Используем collect для получения данных из Flow
            workoutRepository.getAllWorkouts().collect { workouts ->
                _allWorkouts.value = workouts
            }
        }
    }





    // Завершить тренировку (таймер останавливается, но данные сохраняются)
    fun finishWorkout() {
        _timerRunning.value = false
        _isWorkoutStarted.value = false
    }


    // Установить название тренировки
    fun setWorkoutName(name: String) {
        _workoutName.value = name
    }


    // Сохранить текущую тренировку в базу данных
    // WorkoutViewModel.kt
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

    // Удалить тренировку из базы данных
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            workoutRepository.delete(workout)
            loadWorkouts()
            _currentWorkout.value = null // Сбрасываем текущую тренировку
        }

    }


    // Добавление упражнения
    fun addExercise(exercise: ExtendedExercise) {
        _selectedExercises.value = _selectedExercises.value + exercise
    }

    // Удаление упражнения
    fun removeExercise(index: Int) {
        _selectedExercises.value = _selectedExercises.value.toMutableList().also { it.removeAt(index) }
    }

    // Добавление подхода к упражнению
    fun addExerciseSet(exerciseId: Int, set: ExerciseSet) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        val sets = updatedMap[exerciseId]?.toMutableList() ?: mutableListOf()
        sets.add(set)
        updatedMap[exerciseId] = sets
        _exerciseSetsMap.value = updatedMap
    }

    // Удаление подхода
    fun removeExerciseSet(exerciseId: Int, index: Int) {
        val updatedMap = _exerciseSetsMap.value.toMutableMap()
        updatedMap[exerciseId]?.toMutableList()?.let { sets ->
            if (index in sets.indices) {
                sets.removeAt(index)
                updatedMap[exerciseId] = sets
            }
        }
        _exerciseSetsMap.value = updatedMap
    }
}

class WorkoutViewModelFactory(
    private val workoutRepository: WorkoutRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()
        return WorkoutViewModel(workoutRepository, savedStateHandle) as T
    }
}