package com.example.motivationcalendarapi.ui.profile.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.viewmodel.health.HealthConnectUiState

@Composable
fun HealthConnectCard(
    state: HealthConnectUiState,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier,
    smartWatchName: String? = null,
    isSmartWatchConnected: Boolean? = null
) {
    val isHealthConnectConnected = state.isAvailable && state.hasPermissions
    val healthConnectSmartWatchDetected = isHealthConnectConnected && state.isSmartWatchDetected
    val hasBluetoothWatch = !smartWatchName.isNullOrBlank()
    val smartWatchConnected = isSmartWatchConnected ?: healthConnectSmartWatchDetected

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConnectionStatusRow(
                iconRes = R.drawable.ic_health_connect,
                title = stringResource(R.string.health_connect),
                subtitle = when {
                    !state.isAvailable -> stringResource(R.string.health_connect_not_available)
                    state.hasPermissions -> stringResource(R.string.health_connect_connected)
                    else -> stringResource(R.string.health_connect_not_connected)
                },
                isConnected = isHealthConnectConnected
            )

            ConnectionStatusRow(
                iconRes = R.drawable.ic_watch,
                title = stringResource(R.string.smart_watch),
                subtitle = when {
                    hasBluetoothWatch && smartWatchConnected ->
                        stringResource(R.string.smart_watch_named_connected, smartWatchName!!)

                    hasBluetoothWatch ->
                        stringResource(R.string.smart_watch_named_disconnected, smartWatchName!!)

                    state.connectedDevice != null -> state.connectedDevice
                    healthConnectSmartWatchDetected -> stringResource(R.string.smart_watch_connected)
                    else -> stringResource(R.string.health_connect_data_source_unknown)
                },
                isConnected = smartWatchConnected
            )

            if (!isHealthConnectConnected && state.isAvailable) {
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.connect_health_connect),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusRow(
    iconRes: Int,
    title: String,
    subtitle: String,
    isConnected: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        StatusDot(isConnected = isConnected)
    }
}

@Composable
private fun StatusDot(isConnected: Boolean) {
    val color = if (isConnected) Color(0xFF2E7D32) else Color(0xFFC62828)
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}
