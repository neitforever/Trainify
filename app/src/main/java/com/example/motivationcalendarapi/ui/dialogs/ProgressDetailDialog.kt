package com.example.motivationcalendarapi.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.BodyProgress
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProgressDetailDialog(
    progress: BodyProgress,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formattedDateTime = remember(progress.timestamp) {
        val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", locale)
        Instant.ofEpochMilli(progress.timestamp)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .widthIn(max = 1000.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.progress_details),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onBackground
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                progress.photoPath?.let { path ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.surfaceVariant)
                            .border(
                                width = 2.dp,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = colorScheme.primary
                            )
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = stringResource(R.string.progress_photo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.weight),
                            style = MaterialTheme.typography.headlineMedium,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.weight_with_unit_precise,progress.weight),
//                            text = "%.1fkg".format(progress.weight),
                            style = MaterialTheme.typography.headlineMedium,
                            color = colorScheme.onBackground
                        )
                    }

                    Text(
                        text = formattedDateTime,
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.close),
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.primary
                )
            }
        },
        containerColor = colorScheme.background,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(16.dp)
    )
}