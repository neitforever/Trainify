package com.example.motivationcalendarapi.ui.exercise.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.getCardType
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.model.getIconForEquipment
import com.example.motivationcalendarapi.ui.template.fragments.AddExerciseTemplate
import com.example.motivationcalendarapi.viewmodel.AiTemplateGenerationViewModel
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTemplateGeneratorScreen(
    navController: NavController,
    exerciseViewModel: ExerciseViewModel,
    workoutViewModel: WorkoutViewModel,
    aiTemplateGenerationViewModel: AiTemplateGenerationViewModel,
    paddingTopValues: Dp,
    lang: String
) {
    val scope = rememberCoroutineScope()
    val allExercises by exerciseViewModel.getAllExercises().collectAsState(initial = emptyList())
    val bodyParts by exerciseViewModel.getBodyPartsLocalized(lang).collectAsState(initial = emptyList())
    val equipmentList by exerciseViewModel.getAllEquipmentLocalized(lang).collectAsState(initial = emptyList())
    val state by aiTemplateGenerationViewModel.uiState.collectAsState()
    val mediumDifficulty = stringResource(R.string.medium)
    val defaultTemplateName = stringResource(R.string.template)
    val requiredFieldsMessage = stringResource(R.string.ai_generation_required_fields)
    val highDemandMessage = stringResource(R.string.gemini_high_demand_message)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showExerciseSheet by remember { mutableStateOf(false) }
    val inputEnabled = !state.isLoading

    LaunchedEffect(mediumDifficulty) {
        aiTemplateGenerationViewModel.setDefaultDifficultyIfBlank(mediumDifficulty)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingTopValues)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (state.draft == null) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    AiTemplateHelpCard()
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    AiTextField(
                        value = state.prompt,
                        onValueChange = aiTemplateGenerationViewModel::setPrompt,
                        label = stringResource(R.string.ai_user_request),
                        minLines = 3,
                        enabled = inputEnabled
                    )
                }

                item {
                    MultiChoiceCardsSectionState(
                        title = stringResource(R.string.body_part),
                        options = bodyParts,
                        selected = state.selectedBodyParts,
                        iconForOption = { getIconForBodyPart(it) },
                        onToggle = aiTemplateGenerationViewModel::toggleBodyPart,
                        enabled = inputEnabled
                    )
                }

                item {
                    MultiChoiceCardsSectionState(
                        title = stringResource(R.string.equipment),
                        options = equipmentList,
                        selected = state.selectedEquipment,
                        iconForOption = { getIconForEquipment(it) },
                        onToggle = aiTemplateGenerationViewModel::toggleEquipment,
                        enabled = inputEnabled
                    )
                }

                item {
                    DifficultySelector(
                        selected = state.difficulty,
                        onSelected = aiTemplateGenerationViewModel::setDifficulty,
                        enabled = inputEnabled
                    )
                }

                item {
                    TemplateExerciseRangeSelector(
                        start = state.minExercises,
                        end = state.maxExercises,
                        onValueChange = { min, max -> aiTemplateGenerationViewModel.setExerciseRange(min, max) },
                        enabled = inputEnabled
                    )
                }
            }

            state.errorMessage?.let { message ->
                item { ErrorCard(message, isNetworkLike = state.isNetworkError || state.isHighDemandError) }
            }

            state.draft?.let { template ->
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    TemplatePreviewEditor(
                        draft = template,
                        lang = lang,
                        navController = navController,
                        workoutViewModel = workoutViewModel,
                        onChange = aiTemplateGenerationViewModel::updateDraft
                    )
                }
            }

            item { Spacer(modifier = Modifier.absolutePadding(bottom = 180.dp)) }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.draft == null) {
                FloatingActionButton(
                    onClick = {
                        if (!inputEnabled) return@FloatingActionButton
                        aiTemplateGenerationViewModel.generate(
                            lang = lang,
                            localExercises = allExercises,
                            requiredFieldsMessage = requiredFieldsMessage,
                            highDemandMessage = highDemandMessage
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_circle),
                            contentDescription = stringResource(R.string.generate),
                            modifier = Modifier.size(34.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                FloatingActionButton(
                    onClick = { if (!state.isLoading) showExerciseSheet = true },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = stringResource(R.string.add_exercise),
                        modifier = Modifier.size(34.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                FloatingActionButton(
                    onClick = { if (!state.isLoading) aiTemplateGenerationViewModel.clearDraft() },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier.size(36.dp)
                    )
                }

                FloatingActionButton(
                    onClick = {
                        if (state.isLoading) return@FloatingActionButton
                        val templateDraft = state.draft ?: return@FloatingActionButton
                        scope.launch {
                            val name = templateDraft.nameLocalized[lang]
                                ?: templateDraft.nameLocalized["en"]
                                ?: defaultTemplateName
                            val template = Template(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                nameLocalized = templateDraft.nameLocalized,
                                exercises = templateDraft.exercises.map { extended ->
                                    extended.copy(sets = extended.sets.map { it.copy(status = SetStatus.NONE) })
                                },
                                timestamp = System.currentTimeMillis()
                            )
                            runCatching { workoutViewModel.workoutRepository.insertTemplate(template) }
                                .onSuccess {
                                    aiTemplateGenerationViewModel.resetAfterSave()
                                    navController.popBackStack()
                                }
                                .onFailure { error ->
                                    aiTemplateGenerationViewModel.setError(error.message ?: "Save error")
                                }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_save),
                        contentDescription = stringResource(R.string.save),
                        modifier = Modifier.size(34.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (showExerciseSheet && state.draft != null) {
        AddExerciseTemplate(
            isSheetOpen = showExerciseSheet,
            onDismiss = { showExerciseSheet = false },
            sheetState = sheetState,
            exerciseViewModel = exerciseViewModel,
            existingExercises = state.draft?.exercises?.map { it.exercise } ?: emptyList(),
            onExercisesSelected = { selectedExercises ->
                aiTemplateGenerationViewModel.addExercises(selectedExercises, lang)
            },
            lang = lang
        )
    }
}
