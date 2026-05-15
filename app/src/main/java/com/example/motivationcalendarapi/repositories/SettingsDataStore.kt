package com.example.motivationcalendarapi.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {


    companion object {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")

        val MIN_REP = intPreferencesKey("min_rep")
        val MAX_REP = intPreferencesKey("max_rep")
        val STEP_REP = intPreferencesKey("step_rep")

        val MIN_WEIGHT = floatPreferencesKey("min_weight")
        val MAX_WEIGHT = floatPreferencesKey("max_weight")
        val STEP_WEIGHT = floatPreferencesKey("step_weight")

        val MIN_CARDIO_TIME = floatPreferencesKey("min_cardio_time")
        val MAX_CARDIO_TIME = floatPreferencesKey("max_cardio_time")
        val STEP_CARDIO_TIME = floatPreferencesKey("step_cardio_time")

        val MIN_RESISTANCE = floatPreferencesKey("min_resistance")
        val MAX_RESISTANCE = floatPreferencesKey("max_resistance")
        val STEP_RESISTANCE = floatPreferencesKey("step_resistance")

        val MIN_INCLINE = floatPreferencesKey("min_incline")
        val MAX_INCLINE = floatPreferencesKey("max_incline")
        val STEP_INCLINE = floatPreferencesKey("step_incline")
    }




    val minRepFlow: Flow<Int> = context.dataStore.data
        .map { it[MIN_REP] ?: 0 }

    val maxRepFlow: Flow<Int> = context.dataStore.data
        .map { it[MAX_REP] ?: 24 }

    val stepRepFlow: Flow<Int> = context.dataStore.data
        .map { it[STEP_REP] ?: 4 }

    val minWeightFlow: Flow<Float> = context.dataStore.data
        .map { it[MIN_WEIGHT] ?: 0f }

    val maxWeightFlow: Flow<Float> = context.dataStore.data
        .map { it[MAX_WEIGHT] ?: 200f }

    val stepWeightFlow: Flow<Float> = context.dataStore.data
        .map { it[STEP_WEIGHT] ?: 20f }

    val minCardioTimeFlow: Flow<Float> = context.dataStore.data
        .map { it[MIN_CARDIO_TIME] ?: 0f }

    val maxCardioTimeFlow: Flow<Float> = context.dataStore.data
        .map { it[MAX_CARDIO_TIME] ?: 120f }

    val stepCardioTimeFlow: Flow<Float> = context.dataStore.data
        .map { it[STEP_CARDIO_TIME] ?: 10f }

    val minResistanceFlow: Flow<Float> = context.dataStore.data
        .map { it[MIN_RESISTANCE] ?: 0f }

    val maxResistanceFlow: Flow<Float> = context.dataStore.data
        .map { it[MAX_RESISTANCE] ?: 12f }

    val stepResistanceFlow: Flow<Float> = context.dataStore.data
        .map { it[STEP_RESISTANCE] ?: 4f }

    val minInclineFlow: Flow<Float> = context.dataStore.data
        .map { it[MIN_INCLINE] ?: 0f }

    val maxInclineFlow: Flow<Float> = context.dataStore.data
        .map { it[MAX_INCLINE] ?: 12f }

    val stepInclineFlow: Flow<Float> = context.dataStore.data
        .map { it[STEP_INCLINE] ?: 4f }

    suspend fun saveRepSettings(min: Int, max: Int, step: Int) {
        context.dataStore.edit { settings ->
            settings[MIN_REP] = min
            settings[MAX_REP] = max
            settings[STEP_REP] = step
        }
    }

    suspend fun saveWeightSettings(min: Float, max: Float, step: Float) {
        context.dataStore.edit { settings ->
            settings[MIN_WEIGHT] = min
            settings[MAX_WEIGHT] = max
            settings[STEP_WEIGHT] = step
        }
    }

    suspend fun saveCardioTimeSettings(min: Float, max: Float, step: Float) {
        context.dataStore.edit { settings ->
            settings[MIN_CARDIO_TIME] = min
            settings[MAX_CARDIO_TIME] = max
            settings[STEP_CARDIO_TIME] = step
        }
    }

    suspend fun saveResistanceSettings(min: Float, max: Float, step: Float) {
        context.dataStore.edit { settings ->
            settings[MIN_RESISTANCE] = min
            settings[MAX_RESISTANCE] = max
            settings[STEP_RESISTANCE] = step
        }
    }

    suspend fun saveInclineSettings(min: Float, max: Float, step: Float) {
        context.dataStore.edit { settings ->
            settings[MIN_INCLINE] = min
            settings[MAX_INCLINE] = max
            settings[STEP_INCLINE] = step
        }
    }

    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_THEME] != false
        }

}