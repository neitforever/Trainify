package com.example.motivationcalendarapi.ui.template

import LoadingView
import Screen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.ui.dialogs.DeleteTemplateDialog
import com.example.motivationcalendarapi.ui.template.fragments.AddExerciseTemplate
import com.example.motivationcalendarapi.ui.template.fragments.ExerciseTemplateItem
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import java.util.Collections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    templateId: String?,
    viewModel: WorkoutViewModel,
    navController: NavController,
    paddingTopValues: Dp,
    exerciseViewModel: ExerciseViewModel,
) {
    val template by viewModel.getTemplateById(templateId ?: "").collectAsState(initial = null)
    var showDeleteTemplateDialog by remember { mutableStateOf(false) }
    var sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val (showExerciseSheet, setShowExerciseSheet) = remember { mutableStateOf(false) }


    Scaffold(floatingActionButton = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.navigationBarsPadding()
        ) {
            FloatingActionButton(
                onClick = { setShowExerciseSheet(true)  },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add exercise",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            FloatingActionButton(
                onClick = { showDeleteTemplateDialog = true },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Delete template",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }) { paddingValues ->
        if (template == null) {
            LoadingView()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .padding(top = paddingTopValues)
            ) {
                item {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("${Screen.EditTemplateName.route}/${template?.id}")
                            }
                            .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {

                        Text(
                            text = template?.name?.replaceFirstChar { it.uppercase() } ?: "",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(start = 8.dp)
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 16.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                    )
                }


                template?.exercises?.let { exercises ->
                    itemsIndexed(exercises) { index, exercise ->
                        ExerciseTemplateItem(
                            index = index,
                            exercise = exercise,
                            onDelete = {
                                template?.let {
                                    val updatedExercises = it.exercises.toMutableList()
                                        .apply { removeAt(index) }
                                    viewModel.updateTemplateExercises(it.id, updatedExercises)
                                }
                            },
                            onMoveUp = {
                                if (index > 0) {
                                    val updated = exercises.toMutableList().apply {
                                        Collections.swap(this, index, index - 1)
                                    }
                                    viewModel.updateTemplateExercises(template!!.id, updated)
                                }
                            },
                            onMoveDown = {
                                if (index < exercises.size - 1) {
                                    val updated = exercises.toMutableList().apply {
                                        Collections.swap(this, index, index + 1)
                                    }
                                    viewModel.updateTemplateExercises(template!!.id, updated)
                                }
                            },
                            canMoveUp = index > 0,
                            canMoveDown = index < exercises.size - 1,
                            navController = navController,
                            workoutViewModel = viewModel,
                        )
                    }
                }
                item{
                    Spacer(modifier = Modifier.absolutePadding(bottom = 200.dp))
                }
            }
            if (showExerciseSheet) {
                AddExerciseTemplate(
                    isSheetOpen = showExerciseSheet,
                    onDismiss = { setShowExerciseSheet(false) },
                    sheetState = sheetState,
                    exerciseViewModel = exerciseViewModel,
                    existingExercises = template?.exercises?.map { it.exercise } ?: emptyList(),
                    onExercisesSelected = { newExercises ->
                        template?.let {
                            val updated = it.exercises + newExercises.map { ex ->
                                ExtendedExercise(ex, emptyList())
                            }
                            viewModel.updateTemplateExercises(it.id, updated)
                        }
                        setShowExerciseSheet(false)
                    }
                )
            }
        }
    }

    DeleteTemplateDialog(
        showDialog = showDeleteTemplateDialog,
        onDismiss = { showDeleteTemplateDialog = false },
        onConfirm = {
            template?.let {
                viewModel.deleteTemplate(it)
                navController.popBackStack()
            }
            showDeleteTemplateDialog = false
        }
    )
}




