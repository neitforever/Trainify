package com.example.motivationcalendarapi.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.map

val Context.timerDataStore: DataStore<Preferences> by preferencesDataStore(name = "timer_settings")

data class ActiveWorkoutDraft(
    val isStarted: Boolean = false,
    val isRunning: Boolean = false,
    val startTime: Long = 0L,
    val totalPausedDuration: Long = 0L,
    val workoutName: String = "",
    val selectedExercises: List<ExtendedExercise> = emptyList(),
    val exerciseSetsMap: Map<Int, List<ExerciseSet>> = emptyMap()
)

class TimerDataStore(private val context: Context) {

    private val gson = Gson()

    companion object {
        val WARMUP_TIME = intPreferencesKey("warmup_time")
        val ACTIVE_WORKOUT_STARTED = booleanPreferencesKey("active_workout_started")
        val ACTIVE_WORKOUT_RUNNING = booleanPreferencesKey("active_workout_running")
        val ACTIVE_WORKOUT_START_TIME = longPreferencesKey("active_workout_start_time")
        val ACTIVE_WORKOUT_PAUSED_DURATION = longPreferencesKey("active_workout_paused_duration")
        val ACTIVE_WORKOUT_NAME = stringPreferencesKey("active_workout_name")
        val ACTIVE_WORKOUT_EXERCISES_JSON = stringPreferencesKey("active_workout_exercises_json")
        val ACTIVE_WORKOUT_SETS_JSON = stringPreferencesKey("active_workout_sets_json")
        const val DEFAULT_WARMUP_TIME = 60
    }

    val warmupTimeFlow: Flow<Int> = context.timerDataStore.data
        .map { preferences ->
            preferences[WARMUP_TIME] ?: DEFAULT_WARMUP_TIME
        }

    suspend fun saveWarmupTime(time: Int) {
        context.timerDataStore.edit { preferences ->
            preferences[WARMUP_TIME] = time.coerceIn(0, 600)
        }
    }

    val activeWorkoutFlow: Flow<ActiveWorkoutDraft> = context.timerDataStore.data.map { preferences ->
        val exercisesJson = preferences[ACTIVE_WORKOUT_EXERCISES_JSON].orEmpty()
        val setsJson = preferences[ACTIVE_WORKOUT_SETS_JSON].orEmpty()

        val exercisesType = object : TypeToken<List<ExtendedExercise>>() {}.type
        val setsType = object : TypeToken<Map<Int, List<ExerciseSet>>>() {}.type

        ActiveWorkoutDraft(
            isStarted = preferences[ACTIVE_WORKOUT_STARTED] ?: false,
            isRunning = preferences[ACTIVE_WORKOUT_RUNNING] ?: false,
            startTime = preferences[ACTIVE_WORKOUT_START_TIME] ?: 0L,
            totalPausedDuration = preferences[ACTIVE_WORKOUT_PAUSED_DURATION] ?: 0L,
            workoutName = preferences[ACTIVE_WORKOUT_NAME].orEmpty(),
            selectedExercises = runCatching {
                gson.fromJson<List<ExtendedExercise>>(exercisesJson, exercisesType).orEmpty()
            }.getOrDefault(emptyList()),
            exerciseSetsMap = runCatching {
                gson.fromJson<Map<Int, List<ExerciseSet>>>(setsJson, setsType).orEmpty()
            }.getOrDefault(emptyMap())
        )
    }

    suspend fun saveActiveWorkoutDraft(draft: ActiveWorkoutDraft) {
        context.timerDataStore.edit { preferences ->
            preferences[ACTIVE_WORKOUT_STARTED] = draft.isStarted
            preferences[ACTIVE_WORKOUT_RUNNING] = draft.isRunning
            preferences[ACTIVE_WORKOUT_START_TIME] = draft.startTime
            preferences[ACTIVE_WORKOUT_PAUSED_DURATION] = draft.totalPausedDuration
            preferences[ACTIVE_WORKOUT_NAME] = draft.workoutName
            preferences[ACTIVE_WORKOUT_EXERCISES_JSON] = gson.toJson(draft.selectedExercises)
            preferences[ACTIVE_WORKOUT_SETS_JSON] = gson.toJson(draft.exerciseSetsMap)
        }
    }

    suspend fun clearActiveWorkoutDraft() {
        context.timerDataStore.edit { preferences ->
            preferences.remove(ACTIVE_WORKOUT_STARTED)
            preferences.remove(ACTIVE_WORKOUT_RUNNING)
            preferences.remove(ACTIVE_WORKOUT_START_TIME)
            preferences.remove(ACTIVE_WORKOUT_PAUSED_DURATION)
            preferences.remove(ACTIVE_WORKOUT_NAME)
            preferences.remove(ACTIVE_WORKOUT_EXERCISES_JSON)
            preferences.remove(ACTIVE_WORKOUT_SETS_JSON)
        }
    }
}