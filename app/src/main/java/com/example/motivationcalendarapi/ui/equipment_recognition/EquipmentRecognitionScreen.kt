package com.example.motivationcalendarapi.ui.equipment_recognition

import Screen
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.getIconForEquipment
import com.example.motivationcalendarapi.viewmodel.EquipmentRecognitionViewModel
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.EquipmentMatchType
import com.example.motivationcalendarapi.viewmodel.MatchedExercise
import com.example.motivationcalendarapi.viewmodel.SelectedEquipmentImage
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import java.util.Locale

@Composable
fun EquipmentRecognitionScreen(
    navController: NavController,
    exerciseViewModel: ExerciseViewModel,
    recognitionViewModel: EquipmentRecognitionViewModel,
    workoutViewModel: WorkoutViewModel,
    paddingTopValues: Dp,
    lang: String
) {
    val state by recognitionViewModel.uiState.collectAsState()
    val allExercises by exerciseViewModel.getAllExercises().collectAsState(initial = emptyList())
    val noImageError = stringResource(R.string.equipment_recognizer_select_image_first)
    androidx.compose.runtime.LaunchedEffect(allExercises, state.recognizedEquipment) {
        recognitionViewModel.restoreMatchedExercises(allExercises)
    }

    androidx.compose.runtime.LaunchedEffect(state.recognizedEquipment?.equipmentKey) {
        if (state.recognizedEquipment != null) {
            workoutViewModel.unlockEquipmentRecognizerUsedForRewards()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { recognitionViewModel.setGalleryImage(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { recognitionViewModel.setCameraImage(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(null)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingTopValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            ImagePreviewCard(selectedImage = state.selectedImage)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                EquipmentActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.choose_from_gallery),
                    iconRes = R.drawable.ic_gallery,
                    enabled = !state.isAnalyzing,
                    onClick = { galleryLauncher.launch("image/*") }
                )
                EquipmentActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.take_photo),
                    iconRes = R.drawable.ic_camera,
                    enabled = !state.isAnalyzing,
                    onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
        }



        state.errorMessage?.let { message ->
            item {
                EquipmentErrorCard(
                    message = message,
                    isVpnRequired = state.isVpnRequiredError,
                    isHighDemand = state.isHighDemandError
                )
            }
        }

        state.recognizedEquipment?.let { equipment ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.recognized_equipment),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = equipment.getName(lang).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = equipment.getDescription(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.confidence_percent, (equipment.confidence * 100).toInt()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (equipment.alternatives.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.alternative_variants) + ": " + equipment.alternatives.joinToString { alt ->
                                    "${alt.equipmentKey.replace('_', ' ')} ${(alt.confidence * 100).toInt()}%"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.suitable_exercises),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (state.matchedExercises.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_suitable_exercises_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                items(state.matchedExercises) { matched ->
                    EquipmentExerciseCard(matchedExercise = matched, lang = lang, enabled = !state.isAnalyzing) {
                        navController.navigate("${Screen.ExerciseDetailView.route}/${matched.exercise.id}")
                    }
                }
            }
        }

            item { Spacer(modifier = Modifier.height(112.dp)) }
        }

        EquipmentAnalyzeFab(
            enabled = state.selectedImage != null && !state.isAnalyzing,
            isAnalyzing = state.isAnalyzing,
            onClick = { recognitionViewModel.analyzeSelectedImage(allExercises, noImageError) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun EquipmentAnalyzeFab(
    enabled: Boolean,
    isAnalyzing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = { if (enabled) onClick() },
        modifier = modifier,
        expanded = !isAnalyzing,
        containerColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(18.dp),
        icon = {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    strokeCap = StrokeCap.Round,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        text = {
            Text(
                text = stringResource(R.string.analyze_equipment),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
private fun EquipmentActionButton(
    modifier: Modifier = Modifier,
    text: String,
    iconRes: Int,
    enabled: Boolean = true,
    isAnalyzeButton: Boolean = false,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = if (isAnalyzeButton) {
            modifier
        } else {
            modifier.height(96.dp)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isAnalyzeButton) {
                colorScheme.primary
            } else {
                colorScheme.surfaceVariant
            },
            contentColor = if (isAnalyzeButton) {
                Color(0xFF1C1C1C)
            } else {
                colorScheme.onSurface
            },
            disabledContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.55f),
            disabledContentColor = colorScheme.onSurface.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        if (isAnalyzeButton) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EquipmentErrorCard(message: String, isVpnRequired: Boolean, isHighDemand: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = if (isVpnRequired || isHighDemand) R.drawable.ic_info else R.drawable.ic_close),
                contentDescription = null,
                tint = colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        when {
                            isVpnRequired -> R.string.vpn_required_title
                            isHighDemand -> R.string.gemini_high_demand_title
                            else -> R.string.recognition_error_title
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ImagePreviewCard(selectedImage: SelectedEquipmentImage?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            when (selectedImage) {
                is SelectedEquipmentImage -> AsyncImage(
                    model = selectedImage.uri,
                    contentDescription = stringResource(R.string.selected_equipment_image),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dumbbell),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.equipment_recognizer_empty_image),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EquipmentExerciseCard(matchedExercise: MatchedExercise, lang: String, enabled: Boolean = true, onClick: () -> Unit) {
    val exercise = matchedExercise.exercise
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = getIconForEquipment(exercise.getEquipment(lang))),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = exercise.getName(lang),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${exercise.getBodyPart(lang)} • ${exercise.getTarget(lang)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (matchedExercise.matchType is EquipmentMatchType.Exact) stringResource(R.string.exact_match) else stringResource(R.string.similar_match),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
