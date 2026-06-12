package com.example.motivationcalendarapi.repositories.bluetooth

import android.Manifest
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class BluetoothRepository(
    private val context: Context
) {
    fun hasBluetoothConnectPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getBondedDevices(): List<BluetoothDeviceInfo> {
        if (!hasBluetoothConnectPermission()) return emptyList()

        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val adapter = bluetoothManager?.adapter ?: return emptyList()
        val connectedAddresses = bluetoothManager.getConnectedDeviceAddresses()

        return adapter.bondedDevices
            .orEmpty()
            .map { device ->
                val address = runCatching { device.address }.getOrNull() ?: "Unknown address"
                val type = device.bluetoothClass?.majorDeviceClass?.toBluetoothType() ?: "Unknown type"
                val isConnectedByProfiles = connectedAddresses.contains(address)
                val isConnectedByDevice = device.isConnectedCompat()

                BluetoothDeviceInfo(
                    name = runCatching { device.name }.getOrNull() ?: "Unknown device",
                    address = address,
                    type = type,
                    isConnected = isConnectedByProfiles || isConnectedByDevice
                )
            }
            .sortedWith(
                compareByDescending<BluetoothDeviceInfo> { it.isWatch }
                    .thenByDescending { it.isConnected }
                    .thenBy { it.name.lowercase() }
            )
    }

    private fun BluetoothManager.getConnectedDeviceAddresses(): Set<String> {
        val profiles = listOf(
            BluetoothProfile.GATT,
            BluetoothProfile.GATT_SERVER,
            BluetoothProfile.A2DP,
            BluetoothProfile.HEADSET,
            BluetoothProfile.HEALTH
        )

        return profiles
            .flatMap { profile ->
                runCatching { getConnectedDevices(profile).orEmpty() }.getOrDefault(emptyList())
            }
            .mapNotNull { device -> runCatching { device.address }.getOrNull() }
            .toSet()
    }

    /**
     * Android has no public, stable API that reliably reports the active connection state
     * for every Wearable vendor. This method is used only as a secondary signal.
     * Broadcast events in BluetoothViewModel still have priority over this value.
     */
    private fun BluetoothDevice.isConnectedCompat(): Boolean {
        return runCatching {
            val method = javaClass.getMethod("isConnected")
            method.invoke(this) as? Boolean ?: false
        }.getOrDefault(false)
    }

    private fun Int.toBluetoothType(): String {
        return when (this) {
            BluetoothClass.Device.Major.COMPUTER -> "Computer"
            BluetoothClass.Device.Major.PHONE -> "Phone"
            BluetoothClass.Device.Major.AUDIO_VIDEO -> "Audio / Video"
            BluetoothClass.Device.Major.PERIPHERAL -> "Peripheral"
            BluetoothClass.Device.Major.HEALTH -> "Health"
            BluetoothClass.Device.Major.WEARABLE -> "Wearable"
            BluetoothClass.Device.Major.TOY -> "Toy"
            else -> "Other"
        }
    }
}
