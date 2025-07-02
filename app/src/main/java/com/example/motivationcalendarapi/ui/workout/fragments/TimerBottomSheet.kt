package com.example.motivationcalendarapi.ui.workout.fragments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerBottomSheet(
    showSheet: Boolean,
    currentTime: Int,
    warmupTime: Int,
    isTimerRunning: Boolean,
    onDismiss: () -> Unit,
    onRestartClick: () -> Unit,
    onToggleTimer: () -> Unit,
    onEditTimeClick: () -> Unit
) {
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onRestartClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_restart),
                            contentDescription = stringResource(R.string.restart),
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(200.dp)
                            .clickable(onClick = onEditTimeClick)
                    ) {
                        CircularProgressIndicator(
                            progress = { currentTime.toFloat() / warmupTime.toFloat() },
                            modifier = Modifier.size(200.dp),
                            strokeWidth = 8.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$currentTime", style = MaterialTheme.typography.displayLarge
                        )
                    }



                    IconButton(onClick = onToggleTimer) {
                        Icon(
                            painter = painterResource(id = if (isTimerRunning) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                            contentDescription = if (isTimerRunning) stringResource(R.string.pause) else stringResource(R.string.start),
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }


                }

            }
        }
    }
}
