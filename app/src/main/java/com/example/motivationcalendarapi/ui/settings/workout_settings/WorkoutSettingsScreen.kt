package com.example.motivationcalendarapi.ui.settings.workout_settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val errorStepMustBeGreaterThanZero = stringResource(R.string.error_step_must_be_greater_than_zero)

    val errorMinimumRepsCannotBeNegative = stringResource(R.string.error_minimum_reps_cannot_be_negative)
    val errorMinRepsCannotExceedMax = stringResource(R.string.error_min_reps_cannot_exceed_max)
    val errorMaxRepsCannotBeLessThanMin = stringResource(R.string.error_max_reps_cannot_be_less_than_min)

    val errorWeightCannotBeNegative = stringResource(R.string.error_weight_cannot_be_negative)
    val errorMinWeightCannotExceedMax = stringResource(R.string.error_min_weight_cannot_exceed_max)
    val errorMaxWeightCannotBeLessThanMin = stringResource(R.string.error_max_weight_cannot_be_less_than_min)

    val errorTimeCannotBeNegative = stringResource(R.string.error_time_cannot_be_negative)
    val errorMinTimeCannotExceedMax = stringResource(R.string.error_min_time_cannot_exceed_max)
    val errorMaxTimeCannotBeLessThanMin = stringResource(R.string.error_max_time_cannot_be_less_than_min)

    val errorResistanceCannotBeNegative = stringResource(R.string.error_resistance_cannot_be_negative)
    val errorMinResistanceCannotExceedMax = stringResource(R.string.error_min_resistance_cannot_exceed_max)
    val errorMaxResistanceCannotBeLessThanMin = stringResource(R.string.error_max_resistance_cannot_be_less_than_min)

    val errorInclineCannotBeNegative = stringResource(R.string.error_incline_cannot_be_negative)
    val errorMinInclineCannotExceedMax = stringResource(R.string.error_min_incline_cannot_exceed_max)
    val errorMaxInclineCannotBeLessThanMin = stringResource(R.string.error_max_incline_cannot_be_less_than_min)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .padding(top = paddingValues)
            .verticalScroll(rememberScrollState())
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
                        newValue < 0 -> errorMinimumRepsCannotBeNegative
                        newValue > viewModel.maxRep.value -> errorMinRepsCannotExceedMax.format(
                            viewModel.maxRep.value
                        )
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
                        newValue < viewModel.minRep.value -> errorMaxRepsCannotBeLessThanMin.format(
                            viewModel.minRep.value
                        )
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
                        newValue <= 0 -> errorStepMustBeGreaterThanZero
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
                        newValue < 0f -> errorWeightCannotBeNegative
                        newValue > viewModel.maxWeight.value -> errorMinWeightCannotExceedMax.format(
                            viewModel.maxWeight.value
                        )
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
                        newValue < viewModel.minWeight.value -> errorMaxWeightCannotBeLessThanMin.format(
                            viewModel.minWeight.value
                        )
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
                        newValue <= 0f -> errorStepMustBeGreaterThanZero
                        else -> null
                    }
                }
            )
        }

        SettingsSection(title = stringResource(R.string.cardio_time_configuration)) {
            FloatNumberSettingItem(
                title = stringResource(R.string.minimum_time_minutes),
                valueState = viewModel.minCardioTime,
                onSave = { newMin ->
                    viewModel.saveCardioTimeSettings(
                        newMin,
                        viewModel.maxCardioTime.value,
                        viewModel.stepCardioTime.value
                    )
                },
                validate = { newValue ->
                    when {
                        newValue < 0f -> errorTimeCannotBeNegative
                        newValue > viewModel.maxCardioTime.value -> errorMinTimeCannotExceedMax.format(
                            viewModel.maxCardioTime.value
                        )
                        else -> null
                    }
                }
            )

            FloatNumberSettingItem(
                title = stringResource(R.string.maximum_time_minutes),
                valueState = viewModel.maxCardioTime,
                onSave = { newMax ->
                    viewModel.saveCardioTimeSettings(
                        viewModel.minCardioTime.value,
                        newMax,
                        viewModel.stepCardioTime.value
                    )
                },
                validate = { newValue ->
                    when {
                        newValue < viewModel.minCardioTime.value -> errorMaxTimeCannotBeLessThanMin.format(
                            viewModel.minCardioTime.value
                        )
                        else -> null
                    }
                }
            )

            FloatNumberSettingItem(
                title = stringResource(R.string.step_size_minutes),
                valueState = viewModel.stepCardioTime,
                onSave = { newStep ->
                    viewModel.saveCardioTimeSettings(
                        viewModel.minCardioTime.value,
                        viewModel.maxCardioTime.value,
                        newStep
                    )
                },
                validate = { newValue ->
                    when {
                        newValue <= 0f -> errorStepMustBeGreaterThanZero
                        else -> null
                    }
                }
            )
        }

        SettingsSection(title = stringResource(R.string.resistance_configuration)) {
            FloatNumberSettingItem(
                title = stringResource(R.string.minimum_resistance),
                valueState = viewModel.minResistance,
                onSave = { newMin ->
                    viewModel.saveResistanceSettings(
                        newMin,
                        viewModel.maxResistance.value,
                        viewModel.stepResistance.value
                    )
                },
                validate = { newValue ->
                    when {
                        newValue < 0f -> errorResistanceCannotBeNegative
                        newValue > viewModel.maxResistance.value -> errorMinResistanceCannotExceedMax.format(
                            viewModel.maxResistance.value
                        )
                        else -> null
                    }
                }
            )

            FloatNumberSettingItem(
                title = stringResource(R.string.maximum_resistance),
                valueState = viewModel.maxResistance,
                onSave = { newMax ->
                    viewModel.saveResistanceSettings(
                        viewModel.minResistance.value,
                        newMax,
                        viewModel.stepResistance.value
                    )
                },
                validate = { newValue ->
                    when {
                        newValue < viewModel.minResistance.value -> errorMaxResistanceCannotBeLessThanMin.format(
                            viewModel.minResistance.value
                        )
                        else -> null
                    }
                }
            )

            FloatNumberSettingItem(
                title = stringResource(R.string.step_size_resistance),
                valueState = viewModel.stepResistance,
                onSave = { newStep ->
                    viewModel.saveResistanceSettings(
                        viewModel.minResistance.value,
                        viewModel.maxResistance.value,
                        newStep
                    )
                },
                validate = { newValue ->
                    when {
                        newValue <= 0f -> errorStepMustBeGreaterThanZero
                        else -> null
                    }
                }
            )
        }

        SettingsSection(title = stringResource(R.string.incline_configuration)) {
            FloatNumberSettingItem(
                title = stringResource(R.string.minimum_incline),
                valueState = viewModel.minIncline,
                onSave = { newMin ->
                    viewModel.saveInclineSettings(
                        newMin,
                        viewModel.maxIncline.value,
                        viewModel.stepIncline.value
                    )
                },
                validate = { newValue ->
                    when {
                        newValue < 0f -> errorInclineCannotBeNegative
                        newValue > viewModel.maxIncline.value -> errorMinInclineCannotExceedMax.format(
                            viewModel.maxIncline.value
                        )
                        else -> null
                    }
                }
            )

            FloatNumberSettingItem(
                title = stringResource(R.string.maximum_incline),
                valueState = viewModel.maxIncline,
                onSave = { newMax ->
                    viewModel.saveInclineSettings(
                        viewModel.minIncline.value,
                        newMax,
                        viewModel.stepIncline.value
                    )
                },
                validate = { newValue ->
                    when {
                        newValue < viewModel.minIncline.value -> errorMaxInclineCannotBeLessThanMin.format(
                            viewModel.minIncline.value
                        )
                        else -> null
                    }
                }
            )

            FloatNumberSettingItem(
                title = stringResource(R.string.step_size_percent),
                valueState = viewModel.stepIncline,
                onSave = { newStep ->
                    viewModel.saveInclineSettings(
                        viewModel.minIncline.value,
                        viewModel.maxIncline.value,
                        newStep
                    )
                },
                validate = { newValue ->
                    when {
                        newValue <= 0f -> errorStepMustBeGreaterThanZero
                        else -> null
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}