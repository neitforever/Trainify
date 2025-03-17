package com.example.motivationcalendarapi.viewmodel

import GoogleAuthClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authClient: GoogleAuthClient
) : ViewModel() {
    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        authClient.getCurrentUser()?.let {
            _userState.value = UserState.Authenticated(it)
        } ?: run {
            _userState.value = UserState.Unauthenticated
        }
    }

    fun getCurrentUser() = authClient.getCurrentUser()

    fun signIn() {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            val success = authClient.signIn()
            if (success) {
                checkAuthState()
            } else {
                _userState.value = UserState.Error("Ошибка авторизации")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authClient.signOut()
            checkAuthState()
        }
    }

    sealed class UserState {
        object Unauthenticated : UserState()
        object Loading : UserState()
        data class Authenticated(val user: FirebaseUser) : UserState()
        data class Error(val message: String) : UserState()
    }
}