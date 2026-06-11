package com.example.motivationcalendarapi.ui.dialogs

import androidx.compose.runtime.Composable

@Composable
fun FloatMetricDialog(
    showDialog: Boolean,
    title: String,
    label: String,
    initialValue: Float,
    minValue: Float,
    maxValue: Float,
    stepValue: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    FloatMetricInputDialog(
        showDialog = showDialog,
        title = title,
        label = label,
        initialValue = initialValue,
        minValue = minValue,
        maxValue = maxValue,
        stepValue = stepValue,
        onDismiss = onDismiss,
        onSave = onSave
    )
}
