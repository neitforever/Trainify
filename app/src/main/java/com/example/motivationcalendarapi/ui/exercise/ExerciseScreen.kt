package com.example.motivationcalendarapi.ui.exercise

import LoadingView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.example.motivationcalendarapi.ui.exercise.fragments.CollapsibleBodyPartItem
import com.example.motivationcalendarapi.ui.exercise.fragments.ExerciseItem
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import java.util.Locale
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.ui.dialogs.DeleteTemplateDialog
import com.example.motivationcalendarapi.ui.exercise.fragments.CollapsibleTemplateHeader
import com.example.motivationcalendarapi.ui.exercise.fragments.TemplateItem


@Composable
fun ExerciseScreen(
    navController: NavController,
    viewModel: ExerciseViewModel,
    workoutViewModel: WorkoutViewModel,
    paddingTopValues: Dp
) {

    val templates by workoutViewModel.templates.collectAsState(initial = emptyList())

    val bodyParts by viewModel.allBodyParts.collectAsState(initial = emptyList())
    val sortedBodyParts = remember(bodyParts) {
        bodyParts.sortedBy { it.lowercase(Locale.getDefault()) }
    }

    val expandedBodyParts = remember { mutableStateMapOf<String, Boolean>() }
    var isTemplatesExpanded by remember { mutableStateOf(false) }

    val favoriteExercises by viewModel.getFavoriteExercises().collectAsState(initial = emptyList())

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedTemplateForDeletion by remember { mutableStateOf<Template?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchAndSaveExercises()
    }

    LaunchedEffect(Unit) {
        workoutViewModel.loadTemplates()
    }

    DeleteTemplateDialog(
        showDialog = showDeleteDialog,
        onDismiss = {
            showDeleteDialog = false
            selectedTemplateForDeletion = null
        },
        onConfirm = {
            selectedTemplateForDeletion?.let {
                workoutViewModel.deleteTemplate(it)
                showDeleteDialog = false
                selectedTemplateForDeletion = null
            }
        }
    )

    if (bodyParts.isEmpty()) {
        LoadingView()
    } else {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingTopValues)
        ) {

            item {
                CollapsibleTemplateHeader(
                    isExpanded = isTemplatesExpanded,
                    onClick = { isTemplatesExpanded = !isTemplatesExpanded }
                )

                AnimatedVisibility(
                    visible = isTemplatesExpanded,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    Column {
                        if (templates.isEmpty()) {
                            Text(
                                "No templates found",
                                modifier = Modifier.padding(16.dp))
                        } else {
                        templates.forEach { template ->
                            TemplateItem(
                                template = template,
                                onClick = {
                                    navController.navigate("${Screen.TemplateDetailView.route}/${template.id}")
                                },
                                onDelete = {
                                    selectedTemplateForDeletion = template
                                    showDeleteDialog = true
                                },
                                navController = navController
                            )
                        }
                        }
                    }
                }
            }


            items(sortedBodyParts) { bodyPart ->
                val isExpanded = expandedBodyParts[bodyPart] == true

                CollapsibleBodyPartItem(
                    bodyPart = bodyPart,
                    isExpanded = isExpanded,
                    onClick = { expandedBodyParts[bodyPart] = !isExpanded })

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(
                        expandFrom = Alignment.Top, initialHeight = { 0 }),
                    exit = fadeOut() + shrinkVertically(
                        shrinkTowards = Alignment.Top
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val exercises by viewModel.getExercisesByBodyPart(bodyPart)
                        .collectAsState(initial = emptyList())
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        exercises.forEach { exercise ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("${Screen.ExerciseDetailView.route}/${exercise.id}")
                                    }
                                    .padding(start = 16.dp, end = 16.dp)) {
                                ExerciseItem(
                                    exercise = exercise,
                                    onItemClick = { navController.navigate("exercise_detail/${exercise.id}") },
                                    onFavoriteClick = { viewModel.toggleFavorite(exercise) },
                                    isFavorite = favoriteExercises.any { it.id == exercise.id })
                            }
                        }
                    }
                }
            }
            item {
                Spacer(
                    modifier = Modifier.absolutePadding(bottom = 200.dp)
                )
            }

        }
    }
}




