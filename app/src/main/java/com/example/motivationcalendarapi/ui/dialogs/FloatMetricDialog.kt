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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import java.util.Locale

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
    if (!showDialog) return

    var value by remember(initialValue) {
        mutableFloatStateOf(initialValue)
    }

    val safeStep = if (stepValue <= 0f) 1f else stepValue

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FloatMetricRow(
                    value = value,
                    min = minValue,
                    max = maxValue,
                    step = safeStep,
                    label = label,
                    onValueChange = { value = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(value) }) {
                Text(
                    text = stringResource(R.string.save),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun FloatMetricRow(
    value: Float,
    min: Float,
    max: Float,
    step: Float,
    label: String,
    onValueChange: (Float) -> Unit
) {
    var inputText by remember { mutableStateOf(formatFloat(value)) }
    var hasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(value, hasFocus) {
        if (!hasFocus) {
            inputText = formatFloat(value)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(
            onClick = {
                val newValue = (value - step).coerceAtLeast(min)
                inputText = formatFloat(newValue)
                onValueChange(newValue)
            }
        ) {
            Text(
                text = "-${formatFloat(step)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedTextField(
            value = inputText,
            onValueChange = { input ->
                inputText = input
                val normalizedInput = input.replace(',', '.')
                normalizedInput.toFloatOrNull()?.let { parsedValue ->
                    onValueChange(parsedValue.coerceIn(min, max))
                }
            },
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            modifier = Modifier
                .width(120.dp)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && !hasFocus) {
                        inputText = ""
                    } else if (!focusState.isFocused && hasFocus && inputText.isBlank()) {
                        inputText = formatFloat(value)
                    }
                    hasFocus = focusState.isFocused
                }
        )

        TextButton(
            onClick = {
                val newValue = (value + step).coerceAtMost(max)
                inputText = formatFloat(newValue)
                onValueChange(newValue)
            }
        ) {
            Text(
                text = "+${formatFloat(step)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatFloat(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}