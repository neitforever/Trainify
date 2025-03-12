package com.example.motivationcalendarapi.repositories

import android.content.Context
import kotlinx.coroutines.flow.Flow

class MainRepository(private val context: Context) {


    private val settingsDataStore = SettingsDataStore(context)

    val isDarkThemeFlow: Flow<Boolean> = settingsDataStore.isDarkThemeFlow

    suspend fun toggleTheme() {
        settingsDataStore.toggleTheme()
    }
}