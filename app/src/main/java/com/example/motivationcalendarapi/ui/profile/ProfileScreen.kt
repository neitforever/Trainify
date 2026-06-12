package com.example.motivationcalendarapi.ui.profile

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.motivationcalendarapi.repositories.bluetooth.BluetoothRepository
import com.example.motivationcalendarapi.viewmodel.bluetooth.BluetoothViewModel
import com.example.motivationcalendarapi.viewmodel.bluetooth.BluetoothViewModelFactory
import com.example.motivationcalendarapi.ui.profile.fragments.HealthConnectCard
import com.example.motivationcalendarapi.ui.profile.fragments.WatchDataCard
import com.example.motivationcalendarapi.viewmodel.health.HealthConnectViewModelFactory
import com.example.motivationcalendarapi.viewmodel.health.HealthConnectViewModel
import com.example.motivationcalendarapi.repositories.health.HealthConnectRepository
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.ui.profile.fragments.LegendRow
import com.example.motivationcalendarapi.ui.profile.fragments.LogoutButton
import com.example.motivationcalendarapi.ui.profile.fragments.ProfileHeader
import com.example.motivationcalendarapi.ui.profile.fragments.StatsRow
import com.example.motivationcalendarapi.ui.profile.rewards.RewardsSection
import com.example.motivationcalendarapi.ui.profile.profile_calendar.ProfileCalendarView
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    workoutViewModel: WorkoutViewModel,
    paddingValues: Dp,
) {
    val coroutineScope = rememberCoroutineScope()
    val allWorkouts by workoutViewModel.allWorkouts.collectAsState()
    val weekReps by workoutViewModel.weekReps.collectAsState()
    val weekWeight by workoutViewModel.weekWeight.collectAsState()
    val rewards by workoutViewModel.rewards.collectAsState()
    val context = LocalContext.current
    val healthViewModel: HealthConnectViewModel = viewModel(
        factory = HealthConnectViewModelFactory(HealthConnectRepository(context))
    )
    val healthState by healthViewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val healthPermissionsLauncher = rememberLauncherForActivityResult(
        contract = healthViewModel.permissionContract
    ) { healthViewModel.refresh() }

    val bluetoothViewModel: BluetoothViewModel = viewModel(
        factory = BluetoothViewModelFactory(BluetoothRepository(context))
    )
    val bluetoothState by bluetoothViewModel.uiState.collectAsState()
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            bluetoothViewModel.refreshDevices()
        }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        val device = intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )
                        bluetoothViewModel.onBluetoothDeviceConnectionChanged(
                            address = device?.address,
                            isConnected = true
                        )
                        coroutineScope.launch {
                            delay(600)
                            bluetoothViewModel.refreshDevices()
                        }
                    }

                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        val device = intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )
                        bluetoothViewModel.onBluetoothDeviceConnectionChanged(
                            address = device?.address,
                            isConnected = false
                        )
                        coroutineScope.launch {
                            delay(600)
                            bluetoothViewModel.onBluetoothDeviceConnectionChanged(
                                address = device?.address,
                                isConnected = false
                            )
                        }
                    }

                    BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                        val connectionState = intent.getIntExtra(
                            BluetoothAdapter.EXTRA_CONNECTION_STATE,
                            BluetoothAdapter.ERROR
                        )
                        when (connectionState) {
                            BluetoothAdapter.STATE_CONNECTED -> {
                                bluetoothViewModel.onBluetoothAdapterConnectionStateChanged(true)
                            }

                            BluetoothAdapter.STATE_DISCONNECTED -> {
                                bluetoothViewModel.onBluetoothAdapterConnectionStateChanged(false)
                            }

                            else -> bluetoothViewModel.refreshDevices()
                        }
                        coroutineScope.launch {
                            delay(600)
                            bluetoothViewModel.refreshDevices()
                        }
                    }

                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                        bluetoothViewModel.refreshDevices()
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.checkAuthState()
        workoutViewModel.loadWorkouts()
        healthViewModel.refresh()

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothViewModel.refreshDevices()
        } else {
            bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }

    LaunchedEffect(healthState.todaySteps) {
        workoutViewModel.evaluateDailyStepsForRewards(healthState.todaySteps)
    }

    LaunchedEffect(healthState.hasPermissions) {
        if (healthState.hasPermissions) {
            workoutViewModel.unlockHealthConnectConnectedForRewards()
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothViewModel.startConnectionUpdates()
            }
            healthViewModel.startProfileUpdates()
            try {
                awaitCancellation()
            } finally {
                bluetoothViewModel.stopConnectionUpdates()
                healthViewModel.stopProfileUpdates()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues)
            .padding(horizontal = 12.dp)
    ) {
            item {
                ProfileHeader(authViewModel, modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp, start = 8.dp, end = 8.dp))
                StatsRow(
                    allWorkouts = allWorkouts,
                    todaySteps = healthState.todaySteps,
                    todayCalories = healthState.todayCalories,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 4.dp)
                )
                HealthConnectCard(
                    state = healthState,
                    onConnectClick = { healthPermissionsLauncher.launch(healthViewModel.permissions) },
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 4.dp),
                    smartWatchName = bluetoothState.smartWatch?.name,
                    isSmartWatchConnected = bluetoothState.smartWatch?.isConnected
                )
//                 BluetoothDevicesCard(
//                     hasPermission = bluetoothState.hasPermission,
//                     devices = bluetoothState.devices,
//                     modifier = Modifier
//                         .padding(vertical = 8.dp)
//                         .padding(horizontal = 4.dp)
//                 )
//                WatchDataCard(
//                    healthState = healthState,
//                    watch = bluetoothState.smartWatch,
//                    hasBluetoothPermission = bluetoothState.hasPermission,
//                    modifier = Modifier
//                        .padding(vertical = 8.dp)
//                        .padding(horizontal = 4.dp)
//                )
                RewardsSection(
                    rewards = rewards,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 4.dp)
                )
            }

            item {
                ProfileCalendarView(
                    workouts = allWorkouts,
                    workoutViewModel = workoutViewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .padding(horizontal = 4.dp)
                        .height(180.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = MaterialTheme.shapes.medium,
                            clip = true
                        )
                )
                LegendRow(modifier = Modifier.padding(top = 12.dp).padding(horizontal = 8.dp))
            }

            item {
                LogoutButton(
                    coroutineScope = coroutineScope,
                    authViewModel = authViewModel,
                    navController = navController,
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 24.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }
        }
}

