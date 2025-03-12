package com.motivationcalendar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.model.ExerciseSet

@Composable
fun AddSetDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    newSet: MutableState<ExerciseSet>,
    onAddSet: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Add Set",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RepsRow(newSet)
                    WeightRow(newSet)
                }
            },
            confirmButton = {
                TextButton(onClick = onAddSet) {
                    Text(
                        text = "Add",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun RepsRow(newSet: MutableState<ExerciseSet>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(onClick = {
            newSet.value = newSet.value.copy(
                rep = (newSet.value.rep - 4).coerceAtLeast(0))
        }) {
            Text("-4", style = MaterialTheme.typography.bodyMedium)
        }

        OutlinedTextField(
            value = newSet.value.rep.toString(),
            onValueChange = { input ->
                val reps = input.toIntOrNull() ?: 0
                newSet.value = newSet.value.copy(rep = reps.coerceIn(0, 32))
            },
            label = { Text("Reps", style = MaterialTheme.typography.titleMedium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(120.dp)
        )

        TextButton(onClick = {
            newSet.value = newSet.value.copy(
                rep = (newSet.value.rep + 4).coerceAtMost(32))
        }) {
            Text("+4", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun WeightRow(newSet: MutableState<ExerciseSet>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(onClick = {
            newSet.value = newSet.value.copy(
                weigth = (newSet.value.weigth - 5f).coerceAtLeast(0f))
        }) {
            Text("-5", style = MaterialTheme.typography.bodyMedium)
        }

        OutlinedTextField(
            value = newSet.value.weigth.toString(),
            onValueChange = { input ->
                val weight = input.toFloatOrNull() ?: 0f
                newSet.value = newSet.value.copy(weigth = weight.coerceIn(0f, 200f))
            },
            label = { Text("Weight", style = MaterialTheme.typography.titleMedium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(120.dp)
        )

        TextButton(onClick = {
            newSet.value = newSet.value.copy(
                weigth = (newSet.value.weigth + 5f).coerceAtMost(200f))
        }) {
            Text("+5", style = MaterialTheme.typography.bodyMedium)
        }
    }
}