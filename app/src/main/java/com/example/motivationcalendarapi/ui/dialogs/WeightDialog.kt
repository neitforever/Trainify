package com.example.motivationcalendarapi.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.motivationcalendarapi.R

@Composable
fun WeightDialog(
    showDialog: Boolean,
    initialWeight: Float,
    minWeight: Float,
    maxWeight: Float,
    stepWeight: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    FloatMetricInputDialog(
        showDialog = showDialog,
        title = stringResource(R.string.edit_weight),
        label = stringResource(R.string.Weight),
        initialValue = initialWeight,
        minValue = minWeight,
        maxValue = maxWeight,
        stepValue = stepWeight,
        onDismiss = onDismiss,
        onSave = onSave
    )
}
