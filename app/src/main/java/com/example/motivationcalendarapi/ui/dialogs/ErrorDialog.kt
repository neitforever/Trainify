package com.example.motivationcalendarapi.ui.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
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
fun ErrorDialog(
    showError: Boolean,
    onDismiss: () -> Unit
) {
    if (showError) {
        LaunchedEffect(Unit) {
            delay(1500)
            onDismiss()
        }

        Dialog(onDismissRequest = onDismiss) {
            Text(
                text = stringResource(R.string.please_fill_all_required_fields),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}