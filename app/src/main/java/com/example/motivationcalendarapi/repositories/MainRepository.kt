package com.example.motivationcalendarapi.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.motivationcalendarapi.repositories.SettingsDataStore.Companion.IS_DARK_THEME
import kotlinx.coroutines.flow.Flow

class MainRepository(private val context: Context) {


    private val settingsDataStore = SettingsDataStore(context)


    val minRepFlow: Flow<Int> = settingsDataStore.minRepFlow
    val maxRepFlow: Flow<Int> = settingsDataStore.maxRepFlow
    val stepRepFlow: Flow<Int> = settingsDataStore.stepRepFlow

    val minWeightFlow: Flow<Float> = settingsDataStore.minWeightFlow
    val maxWeightFlow: Flow<Float> = settingsDataStore.maxWeightFlow
    val stepWeightFlow: Flow<Float> = settingsDataStore.stepWeightFlow

    suspend fun saveRepSettings(min: Int, max: Int, step: Int) {
        settingsDataStore.saveRepSettings(min, max, step)
    }

    suspend fun saveWeightSettings(min: Float, max: Float, step: Float) {
        settingsDataStore.saveWeightSettings(min, max, step)
    }

    val isDarkThemeFlow: Flow<Boolean> = settingsDataStore.isDarkThemeFlow

    suspend fun toggleTheme() {
        settingsDataStore.toggleTheme()
    }

    suspend fun saveThemePreference(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDark
        }
    }
}