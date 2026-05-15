package com.example.motivationcalendarapi.ui.dialogs

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
fun WeightDialog(
    showDialog: Boolean,
    initialWeight: Float,
    minWeight: Float,
    maxWeight: Float,
    stepWeight: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    if (showDialog) {
        var weight by remember { mutableStateOf(initialWeight) }

        AlertDialog(onDismissRequest = onDismiss, title = {
            Text(
                text = stringResource(R.string.edit_weight),
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
                WeightRow(
                    value = weight,
                    min = minWeight,
                    max = maxWeight,
                    step = stepWeight,
                    onValueChange = { weight = it }
                )
            }
        }, confirmButton = {
            TextButton(onClick = { onSave(weight) }) {
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
private fun WeightRow(
    value: Float,
    min: Float,
    max: Float,
    step: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(onClick = { onValueChange((value - step).coerceAtLeast(min)) }) {
            Text("-$step", style = MaterialTheme.typography.bodyMedium)
        }

        OutlinedTextField(value = value.toString(),
            onValueChange = { input ->
                val weight = input.toFloatOrNull() ?: min
                onValueChange(weight.coerceIn(min, max))
            },
            label = { Text(text = stringResource(R.string.Weight), style = MaterialTheme.typography.titleMedium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(100.dp)
        )

        TextButton(onClick = { onValueChange((value + step).coerceAtMost(max)) }) {
            Text("+$step", style = MaterialTheme.typography.bodyMedium)
        }
    }
}