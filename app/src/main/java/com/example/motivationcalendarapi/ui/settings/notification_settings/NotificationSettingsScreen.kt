package com.example.motivationcalendarapi.ui.settings.notification_settings

import android.Manifest
import android.os.Build
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.notifications.NotificationHelper
import com.example.motivationcalendarapi.viewmodel.NotificationSettingsViewModel

@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    paddingValues: Dp
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    var hasPermission by remember { mutableStateOf(NotificationHelper.hasNotificationPermission(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted || NotificationHelper.hasNotificationPermission(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp)
            .padding(top = paddingValues)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        if (!hasPermission) {
            PermissionCard(
                hasPermission = false,
                onRequestPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        hasPermission = true
                    }
                }
            )
        }

        SettingsSection(title = stringResource(R.string.notification_section_workouts)) {
            NotificationSwitchRow(stringResource(R.string.notification_workout_active_setting), settings.workoutActiveEnabled, viewModel::setWorkoutActiveEnabled)
            NotificationSwitchRow(stringResource(R.string.notification_workout_reminder_setting), settings.workoutReminderEnabled, viewModel::setWorkoutReminderEnabled)
            DaysRow(
                title = stringResource(R.string.notification_workout_reminder_days),
                value = settings.workoutReminderDays,
                onMinus = { viewModel.setWorkoutReminderDays(settings.workoutReminderDays - 1) },
                onPlus = { viewModel.setWorkoutReminderDays(settings.workoutReminderDays + 1) }
            )
        }

        SettingsSection(title = stringResource(R.string.notification_section_ai)) {
            NotificationSwitchRow(stringResource(R.string.notification_ai_exercise_setting), settings.aiExerciseCreatedEnabled, viewModel::setAiExerciseCreatedEnabled)
            NotificationSwitchRow(stringResource(R.string.notification_ai_template_setting), settings.aiTemplateCreatedEnabled, viewModel::setAiTemplateCreatedEnabled)
        }

        SettingsSection(title = stringResource(R.string.notification_section_weight_progress)) {
            NotificationSwitchRow(stringResource(R.string.notification_weight_progress_setting), settings.weightProgressEnabled, viewModel::setWeightProgressEnabled)
            NotificationSwitchRow(stringResource(R.string.notification_weight_progress_reminder_setting), settings.weightProgressReminderEnabled, viewModel::setWeightProgressReminderEnabled)
            DaysRow(
                title = stringResource(R.string.notification_weight_progress_reminder_days),
                value = settings.weightProgressReminderDays,
                onMinus = { viewModel.setWeightProgressReminderDays(settings.weightProgressReminderDays - 1) },
                onPlus = { viewModel.setWeightProgressReminderDays(settings.weightProgressReminderDays + 1) }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionCard(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val container = if (hasPermission) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer
    val content = if (hasPermission) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (hasPermission) "✓" else "!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (hasPermission) stringResource(R.string.notification_permission_enabled) else stringResource(R.string.notifications_permission_required),
                    color = content,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.notifications_permission_required_description),
                    color = content.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (!hasPermission) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) { Text(stringResource(R.string.allow_notifications)) }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
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
private fun NotificationSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.42f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun DaysRow(title: String, value: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.42f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            DaysCounterButton(
                icon = {  Icon(
                    painter = painterResource(id = R.drawable.ic_minus),
                    contentDescription = "Decrease days",
                    modifier = Modifier.size(16.dp),
                ) },
                contentDescription = "Decrease days",
                onClick = onMinus
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            DaysCounterButton(
                icon = { Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = "Increase days",
                    modifier = Modifier.size(16.dp),
                ) },
                contentDescription = "Increase days",
                onClick = onPlus
            )
        }
    }
}

@Composable
private fun DaysCounterButton(
    icon: @Composable () -> Unit,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f)),
        modifier = Modifier.size(40.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(40.dp)
                .semantics { this.contentDescription = contentDescription }
        ) {
            icon()
        }
    }
}
