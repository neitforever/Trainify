package com.example.motivationcalendarapi.ui.settings.workout_settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.settings.workout_settings.fragments.FloatNumberSettingItem
import com.example.motivationcalendarapi.ui.settings.workout_settings.fragments.NumberSettingItem
import com.example.motivationcalendarapi.ui.settings.workout_settings.fragments.SettingsSection
import com.example.motivationcalendarapi.viewmodel.WorkoutSettingsViewModel

@Composable
fun WorkoutSettingsScreen(
    viewModel: WorkoutSettingsViewModel,
    paddingValues: Dp
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .padding(top = paddingValues)
    ) {
        SettingsSection(title = stringResource(R.string.repetitions_configuration)) {
            NumberSettingItem(
                title = stringResource(R.string.minimum_reps),
                valueState = viewModel.minRep,
                onSave = { newMin ->
                    viewModel.saveRepSettings(
                        newMin,
                        viewModel.maxRep.value,
                        viewModel.stepRep.value
                    )
                },
                validate = { newValue ->
                    when {
                        newValue < 0 -> "Minimum reps cannot be negative"
                        newValue > viewModel.maxRep.value ->
                            "Min reps cannot exceed max (${viewModel.maxRep.value})"
                        else -> null
                    }
                }
            )

            NumberSettingItem(
                title = stringResource(R.string.maximum_reps),
                valueState = viewModel.maxRep,
                onSave = { newMax ->
                    viewModel.saveRepSettings(
                        viewModel.minRep.value,
                        newMax,
                        viewModel.stepRep.value
                    )
                },
                validate = { newValue ->
                    when {
                        newValue < viewModel.minRep.value ->
                            "Max reps cannot be less than min (${viewModel.minRep.value})"
                        else -> null
                    }
                }
            )

            NumberSettingItem(
                title = stringResource(R.string.step_size),
                valueState = viewModel.stepRep,
                onSave = { newStep ->
                    viewModel.saveRepSettings(
                        viewModel.minRep.value,
                        viewModel.maxRep.value,
                        newStep
                    )
                },
                validate = { newValue ->
                    when {
                        newValue <= 0 -> "Step must be greater than 0"
                        else -> null
                    }
                }
            )
        }

        SettingsSection(title = stringResource(R.string.weight_configuration)) {
            FloatNumberSettingItem(
                title = stringResource(R.string.minimum_weight_kg),
                valueState = viewModel.minWeight,
                onSave = { newMin ->
                    viewModel.saveWeightSettings(
                        newMin,
                        viewModel.maxWeight.value,
                        viewModel.stepWeight.value
                    )
                },
                validate = { newValue ->
                    when {
                        newValue < 0 -> "Weight cannot be negative"
                        newValue > viewModel.maxWeight.value ->
                            "Min weight cannot exceed max (${viewModel.maxWeight.value} kg)"
                        else -> null
                    }
                }
            )

            FloatNumberSettingItem(
                title = stringResource(R.string.maximum_weight_kg),
                valueState = viewModel.maxWeight,
                onSave = { newMax ->
                    viewModel.saveWeightSettings(
                        viewModel.minWeight.value,
                        newMax,
                        viewModel.stepWeight.value
                    )
                },
                validate = { newValue ->
                    when {
                        newValue < viewModel.minWeight.value ->
                            "Max weight cannot be less than min (${viewModel.minWeight.value} kg)"
                        else -> null
                    }
                }
            )

            FloatNumberSettingItem(
                title = stringResource(R.string.step_size_kg),
                valueState = viewModel.stepWeight,
                onSave = { newStep ->
                    viewModel.saveWeightSettings(
                        viewModel.minWeight.value,
                        viewModel.maxWeight.value,
                        newStep
                    )
                },
                validate = { newValue ->
                    when {
                        newValue <= 0 -> "Step must be greater than 0"
                        else -> null
                    }
                }
            )
        }
    }
}






