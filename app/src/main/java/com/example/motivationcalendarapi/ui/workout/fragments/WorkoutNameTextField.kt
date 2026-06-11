package com.example.motivationcalendarapi.ui.workout.fragments

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.components.TrainifyNameTextField

@Composable
fun WorkoutNameTextField(
    workoutName: String,
    onValueChange: (String) -> Unit,
    keyboardController: SoftwareKeyboardController?
) {
    val focusManager = LocalFocusManager.current

    TrainifyNameTextField(
        value = workoutName,
        onValueChange = onValueChange,
        placeholder = stringResource(R.string.workout_name),
        leadingIconRes = R.drawable.ic_write,
        imeAction = ImeAction.Done,
        onDone = {
            keyboardController?.hide()
            focusManager.clearFocus(force = true)
        }
    )
}
