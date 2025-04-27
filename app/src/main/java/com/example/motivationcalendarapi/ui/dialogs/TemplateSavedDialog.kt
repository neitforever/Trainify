package com.example.motivationcalendarapi.ui.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TemplateSavedDialog(
    showDialog: Boolean,
    templateName: String,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        LaunchedEffect(Unit) {
            delay(1500)
            onDismiss()
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Template Saved",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = "Template '$templateName' was successfully saved!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

