package com.example.motivationcalendarapi.viewmodel

import GoogleAuthClient
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.repositories.BodyProgressRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authClient: GoogleAuthClient,
    private val bodyProgressRepository: BodyProgressRepository
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
        val localData = bodyProgressRepository.getAllProgress().first()
        localData.forEach { progress ->
            bodyProgressRepository.insert(progress)
        }

        bodyProgressRepository.syncWithFirestore()
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