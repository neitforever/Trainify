package com.example.motivationcalendarapi.ui.settings.workout_settings

import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.motivationcalendarapi.viewmodel.WorkoutSettingsViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutSettingsViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun WorkoutSettingsScreen(
    viewModel: WorkoutSettingsViewModel,
    navController: NavController,
    paddingValues: Dp
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = paddingValues)
    ) {
        Text(
            text = "Workout Settings",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        SettingsSection(title = "Repetitions Configuration") {
            NumberSettingItem(
                title = "Minimum Reps",
                valueState = viewModel.minRep,
                onSave = { newMin ->
                    viewModel.saveRepSettings(
                        newMin,
                        viewModel.maxRep.value,
                        viewModel.stepRep.value
                    )
                }
            )

            NumberSettingItem(
                title = "Maximum Reps",
                valueState = viewModel.maxRep,
                onSave = { newMax ->
                    viewModel.saveRepSettings(
                        viewModel.minRep.value,
                        newMax,
                        viewModel.stepRep.value
                    )
                }
            )

            NumberSettingItem(
                title = "Step Size",
                valueState = viewModel.stepRep,
                onSave = { newStep ->
                    viewModel.saveRepSettings(
                        viewModel.minRep.value,
                        viewModel.maxRep.value,
                        newStep
                    )
                }
            )
        }

        SettingsSection(title = "Weight Configuration") {
            FloatNumberSettingItem(
                title = "Minimum Weight (kg)",
                valueState = viewModel.minWeight,
                onSave = { newMin ->
                    viewModel.saveWeightSettings(
                        newMin,
                        viewModel.maxWeight.value,
                        viewModel.stepWeight.value
                    )
                }
            )

            FloatNumberSettingItem(
                title = "Maximum Weight (kg)",
                valueState = viewModel.maxWeight,
                onSave = { newMax ->
                    viewModel.saveWeightSettings(
                        viewModel.minWeight.value,
                        newMax,
                        viewModel.stepWeight.value
                    )
                }
            )

            FloatNumberSettingItem(
                title = "Step Size (kg)",
                valueState = viewModel.stepWeight,
                onSave = { newStep ->
                    viewModel.saveWeightSettings(
                        viewModel.minWeight.value,
                        viewModel.maxWeight.value,
                        newStep
                    )
                }
            )
        }
    }
}

@Composable
private fun NumberSettingItem(
    title: String,
    valueState: StateFlow<Int>,
    onSave: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf("") }
    val currentValue by valueState.collectAsState()

    LaunchedEffect(currentValue) {
        textValue = currentValue.toString()
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
                    textValue.toIntOrNull()?.let { onSave(it) }
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }
        )
    }
}

@Composable
private fun FloatNumberSettingItem(
    title: String,
    valueState: StateFlow<Float>,
    onSave: (Float) -> Unit
) {
    var textValue by remember { mutableStateOf("") }
    val currentValue by valueState.collectAsState()

    LaunchedEffect(currentValue) {
        textValue = currentValue.toString()
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
                    textValue.toFloatOrNull()?.let { onSave(it) }
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }
        )
    }
}


@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingItemLayout(
    title: String,
    content: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        content()
    }
}