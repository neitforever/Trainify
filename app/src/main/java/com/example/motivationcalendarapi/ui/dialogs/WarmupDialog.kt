package com.example.motivationcalendarapi.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.motivationcalendarapi.R
import kotlin.math.roundToInt

@Composable
fun WarmupDialog(
    showDialog: Boolean, warmupTime: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit
) {
    if (showDialog) {
        val initialWarmupTime = remember(warmupTime) {
            ((warmupTime.toFloat() / 10f).roundToInt() * 10).coerceIn(0, 600)
        }
        var localWarmupTime by remember { mutableStateOf(initialWarmupTime) }

        AlertDialog(onDismissRequest = onDismiss, title = {
            Text(
                text = stringResource(R.string.set_rest_time),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
            )
        }, text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            localWarmupTime = (localWarmupTime - 10).coerceAtLeast(10)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_minus),
                            contentDescription = stringResource(R.string.restart),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = localWarmupTime.toFloat(),
                        onValueChange = { newValue ->
                            localWarmupTime = newValue.toInt()
                        },
                        valueRange = 10f..600f,
                        steps = 58,
                        colors = SliderDefaults.colors(
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent,
                        ),
                        modifier = Modifier.fillMaxWidth(0.76f)
                    )


                    IconButton(
                        onClick = {
                            localWarmupTime = (localWarmupTime + 10).coerceAtMost(600)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_plus),
                            contentDescription = stringResource(R.string.restart),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                    }
                }
                    Text(
                        text = "${localWarmupTime}s",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

            }
        }, confirmButton = {
            TextButton(onClick = {
                onConfirm(localWarmupTime)
                onDismiss()
            }) {
                Text(
                    text = stringResource(R.string.save),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }, dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }, properties = DialogProperties()
        )
    }
}