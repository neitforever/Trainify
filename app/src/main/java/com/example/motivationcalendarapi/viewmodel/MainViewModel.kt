package com.example.motivationcalendarapi.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.repositories.MainRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MainRepository) : ViewModel() {

    var isDarkTheme by mutableStateOf(true)
        private set

    init {
        viewModelScope.launch {
            repository.isDarkThemeFlow.collect { theme ->
                isDarkTheme = theme
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            repository.toggleTheme()
        }
    }
}

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(MainRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}