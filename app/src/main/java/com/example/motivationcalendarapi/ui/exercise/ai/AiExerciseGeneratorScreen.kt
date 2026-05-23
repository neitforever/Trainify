package com.example.motivationcalendarapi.ui.exercise.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.motivationcalendarapi.notifications.NotificationHelper
import com.example.motivationcalendarapi.repositories.NotificationSettingsDataStore
import kotlinx.coroutines.flow.first
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.BodyPart
import com.example.motivationcalendarapi.model.Equipment
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.model.getIconForEquipment
import com.example.motivationcalendarapi.repositories.ai.GeminiAiGenerationApi
import com.example.motivationcalendarapi.repositories.ai.GeneratedTemplateDraft
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import com.example.motivationcalendarapi.viewmodel.AiExerciseGenerationViewModel
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiExerciseGeneratorScreen(
    navController: NavController,
    exerciseViewModel: ExerciseViewModel,
    workoutViewModel: WorkoutViewModel,
    aiExerciseGenerationViewModel: AiExerciseGenerationViewModel,
    paddingTopValues: Dp,
    lang: String
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val allExercises by exerciseViewModel.getAllExercises().collectAsState(initial = emptyList())
    val bodyParts = remember(lang) { BodyPart.all.map { it.getLabel(lang) } }
    val equipmentList = remember(lang) { Equipment.all.map { it.getLabel(lang) } }
    val state by aiExerciseGenerationViewModel.uiState.collectAsState()
    var bodyPartExpanded by remember { mutableStateOf(false) }
    var equipmentExpanded by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val requiredFieldsMessage = stringResource(R.string.ai_generation_required_fields)
    val highDemandMessage = stringResource(R.string.gemini_high_demand_message)
    val mediumDifficulty = stringResource(R.string.medium)
    val inputEnabled = !state.isLoading

    LaunchedEffect(mediumDifficulty) {
        if (state.difficulty.isBlank()) {
            aiExerciseGenerationViewModel.setDifficulty(mediumDifficulty)
        }
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
                    AiExerciseHelpCard()
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    AiTextField(
                        value = state.prompt,
                        onValueChange = aiExerciseGenerationViewModel::setPrompt,
                        label = stringResource(R.string.ai_user_request),
                        minLines = 3,
                        enabled = inputEnabled
                    )
                }


                item {
                    SelectorRowWithIcon(
                        title = stringResource(R.string.body_part),
                        value = state.selectedBodyPart.ifBlank { stringResource(R.string.not_set) },
                        iconRes = if (state.selectedBodyPart.isNotBlank()) getIconForBodyPart(state.selectedBodyPart) else R.drawable.ic_body_upper_arms,
                        isFilled = state.selectedBodyPart.isNotBlank(),
                        expanded = bodyPartExpanded,
                        onExpandedChange = { if (inputEnabled) bodyPartExpanded = it },
                        options = bodyParts,
                        optionIcon = { option -> getIconForBodyPart(option) },
                        onSelected = { aiExerciseGenerationViewModel.setBodyPart(it) },
                        enabled = inputEnabled
                    )
                }


                item {
                    SelectorRowWithIcon(
                        title = stringResource(R.string.equipment),
                        value = state.selectedEquipment.ifBlank { stringResource(R.string.not_set) },
                        iconRes = if (state.selectedEquipment.isNotBlank()) getIconForEquipment(state.selectedEquipment) else R.drawable.ic_dumbbell,
                        isFilled = state.selectedEquipment.isNotBlank(),
                        expanded = equipmentExpanded,
                        onExpandedChange = { if (inputEnabled) equipmentExpanded = it },
                        options = equipmentList,
                        optionIcon = { option -> getIconForEquipment(option) },
                        onSelected = { aiExerciseGenerationViewModel.setEquipment(it) },
                        enabled = inputEnabled
                    )
                }


                item {
                    DifficultySelector(
                        selected = state.difficulty,
                        onSelected = aiExerciseGenerationViewModel::setDifficulty,
                        enabled = inputEnabled
                    )
                }
            }

            state.errorMessage?.let { message ->
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    ErrorCard(message, isNetworkLike = state.isNetworkError || state.isHighDemandError)
                }
            }

            saveError?.let { message ->
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    ErrorCard(message)
                }
            }

            state.draft?.let { exercise ->
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    ExercisePreviewEditor(
                        exercise = exercise,
                        lang = lang,
                        allExercises = allExercises,
                        selectedBodyPart = state.selectedBodyPart,
                        selectedEquipment = state.selectedEquipment,
                        onChange = aiExerciseGenerationViewModel::updateDraft
                    )
                }
            }

            item { Spacer(modifier = Modifier.absolutePadding(bottom = 160.dp)) }
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
                        aiExerciseGenerationViewModel.generate(
                            lang = lang,
                            localExercises = allExercises,
                            requiredFieldsMessage = requiredFieldsMessage,
                            highDemandMessage = highDemandMessage,
                            context = context
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
                    onClick = {
                        aiExerciseGenerationViewModel.clearDraft()
                        saveError = null
                    },
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
                        val exercise = state.draft ?: return@FloatingActionButton
                        scope.launch {
                            runCatching {
                                val savedExercise = exercise.normalizedAiExerciseForSave(lang)
                                exerciseViewModel.exerciseRepository.insertExercise(savedExercise)
                            }.onSuccess {
                                workoutViewModel.increaseAiExerciseCreatedForRewards()
                                aiExerciseGenerationViewModel.clearDraft()
                                navController.popBackStack()
                            }.onFailure { error ->
                                saveError = error.message
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
}


private fun Exercise.normalizedAiExerciseForSave(currentLang: String): Exercise {
    val normalized = normalizedForSave(currentLang)

    return normalized.copy(
        nameLocalized = normalized.nameLocalized.lowercaseValues(),
        bodyPartLocalized = normalized.bodyPartLocalized.lowercaseValues(),
        equipmentLocalized = normalized.equipmentLocalized.lowercaseValues(),
        targetLocalized = normalized.targetLocalized.lowercaseValues(),
        secondaryMusclesLocalized = normalized.secondaryMusclesLocalized.mapValues { (_, muscles) ->
            muscles.map { muscle -> muscle.trim().lowercase(Locale.ROOT) }
        },
        note = normalized.note.trim().lowercase(Locale.ROOT)
    )
}

private fun Map<String, String>.lowercaseValues(): Map<String, String> {
    return mapValues { (_, value) -> value.trim().lowercase(Locale.ROOT) }
}
