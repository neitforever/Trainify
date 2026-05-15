package com.example.motivationcalendarapi.viewmodel

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.repositories.MainRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MainRepository) : ViewModel() {

    fun setLanguage(languageCode: String, context: Context) {
        val normalizedLanguageCode = languageCode.ifBlank { "en" }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            val currentLanguageCode = localeManager.applicationLocales.toLanguageTags()

            if (currentLanguageCode == normalizedLanguageCode) {
                repository.saveLanguage(normalizedLanguageCode)
                return
            }

            repository.saveLanguage(normalizedLanguageCode)
            localeManager.applicationLocales = LocaleList.forLanguageTags(normalizedLanguageCode)
        } else {
            val currentLanguageCode = AppCompatDelegate
                .getApplicationLocales()
                .toLanguageTags()

            if (currentLanguageCode == normalizedLanguageCode) {
                repository.saveLanguage(normalizedLanguageCode)
                return
            }

            repository.saveLanguage(normalizedLanguageCode)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(normalizedLanguageCode))
        }
    }

    fun getSavedLanguageCode(): String? {
        return repository.getSavedLanguageCode()
    }


    var isDarkTheme by mutableStateOf(repository.getSavedThemePreference())
        private set

    init {
        viewModelScope.launch {
            repository.isDarkThemeFlow.collect { theme ->
                isDarkTheme = theme
            }
        }
    }


    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            repository.saveThemePreference(isDark)
            isDarkTheme = isDark
        }
    }
}

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return MainViewModel(MainRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}