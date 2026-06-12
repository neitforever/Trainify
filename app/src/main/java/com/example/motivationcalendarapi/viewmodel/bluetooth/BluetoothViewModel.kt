package com.example.motivationcalendarapi.viewmodel.bluetooth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.repositories.bluetooth.BluetoothDeviceInfo
import com.example.motivationcalendarapi.repositories.bluetooth.BluetoothRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BluetoothViewModel(
    private val repository: BluetoothRepository
) : ViewModel() {

    private var connectionUpdatesJob: Job? = null

    /**
     * Explicit Bluetooth events are the most important signal.
     * They have priority over repository polling because vendor Wearable devices may leave
     * stale values in profile/hidden connection APIs after disconnect.
     */
    private val eventConnectionStateByAddress = mutableMapOf<String, Boolean>()

    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()

    fun refreshDevices() {
        val hasPermission = repository.hasBluetoothConnectPermission()
        val rawDevices = if (hasPermission) repository.getBondedDevices() else emptyList()
        val devices = rawDevices.map { device ->
            val eventState = eventConnectionStateByAddress[device.address]
            device.copy(isConnected = eventState ?: device.isConnected)
        }

        _uiState.value = BluetoothUiState(
            hasPermission = hasPermission,
            devices = devices,
            smartWatch = devices.firstOrNull { it.isWatch }
        )
    }

    fun onBluetoothDeviceConnectionChanged(address: String?, isConnected: Boolean) {
        if (!address.isNullOrBlank()) {
            eventConnectionStateByAddress[address] = isConnected
        }
        refreshDevices()
    }

    fun onBluetoothAdapterConnectionStateChanged(isConnected: Boolean) {
        val watchAddresses = _uiState.value.devices
            .filter { it.isWatch }
            .map { it.address }

        if (watchAddresses.isEmpty()) {
            refreshDevices()
            return
        }

        watchAddresses.forEach { address ->
            eventConnectionStateByAddress[address] = isConnected
        }
        refreshDevices()
    }

    fun startConnectionUpdates() {
        if (connectionUpdatesJob?.isActive == true) return

        connectionUpdatesJob = viewModelScope.launch {
            while (true) {
                refreshDevices()
                delay(2000)
            }
        }
    }

    fun stopConnectionUpdates() {
        connectionUpdatesJob?.cancel()
        connectionUpdatesJob = null
    }

    override fun onCleared() {
        connectionUpdatesJob?.cancel()
        super.onCleared()
    }
}

data class BluetoothUiState(
    val hasPermission: Boolean = false,
    val devices: List<BluetoothDeviceInfo> = emptyList(),
    val smartWatch: BluetoothDeviceInfo? = null
)

class BluetoothViewModelFactory(
    private val repository: BluetoothRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BluetoothViewModel(repository) as T
    }
}
