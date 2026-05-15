package com.example.motivationcalendarapi.ui.equipment_recognition

import Screen
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.viewmodel.EquipmentRecognitionViewModel
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.SelectedEquipmentImage
import java.util.Locale

@Composable
fun EquipmentRecognitionScreen(
    navController: NavController,
    exerciseViewModel: ExerciseViewModel,
    recognitionViewModel: EquipmentRecognitionViewModel,
    paddingTopValues: Dp,
    lang: String
) {
    val state by recognitionViewModel.uiState.collectAsState()
    val allExercises by exerciseViewModel.getAllExercises().collectAsState(initial = emptyList())
    val noImageError = stringResource(R.string.equipment_recognizer_select_image_first)

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { recognitionViewModel.setGalleryImage(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { recognitionViewModel.setCameraImage(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(null)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingTopValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            ImagePreviewCard(selectedImage = state.selectedImage)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(modifier = Modifier.weight(1f), onClick = { galleryLauncher.launch("image/*") }) {
                    Text(stringResource(R.string.choose_from_gallery))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            cameraLauncher.launch(null)
                        }
                    }
                ) {
                    Text(stringResource(R.string.take_photo))
                }
            }
        }

        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedImage != null && !state.isAnalyzing,
                onClick = { recognitionViewModel.analyzeSelectedImage(allExercises, lang, noImageError) }
            ) {
                Text(stringResource(R.string.analyze_equipment))
            }
        }

        if (state.isAnalyzing) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        state.errorMessage?.let { message ->
            item { Text(text = message, color = MaterialTheme.colorScheme.error) }
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

            if (state.suitableExercises.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_suitable_exercises_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                items(state.suitableExercises) { exercise ->
                    EquipmentExerciseCard(exercise = exercise, lang = lang) {
                        navController.navigate("${Screen.ExerciseDetailView.route}/${exercise.id}")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(96.dp)) }
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
                is SelectedEquipmentImage.Gallery -> AsyncImage(
                    model = selectedImage.uri,
                    contentDescription = stringResource(R.string.selected_equipment_image),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                is SelectedEquipmentImage.Camera -> Image(
                    bitmap = selectedImage.bitmap.asImageBitmap(),
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
private fun EquipmentExerciseCard(exercise: Exercise, lang: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = exercise.getName(lang).firstOrNull()?.uppercase().orEmpty(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge
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
            }
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
