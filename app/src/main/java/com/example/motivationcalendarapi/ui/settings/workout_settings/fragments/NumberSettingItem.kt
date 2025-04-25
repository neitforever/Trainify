package com.example.motivationcalendarapi.ui.settings.workout_settings.fragments

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.ui.settings.workout_settings.dialogs.ErrorWorkoutSettingsDialog
import kotlinx.coroutines.flow.StateFlow

@Composable
fun NumberSettingItem(
    title: String,
    valueState: StateFlow<Int>,
    onSave: (Int) -> Unit,
    validate: (Int) -> String?
) {
    var textValue by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val currentValue by valueState.collectAsState()

    LaunchedEffect(currentValue) {
        textValue = currentValue.toString()
    }

    if (showError) {
        ErrorWorkoutSettingsDialog(
            message = errorMessage,
            showDialog = showError,
            onDismiss = { showError = false }
        )
    }

    SettingItemLayout(title = title) {
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                    textValue = newValue
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(120.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    textValue.toIntOrNull()?.let { newValue ->
                        validate(newValue)?.let { error ->
                            errorMessage = error
                            showError = true
                        } ?: run {
                            onSave(newValue)
                        }
                    } ?: run {
                        errorMessage = "Invalid number format"
                        showError = true
                    }
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }
        )
    }
}

