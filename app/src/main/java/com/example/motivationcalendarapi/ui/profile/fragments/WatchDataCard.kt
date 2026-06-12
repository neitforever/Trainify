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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.repositories.bluetooth.BluetoothDeviceInfo
import com.example.motivationcalendarapi.viewmodel.health.HealthConnectUiState

@Composable
fun WatchDataCard(
    healthState: HealthConnectUiState,
    watch: BluetoothDeviceInfo?,
    hasBluetoothPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val isConnected = watch?.isConnected == true

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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Данные часов",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            watch == null -> "Часы не найдены среди Bluetooth-устройств"
                            isConnected -> "${watch.name} подключены"
                            else -> "${watch.name} отключены"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusDot(isConnected = isConnected)
            }

            WatchInfoRow(label = "Bluetooth-разрешение", value = if (hasBluetoothPermission) "Получено" else "Не получено")
            WatchInfoRow(label = "Название", value = watch?.name ?: "Не определено")
            WatchInfoRow(label = "Тип Bluetooth", value = watch?.type ?: "Не определён")
            WatchInfoRow(label = "MAC-адрес", value = watch?.address ?: "Не определён")
            WatchInfoRow(label = "Статус Bluetooth", value = if (isConnected) "Подключены" else "Отключены / не найдены")
            WatchInfoRow(label = "Health Connect", value = if (healthState.isAvailable) "Доступен" else "Недоступен")
            WatchInfoRow(label = "Разрешения Health Connect", value = if (healthState.hasPermissions) "Выданы" else "Не выданы")
            WatchInfoRow(label = "Источник Health Connect", value = healthState.connectedDevice ?: "Не определён")
            WatchInfoRow(label = "Откуда берутся метрики", value = "Health Connect, не Bluetooth")
            WatchInfoRow(label = "Bluetooth-метрики", value = "Недоступны через стандартный Android API")
            WatchInfoRow(label = "Пульс", value = healthState.currentHeartRate?.let { "$it уд/мин" } ?: "Нет данных")
            WatchInfoRow(label = "Пульс: источник", value = "Последняя запись HeartRateRecord")
            WatchInfoRow(label = "Шаги сегодня", value = healthState.todaySteps?.toString() ?: "Нет данных")
            WatchInfoRow(label = "Шаги: источник", value = "Агрегация StepsRecord за сегодня")
            WatchInfoRow(label = "Активные калории сегодня", value = healthState.todayCalories?.let { "$it ккал" } ?: "Нет данных")
            WatchInfoRow(label = "Калории: источник", value = "Агрегация ActiveCaloriesBurnedRecord за сегодня")
            WatchInfoRow(label = "Источник пульса", value = if (healthState.isSmartWatchDetected) "Обнаружен" else "Не обнаружен")
        }
    }
}

@Composable
private fun WatchInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.25f)
        )
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
