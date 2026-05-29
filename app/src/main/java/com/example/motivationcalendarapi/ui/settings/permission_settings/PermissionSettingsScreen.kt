package com.example.motivationcalendarapi.ui.settings.permission_settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.notifications.NotificationHelper
import com.example.motivationcalendarapi.repositories.health.HealthConnectRepository
import com.example.motivationcalendarapi.viewmodel.health.HealthConnectViewModel
import com.example.motivationcalendarapi.viewmodel.health.HealthConnectViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionSettingsScreen(paddingValues: Dp) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val healthViewModel: HealthConnectViewModel = viewModel(
        factory = HealthConnectViewModelFactory(HealthConnectRepository(applicationContext))
    )
    val healthState by healthViewModel.uiState.collectAsState()

    val scrollState = rememberScrollState()

    var cameraGranted by rememberSaveable { mutableStateOf(context.hasPermission(Manifest.permission.CAMERA)) }
    var notificationsGranted by rememberSaveable { mutableStateOf(NotificationHelper.hasNotificationPermission(context)) }
    var mediaImagesGranted by rememberSaveable { mutableStateOf(context.hasMediaImagesPermission()) }
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        cameraGranted = context.hasPermission(Manifest.permission.CAMERA)
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsGranted = NotificationHelper.hasNotificationPermission(context)
    }
    val mediaImagesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        mediaImagesGranted = context.hasMediaImagesPermission()
    }
    val healthPermissionsLauncher = rememberLauncherForActivityResult(
        contract = healthViewModel.permissionContract
    ) { healthViewModel.refresh() }

    fun refreshPermissionStatuses() {
        cameraGranted = context.hasPermission(Manifest.permission.CAMERA)
        notificationsGranted = NotificationHelper.hasNotificationPermission(context)
        mediaImagesGranted = context.hasMediaImagesPermission()
        healthViewModel.refresh()
    }

    val appSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissionStatuses()
    }

    fun openPermissionSettings() {
        appSettingsLauncher.launch(context.appPermissionSettingsIntent())
    }

    LaunchedEffect(Unit) { refreshPermissionStatuses() }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                refreshPermissionStatuses()
                delay(350)
                isRefreshing = false
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

        PermissionInfoCard()

        PermissionSection(title = stringResource(R.string.permission_section_runtime)) {
            PermissionSwitchRow(
                title = stringResource(R.string.permission_camera_title),
                description = stringResource(R.string.permission_camera_description),
                checked = cameraGranted,
                onCheckedChange = { checked ->
                    if (checked) cameraLauncher.launch(Manifest.permission.CAMERA) else openPermissionSettings()
                }
            )

            PermissionSwitchRow(
                title = stringResource(R.string.permission_notifications_title),
                description = stringResource(R.string.permission_notifications_description),
                checked = notificationsGranted,
                onCheckedChange = { checked ->
                    if (checked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            notificationsGranted = true
                        }
                    } else {
                        openPermissionSettings()
                    }
                }
            )

            PermissionSwitchRow(
                title = stringResource(R.string.permission_gallery_title),
                description = stringResource(R.string.permission_gallery_description),
                checked = mediaImagesGranted,
                onCheckedChange = { checked ->
                    if (checked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mediaImagesLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            mediaImagesLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    } else {
                        openPermissionSettings()
                    }
                }
            )
        }

        PermissionSection(title = stringResource(R.string.permission_section_health_connect)) {
            PermissionSwitchRow(
                title = stringResource(R.string.permission_health_connect_title),
                description = when {
                    !healthState.isAvailable -> stringResource(R.string.permission_health_connect_not_available)
                    healthState.hasPermissions -> stringResource(R.string.permission_health_connect_description_enabled)
                    else -> stringResource(R.string.permission_health_connect_description)
                },
                checked = healthState.isAvailable && healthState.hasPermissions,
                enabled = healthState.isAvailable,
                onCheckedChange = { checked ->
                    if (checked) healthPermissionsLauncher.launch(healthViewModel.permissions) else openPermissionSettings()
                }
            )
        }

        PermissionSection(title = stringResource(R.string.permission_section_normal)) {
            PermissionStatusRow(
                title = stringResource(R.string.permission_internet_title),
                description = stringResource(R.string.permission_internet_description),
                granted = true
            )
            PermissionStatusRow(
                title = stringResource(R.string.permission_vibration_title),
                description = stringResource(R.string.permission_vibration_description),
                granted = true
            )
            PermissionStatusRow(
                title = stringResource(R.string.permission_foreground_service_title),
                description = stringResource(R.string.permission_foreground_service_description),
                granted = true
            )
        }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionInfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.permission_info_title),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            val permissionGuidelines = listOf(
                stringResource(R.string.permission_info_runtime_line),
                stringResource(R.string.permission_info_disable_line),
                stringResource(R.string.permission_info_refresh_line)
            )

            permissionGuidelines.forEach { guideline ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_complete),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(16.dp)
                    )
                    Text(
                        text = guideline,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = title, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun PermissionSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.42f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun PermissionStatusRow(title: String, description: String, granted: Boolean) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.42f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            PermissionStatusDot(granted = granted)
        }
    }
}

@Composable
private fun PermissionStatusDot(granted: Boolean) {
    val color = if (granted) Color(0xFF2E7D32) else Color(0xFFC62828)
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
    }
}

private fun android.content.Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun android.content.Context.hasMediaImagesPermission(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        hasPermission(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

private fun android.content.Context.appPermissionSettingsIntent(): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
