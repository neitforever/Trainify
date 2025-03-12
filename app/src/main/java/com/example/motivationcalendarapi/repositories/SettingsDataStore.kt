package com.example.motivationcalendarapi.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }

    // Получение текущей темы
    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_THEME] ?: true // По умолчанию тема светлая
        }

    // Переключение темы
    suspend fun toggleTheme() {
        context.dataStore.edit { preferences ->
            val currentTheme = preferences[IS_DARK_THEME] ?: true
            preferences[IS_DARK_THEME] = !currentTheme
        }
    }
}