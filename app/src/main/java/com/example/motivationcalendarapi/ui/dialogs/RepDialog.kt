package com.motivationcalendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.dialogs.IntMetricInputDialog

@Composable
fun RepsDialog(
    showDialog: Boolean,
    initialRep: Int,
    minRep: Int,
    maxRep: Int,
    stepRep: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    IntMetricInputDialog(
        showDialog = showDialog,
        title = stringResource(R.string.edit_reps),
        label = stringResource(R.string.reps),
        initialValue = initialRep,
        minValue = minRep,
        maxValue = maxRep,
        stepValue = stepRep,
        onDismiss = onDismiss,
        onSave = onSave
    )
}
