package com.example.motivationcalendarapi.ui.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R

@Composable
fun ExistWorkoutDialog(
    showDialog: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(onDismissRequest = onDismiss, title = {
            Text(
                text = stringResource(R.string.workout_already_exists),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }, text = {
            Text(
                text = stringResource(R.string.do_you_want_to_overwrite_training),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }, confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.overwrite),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }, dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
            modifier = Modifier.padding(16.dp)
        )
    }
}