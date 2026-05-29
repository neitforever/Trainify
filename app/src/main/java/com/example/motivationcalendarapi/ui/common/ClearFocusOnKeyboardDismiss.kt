package com.example.motivationcalendarapi.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun ClearFocusOnKeyboardDismiss() {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val isKeyboardVisible = imeBottom > 0
    var wasKeyboardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isKeyboardVisible) {
        if (wasKeyboardVisible && !isKeyboardVisible) {
            focusManager.clearFocus(force = true)
        }
        wasKeyboardVisible = isKeyboardVisible
    }
}
