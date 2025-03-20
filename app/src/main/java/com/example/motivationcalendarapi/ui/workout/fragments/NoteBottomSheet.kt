package com.example.motivationcalendarapi.ui.workout.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.example.motivationcalendarapi.model.ExtendedExercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteBottomSheet(
    showBottomSheet: Boolean,
    exercise: ExtendedExercise,
    onDismiss: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    var noteText by remember(exercise.exercise.note) {
        mutableStateOf(exercise.exercise.note)
    }
    val charLimit = 100
    val charCount = noteText.length



    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                onSaveNote(noteText)
                onDismiss()
            }, containerColor = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp,end = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Note",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = exercise.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )


                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = {
                        if (it.length <= charLimit) noteText = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text
                    ),
                    placeholder = {
                        Text("Add your notes here...")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "$charCount/$charLimit",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (charCount == charLimit) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))


            }
        }
    }
}