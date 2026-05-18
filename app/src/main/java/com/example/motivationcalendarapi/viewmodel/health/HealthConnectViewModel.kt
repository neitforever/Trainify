package com.example.motivationcalendarapi.viewmodel.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.repositories.health.HealthConnectRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class HealthConnectViewModel(private val repository: HealthConnectRepository) : ViewModel() {
    private var heartRateJob: Job? = null
    private var profileJob: Job? = null
    private val workoutHeartRateSamples = mutableListOf<Long>()

    private val _uiState = MutableStateFlow(HealthConnectUiState())
    val uiState: StateFlow<HealthConnectUiState> = _uiState.asStateFlow()

    val permissions = repository.permissions
    val permissionContract = repository.permissionContract

    fun refresh() {
        viewModelScope.launch { refreshNow() }
    }

    private suspend fun refreshNow() {
        runCatching {
            if (!repository.isAvailable()) {
                _uiState.value = HealthConnectUiState(isAvailable = false, isSmartWatchDetected = false)
                return
            }

            val granted = repository.hasPermissions()
            _uiState.value = _uiState.value.copy(isAvailable = true, hasPermissions = granted)

            if (!granted) {
                _uiState.value = _uiState.value.copy(
                    todaySteps = null,
                    todayCalories = null,
                    currentHeartRate = null,
                    connectedDevice = null,
                    isSmartWatchDetected = false
                )
                return
            }

            _uiState.value = _uiState.value.copy(
                todaySteps = repository.readTodaySteps(),
                todayCalories = repository.readTodayCalories(),
                currentHeartRate = repository.readLatestHeartRate(),
                connectedDevice = repository.readConnectedDeviceLabel(),
                isSmartWatchDetected = repository.hasHeartRateSource()
            )
        }.onFailure {
            _uiState.value = _uiState.value.copy(
                todaySteps = null,
                todayCalories = null,
                currentHeartRate = null
            )
        }
    }

    fun startProfileUpdates() {
        if (profileJob?.isActive == true) return
        profileJob = viewModelScope.launch {
            while (true) {
                refreshNow()
                delay(5000)
            }
        }
    }

    fun stopProfileUpdates() {
        profileJob?.cancel()
        profileJob = null
    }

    fun startHeartRateUpdates() {
        if (heartRateJob?.isActive == true) return
        workoutHeartRateSamples.clear()
        heartRateJob = viewModelScope.launch {
            while (true) {
                refreshHeartRateOnly()
                delay(10000)
            }
        }
    }

    private suspend fun refreshHeartRateOnly() {
        runCatching {
            if (!repository.isAvailable()) {
                _uiState.value = _uiState.value.copy(isAvailable = false, isSmartWatchDetected = false)
                return
            }

            val granted = repository.hasPermissions()
            _uiState.value = _uiState.value.copy(isAvailable = true, hasPermissions = granted)

            if (!granted) {
                _uiState.value = _uiState.value.copy(
                    currentHeartRate = null,
                    connectedDevice = null,
                    isSmartWatchDetected = false
                )
                return
            }

            val latestHeartRate = repository.readLatestHeartRate()
            if (latestHeartRate != null) {
                workoutHeartRateSamples.add(latestHeartRate)
            }

            _uiState.value = _uiState.value.copy(
                currentHeartRate = latestHeartRate,
                connectedDevice = repository.readConnectedDeviceLabel(),
                isSmartWatchDetected = repository.hasHeartRateSource()
            )
        }.onFailure {
            _uiState.value = _uiState.value.copy(currentHeartRate = null)
        }
    }

    fun stopHeartRateUpdates() {
        heartRateJob?.cancel()
        heartRateJob = null
    }

    suspend fun readAverageHeartRateSince(startMillis: Long): Long? {
        val healthConnectAverage = repository.readAverageHeartRateSince(startMillis)
        if (healthConnectAverage != null) return healthConnectAverage

        val collectedAverage = workoutHeartRateSamples
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.roundToLong()

        return collectedAverage ?: _uiState.value.currentHeartRate
    }

    override fun onCleared() {
        heartRateJob?.cancel()
        profileJob?.cancel()
        super.onCleared()
    }
}

data class HealthConnectUiState(
    val isAvailable: Boolean = true,
    val hasPermissions: Boolean = false,
    val todaySteps: Long? = null,
    val todayCalories: Long? = null,
    val currentHeartRate: Long? = null,
    val connectedDevice: String? = null,
    val isSmartWatchDetected: Boolean = false
)

class HealthConnectViewModelFactory(private val repository: HealthConnectRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HealthConnectViewModel(repository) as T
    }
}
