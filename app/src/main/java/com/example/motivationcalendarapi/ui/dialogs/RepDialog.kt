package com.motivationcalendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R

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
    if (showDialog) {
        var rep by remember { mutableStateOf(initialRep) }

        AlertDialog(
            onDismissRequest = onDismiss, title = {
            Text(
                text = stringResource(R.string.edit_reps),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
            )
        }, text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RepsRow(
                    value = rep,
                    min = minRep,
                    max = maxRep,
                    step = stepRep,
                    onValueChange = { rep = it }
                )
            }
        }, confirmButton = {
            TextButton(onClick = { onSave(rep) }) {
                Text(
                    text = stringResource(R.string.save),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }, dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }, modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun RepsRow(
    value: Int,
    min: Int,
    max: Int,
    step: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(onClick = { onValueChange((value - step).coerceAtLeast(min)) }) {
            Text("-$step", style = MaterialTheme.typography.bodyMedium)
        }

        OutlinedTextField(
            value = value.toString(),
            onValueChange = { input ->
                val reps = input.toIntOrNull() ?: min
                onValueChange(reps.coerceIn(min, max))
            },
            label = { Text(text = stringResource(R.string.reps), style = MaterialTheme.typography.titleMedium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(100.dp)
        )

        TextButton(onClick = { onValueChange((value + step).coerceAtMost(max)) }) {
            Text("+$step", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


