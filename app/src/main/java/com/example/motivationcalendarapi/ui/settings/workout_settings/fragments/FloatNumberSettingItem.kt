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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.settings.workout_settings.dialogs.ErrorWorkoutSettingsDialog
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FloatNumberSettingItem(
    title: String,
    valueState: StateFlow<Float>,
    onSave: (Float) -> Unit,
    validate: (Float) -> String?
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
                if (newValue.isEmpty() || newValue.toFloatOrNull() != null) {
                    textValue = newValue
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(120.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    textValue.toFloatOrNull()?.let { newValue ->
                        validate(newValue)?.let { error ->
                            errorMessage = error
                            showError = true
                        } ?: run {
                            onSave(newValue)
                        }
                    } ?: run {
                        errorMessage = R.string.invalid_number_format.toString()
                        showError = true
                    }
                }) {
                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
                }
            }
        )
    }
}