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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.getCardType
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.model.getIconForEquipment
import com.example.motivationcalendarapi.repositories.ai.GeminiAiGenerationApi
import com.example.motivationcalendarapi.repositories.ai.GeneratedTemplateDraft
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import com.example.motivationcalendarapi.ui.template.fragments.ExerciseTemplateItem
import com.example.motivationcalendarapi.viewmodel.AiExerciseGenerationViewModel
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Collections

@Composable
internal fun ExercisePreviewEditor(
    exercise: Exercise,
    lang: String,
    allExercises: List<Exercise>,
    selectedBodyPart: String,
    selectedEquipment: String,
    onChange: (Exercise) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.preview_and_edit),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        PreviewLocalizedStringCard(
            title = stringResource(R.string.name),
            lang = lang,
            values = exercise.nameLocalized
        ) { onChange(exercise.copy(nameLocalized = it)) }

        PreviewBodyEquipmentCard(
            title = stringResource(R.string.body_part),
            selected = exercise.getBodyPart(lang).ifBlank { selectedBodyPart },
            iconRes = getIconForBodyPart(exercise.getBodyPart(lang).ifBlank { selectedBodyPart }),
            options = allExercises.map { it.getBodyPart(lang) }.filter { it.isNotBlank() }.distinct().sorted(),
            optionIcon = { getIconForBodyPart(it) },
            onSelected = { selected ->
                val bodyMap = allExercises.firstOrNull { it.getBodyPart(lang) == selected }?.bodyPartLocalized
                    ?: mapOf("en" to selected, "ru" to selected, "be" to selected)
                onChange(exercise.copy(bodyPartLocalized = bodyMap))
            }
        )

        PreviewBodyEquipmentCard(
            title = stringResource(R.string.equipment),
            selected = exercise.getEquipment(lang).ifBlank { selectedEquipment },
            iconRes = getIconForEquipment(exercise.getEquipment(lang).ifBlank { selectedEquipment }),
            options = allExercises.map { it.getEquipment(lang) }.filter { it.isNotBlank() }.distinct().sorted(),
            optionIcon = { getIconForEquipment(it) },
            onSelected = { selected ->
                val equipmentMap = allExercises.firstOrNull { it.getEquipment(lang) == selected }?.equipmentLocalized
                    ?: mapOf("en" to selected, "ru" to selected, "be" to selected)
                onChange(exercise.copy(equipmentLocalized = equipmentMap))
            }
        )

        PreviewLocalizedStringCard(
            title = stringResource(R.string.target),
            lang = lang,
            values = exercise.targetLocalized
        ) { onChange(exercise.copy(targetLocalized = it)) }

        PreviewCardTypeCard(
            selected = exercise.cardType,
            onSelected = { onChange(exercise.copy(cardType = it)) }
        )

        PreviewLocalizedListCard(
            title = stringResource(R.string.instructions),
            lang = lang,
            values = exercise.instructionsLocalized
        ) { onChange(exercise.copy(instructionsLocalized = it)) }
    }
}

@Composable
internal fun PreviewLocalizedStringCard(
    title: String,
    lang: String,
    values: Map<String, String>,
    onChange: (Map<String, String>) -> Unit
) {
    CardBlock(title = title) {
        AiTextField(
            value = values[lang].orEmpty(),
            onValueChange = { value ->
                onChange(values.toMutableMap().apply { this[lang] = value })
            },
            label = title
        )
    }
}

@Composable
internal fun PreviewLocalizedListCard(
    title: String,
    lang: String,
    values: Map<String, List<String>>,
    onChange: (Map<String, List<String>>) -> Unit
) {
    CardBlock(title = title) {
        val joined = values[lang].orEmpty().joinToString("\n")
        AiTextField(
            value = joined,
            onValueChange = { value ->
                onChange(values.toMutableMap().apply {
                    this[lang] = value.lines().map { it.trim() }.filter { it.isNotBlank() }
                })
            },
            label = title,
            minLines = 4
        )
    }
}

