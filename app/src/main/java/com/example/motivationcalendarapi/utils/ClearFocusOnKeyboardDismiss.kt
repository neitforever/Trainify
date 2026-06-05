package com.example.motivationcalendarapi.utils

import android.graphics.Rect
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView

@Composable
fun ClearFocusOnKeyboardDismiss(onKeyboardDismiss: () -> Unit = {}) {
    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    val density = LocalDensity.current
    val visibleFrame = remember { Rect() }
    val currentOnKeyboardDismiss by rememberUpdatedState(onKeyboardDismiss)
    var wasKeyboardVisibleByInsets by remember { mutableStateOf(false) }

    fun clearFocusAfterKeyboardDismiss() {
        focusManager.clearFocus(force = true)
        currentOnKeyboardDismiss()
    }

    val imeBottom = WindowInsets.ime.getBottom(density)

    LaunchedEffect(imeBottom) {
        val isKeyboardVisible = imeBottom > 0
        if (wasKeyboardVisibleByInsets && !isKeyboardVisible) {
            clearFocusAfterKeyboardDismiss()
        }
        wasKeyboardVisibleByInsets = isKeyboardVisible
    }

    DisposableEffect(view) {
        var wasKeyboardVisibleByLayout = false

        val listener = android.view.ViewTreeObserver.OnGlobalLayoutListener {
            view.getWindowVisibleDisplayFrame(visibleFrame)

            val rootHeight = view.rootView.height
            val visibleHeight = visibleFrame.height()
            val heightDiff = rootHeight - visibleHeight
            val isKeyboardVisible = heightDiff > rootHeight * 0.15f

            if (wasKeyboardVisibleByLayout && !isKeyboardVisible) {
                clearFocusAfterKeyboardDismiss()
            }

            wasKeyboardVisibleByLayout = isKeyboardVisible
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
}
