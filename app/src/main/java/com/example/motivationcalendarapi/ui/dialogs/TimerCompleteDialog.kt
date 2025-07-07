package com.example.motivationcalendarapi.ui.dialogs


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.motivationcalendarapi.R
import kotlinx.coroutines.delay

@Composable
fun TimerCompleteDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        LaunchedEffect(Unit) {
            delay(1500)
            onDismiss()
        }

        Dialog(
            onDismissRequest = onDismiss,
            content = {
                Text(
                    text = stringResource(R.string.warmup_completed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}