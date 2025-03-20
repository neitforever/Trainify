package com.motivationcalendar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RepsDialog(
    showDialog: Boolean, initialRep: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit
) {
    if (showDialog) {
        var rep by remember { mutableStateOf(initialRep) }

        AlertDialog(onDismissRequest = onDismiss, title = {
            Text(
                text = "Edit Reps",
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
                RepsRow(value = rep, onValueChange = { rep = it })
            }
        }, confirmButton = {
            TextButton(onClick = { onSave(rep) }) {
                Text(
                    text = "Save",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }, dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
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
    value: Int, onValueChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(onClick = {
            onValueChange((value - 4).coerceAtLeast(0))
        }) {
            Text("-4", style = MaterialTheme.typography.bodyMedium)
        }

        OutlinedTextField(value = value.toString(),
            onValueChange = { input ->
                val reps = input.toIntOrNull() ?: 0
                onValueChange(reps.coerceIn(0, 32))
            },
            label = { Text("Reps", style = MaterialTheme.typography.titleMedium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(120.dp)
        )

        TextButton(onClick = {
            onValueChange((value + 4).coerceAtMost(32))
        }) {
            Text("+4", style = MaterialTheme.typography.bodyMedium)
        }
    }
}