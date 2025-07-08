package com.example.motivationcalendarapi.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        val LANGUAGE_KEY = stringPreferencesKey("app_language")
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }

        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("app_language", languageCode)
            .apply()
    }

    val languageFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: "system"
        }

    val minRepFlow: Flow<Int> = context.dataStore.data
        .map { it[MIN_REP] ?: 0 }

    val maxRepFlow: Flow<Int> = context.dataStore.data
        .map { it[MAX_REP] ?: 12 }

    val stepRepFlow: Flow<Int> = context.dataStore.data
        .map { it[STEP_REP] ?: 1 }

    val minWeightFlow: Flow<Float> = context.dataStore.data
        .map { it[MIN_WEIGHT] ?: 0f }

    val maxWeightFlow: Flow<Float> = context.dataStore.data
        .map { it[MAX_WEIGHT] ?: 200f }

    val stepWeightFlow: Flow<Float> = context.dataStore.data
        .map { it[STEP_WEIGHT] ?: 2.5f }

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

    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_THEME] != false
        }

//    suspend fun toggleTheme() {
//        context.dataStore.edit { preferences ->
//            val currentTheme = preferences[IS_DARK_THEME] != false
//            preferences[IS_DARK_THEME] = !currentTheme
//        }
//    }
}