package com.example.motivationcalendarapi.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.motivationcalendarapi.repositories.SettingsDataStore.Companion.IS_DARK_THEME
import kotlinx.coroutines.flow.Flow

class MainRepository(private val context: Context) {


    private val settingsDataStore = SettingsDataStore(context)

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