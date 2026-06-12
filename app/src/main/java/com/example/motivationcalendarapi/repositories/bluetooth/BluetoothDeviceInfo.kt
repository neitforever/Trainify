package com.example.motivationcalendarapi.repositories.bluetooth

data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val type: String,
    val isConnected: Boolean
) {
    val isWatch: Boolean
        get() = type.equals("Wearable", ignoreCase = true)
}
