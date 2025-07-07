package com.example.motivationcalendarapi.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.motivationcalendarapi.R

@Composable
fun AddProgressDialog(
    showDialog: Boolean,
    initialWeight: Double,
    onDismiss: () -> Unit,
    onSave: (Double, Uri) -> Unit
) {
    if (showDialog) {
        var weight by remember { mutableDoubleStateOf(initialWeight) }
        var photoUri by remember { mutableStateOf<Uri?>(null) }
        val isValid = photoUri != null && weight > 0.0

        val galleryLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { photoUri = it }
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
                        text = stringResource(R.string.add_progress),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorScheme.onBackground
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WeightRow(
                        value = weight,
                        onValueChange = { weight = it }
                    )

                    if (photoUri != null) {
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
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(photoUri)
                                        .apply {
                                            if (photoUri?.scheme == "content") {
                                                addHeader("Content-Type", "image/*")
                                            }
                                        }
                                        .build()
                                ),
                                contentDescription = stringResource(R.string.progress_photo),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)))
                        }
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = if (photoUri != null) stringResource(R.string.change_photo) else stringResource(R.string.choose_from_gallery),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSave(weight, photoUri!!)
                        onDismiss()
                    },
                    enabled = isValid,
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (photoUri != null && weight > 0) colorScheme.primary else colorScheme.secondary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.secondary
                    )
                }
            },
            containerColor = colorScheme.background,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(16.dp))
    }
}

@Composable
private fun WeightRow(
    value: Double,
    onValueChange: (Double) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = { onValueChange((value - 0.5).coerceAtLeast(0.0)) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_minus),
                contentDescription = stringResource(R.string.decrease_weight),
                tint = colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        OutlinedTextField(
            value = "%.1f".format(value),
            onValueChange = { input ->
                val newValue = input.toDoubleOrNull() ?: 0.0
                onValueChange(newValue.coerceIn(0.0, 200.0))
            },
            label = {
                Text(
                    text = stringResource(R.string.weight_kg),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurfaceVariant
                )
            },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                color = colorScheme.onBackground,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                autoCorrectEnabled  = false
            ),
            modifier = Modifier
                .width(160.dp)
                .padding(horizontal = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = colorScheme.onBackground,
                unfocusedTextColor = colorScheme.onBackground,
                focusedLabelColor = colorScheme.onSurfaceVariant,
                unfocusedLabelColor = colorScheme.onSurfaceVariant
            )
        )

        IconButton(
            onClick = { onValueChange((value + 0.5).coerceAtMost(500.0)) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = stringResource(R.string.increase_weight),
                tint = colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}