package com.example.motivationcalendarapi.ui.dialogs

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
fun WeightDialog(
    showDialog: Boolean, initialWeight: Float, onDismiss: () -> Unit, onSave: (Float) -> Unit
) {
    if (showDialog) {
        var weight by remember { mutableStateOf(initialWeight) }

        AlertDialog(onDismissRequest = onDismiss, title = {
            Text(
                text = "Edit Weight",
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
                WeightRow(value = weight, onValueChange = { weight = it })
            }
        }, confirmButton = {
            TextButton(onClick = { onSave(weight) }) {
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
private fun WeightRow(
    value: Float, onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(onClick = {
            onValueChange((value - 10f).coerceAtLeast(0f))
        }) {
            Text("-10", style = MaterialTheme.typography.bodyMedium)
        }

        OutlinedTextField(value = value.toString(),
            onValueChange = { input ->
                val weight = input.toFloatOrNull() ?: 0f
                onValueChange(weight.coerceIn(0f, 200f))
            },
            label = { Text("Weight", style = MaterialTheme.typography.titleMedium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(120.dp)
        )

        TextButton(onClick = {
            onValueChange((value + 10f).coerceAtMost(200f))
        }) {
            Text("+10", style = MaterialTheme.typography.bodyMedium)
        }
    }
}