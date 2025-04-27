package com.example.motivationcalendarapi.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SaveTemplateDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var templateName by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Save as Template",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter template name",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        label = { Text("Template name") },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (templateName.isNotBlank()) {
                            onConfirm(templateName)
                            onDismiss()
                        }
                    },
                ) {
                    Text(
                        text = "Save",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                ) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}

