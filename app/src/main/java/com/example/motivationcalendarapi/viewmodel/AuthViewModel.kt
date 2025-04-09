package com.example.motivationcalendarapi.viewmodel

import GoogleAuthClient
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.repositories.BodyProgressRepository
import com.example.motivationcalendarapi.repositories.WorkoutRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authClient: GoogleAuthClient,
    private val bodyProgressRepository: BodyProgressRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        viewModelScope.launch {
            authClient.getCurrentUser()?.let { user ->
                _userState.value = UserState.Authenticated(user)
                bodyProgressRepository.syncWithFirestore()
            } ?: run {
                _userState.value = UserState.Unauthenticated
            }
        }
    }

    fun getCurrentUser() = authClient.getCurrentUser()

    private suspend fun syncLocalDataToFirestore() {
        // Существующий код для BodyProgress
        val localBodyProgress = bodyProgressRepository.getAllProgress().first()
        localBodyProgress.forEach { progress ->
            bodyProgressRepository.insert(progress)
        }

        // Добавить синхронизацию тренировок
        val localWorkouts = workoutRepository.getAllWorkouts().first()
        localWorkouts.forEach { workout ->
            workoutRepository.insertWorkout(workout)
        }

        bodyProgressRepository.syncWithFirestore()
        workoutRepository.syncWithFirestore() // Новый метод синхронизации
    }


    fun signIn() {
        viewModelScope.launch(Dispatchers.IO) {
            _userState.value = UserState.Loading
            val success = authClient.signIn()
            if (success) {
                syncLocalDataToFirestore()
                checkAuthState()
            } else {
                _userState.value = UserState.Error("Ошибка авторизации")
            }
        }
    }

    suspend fun signOut() {
        authClient.signOut()
        bodyProgressRepository.deleteAll()
        checkAuthState()
    }

    sealed class UserState {
        object Unauthenticated : UserState()
        object Loading : UserState()
        data class Authenticated(val user: FirebaseUser) : UserState()
        data class Error(val message: String) : UserState()
    }
}