@Composable
internal fun PreviewBodyEquipmentCard(
    title: String,
    selected: String,
    iconRes: Int,
    options: List<String>,
    optionIcon: (String) -> Int,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 210),
        label = "$title preview arrow rotation"
    )

    CardBlock(title = title) {
        SelectableHeaderCard(
            value = selected.ifBlank { stringResource(R.string.not_set) },
            iconRes = iconRes,
            isFilled = selected.isNotBlank(),
            rotation = rotation,
            onClick = { expanded = !expanded }
        )

        AnimatedVisibility(visible = expanded) {
            InlineOptionsList(
                options = options,
                optionIcon = optionIcon,
                optionContainerColor = MaterialTheme.colorScheme.surface,
                onSelected = { option ->
                    onSelected(option)
                    expanded = false
                }
            )
        }
    }
}

@Composable
internal fun PreviewCardTypeCard(selected: String, onSelected: (String) -> Unit) {
    val options = ExerciseCardType.entries.map { it.name }
    PreviewBodyEquipmentCard(
        title = stringResource(R.string.exercise_type),
        selected = selected,
        iconRes = cardTypeIcon(selected),
        options = options,
        optionIcon = { cardTypeIcon(it) },
        onSelected = onSelected
    )
}

internal fun cardTypeIcon(cardType: String): Int = when (cardType) {
    ExerciseCardType.BIKE.name -> R.drawable.ic_card_bike
    ExerciseCardType.TREADMILL.name -> R.drawable.ic_card_treadmill
    else -> R.drawable.ic_dumbbell
}


@Composable
internal fun TemplatePreviewEditor(
    draft: GeneratedTemplateDraft,
    lang: String,
    navController: NavController,
    workoutViewModel: WorkoutViewModel,
    onChange: (GeneratedTemplateDraft) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.preview_and_edit),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        PreviewLocalizedStringCard(
            title = stringResource(R.string.template_name),
            lang = lang,
            values = draft.nameLocalized
        ) { updatedName ->
            onChange(draft.copy(nameLocalized = updatedName))
        }

        draft.exercises.forEachIndexed { index, extended ->
            ExerciseTemplateItem(
                index = index,
                exercise = extended,
                templateId = "ai_template_preview",
                onDelete = {
                    val updated = draft.exercises.toMutableList().apply { removeAt(index) }
                    onChange(draft.copy(exercises = updated))
                },
                onMoveUp = {
                    if (index > 0) {
                        val updated = draft.exercises.toMutableList().apply {
                            Collections.swap(this, index, index - 1)
                        }
                        onChange(draft.copy(exercises = updated))
                    }
                },
                onMoveDown = {
                    if (index < draft.exercises.lastIndex) {
                        val updated = draft.exercises.toMutableList().apply {
                            Collections.swap(this, index, index + 1)
                        }
                        onChange(draft.copy(exercises = updated))
                    }
                },
                canMoveUp = index > 0,
                canMoveDown = index < draft.exercises.lastIndex,
                onAddSetClick = { exerciseIndex ->
                    val updated = draft.exercises.toMutableList()
                    val current = updated[exerciseIndex]
                    val cardType = current.exercise.getCardType(lang)
                    if (cardType == ExerciseCardType.STRENGTH) {
                        val nextSet = current.sets.lastOrNull() ?: ExerciseSet(rep = 12, weight = 30f, status = SetStatus.NONE)
                        updated[exerciseIndex] = current.copy(sets = current.sets + nextSet.copy(status = SetStatus.NONE))
                        onChange(draft.copy(exercises = updated))
                    }
                },
                onRepClick = { _, _ -> },
                onWeightClick = { _, _ -> },
                onTimeClick = { _, _ -> },
                onResistanceClick = { _, _ -> },
                onInclineClick = { _, _ -> },
                onStatusClick = { exerciseIndex, setIndex, status ->
                    val updated = draft.exercises.toMutableList()
                    val current = updated[exerciseIndex]
                    updated[exerciseIndex] = current.copy(
                        sets = current.sets.mapIndexed { currentSetIndex, set ->
                            if (currentSetIndex == setIndex) set.copy(status = status) else set
                        }
                    )
                    onChange(draft.copy(exercises = updated))
                },
                navController = navController,
                onDeleteSet = { _, exerciseIndex, setIndex ->
                    val updated = draft.exercises.toMutableList()
                    val current = updated[exerciseIndex]
                    if (current.sets.size > 1) {
                        updated[exerciseIndex] = current.copy(
                            sets = current.sets.toMutableList().apply { removeAt(setIndex) }
                        )
                        onChange(draft.copy(exercises = updated))
                    }
                },
                exerciseSets = extended.sets,
                workoutViewModel = workoutViewModel,
                lang = lang
            )
        }
    }
}


