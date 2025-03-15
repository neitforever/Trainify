package com.example.motivationcalendarapi.ui.utils

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import formatTime
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.motivationcalendarapi.R

@Composable
fun TotalWeightAndTimeRow(
    timerValue: Int,
    totalKg: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically){
            Icon(
                painter = painterResource(R.drawable.ic_equipment_body_weight),
                contentDescription = "weight",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp).padding(end = 4.dp))
            Text(
                text = "%.1f".format(totalKg),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "kg",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically){
            Text(
                text = formatTime(timerValue),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_time),
                contentDescription = "time",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp))

        }
    }
}