package com.example.motivationcalendarapi.ui.workout.planning

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExerciseCatalog
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.ui.dialogs.TimeMetricDialog
import com.example.motivationcalendarapi.ui.exercise.ai.MultiChoiceCardsSectionState
import com.example.motivationcalendarapi.ui.exercise.ai.isAiChooseOption
import com.example.motivationcalendarapi.ui.exercise.ai.safeEquipmentIcon
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import com.example.motivationcalendarapi.viewmodel.TrainingPlanCreationViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanCreationBottomSheet(
    templates: List<Template>,
    lang: String,
    drawerState: MutableState<DrawerState>,
    allExercises: List<Exercise>,
    trainingPlanCreationViewModel: TrainingPlanCreationViewModel,
    onDismiss: () -> Unit,
    onCreateAiPlan: (
        dates: List<LocalDate>,
        prompt: String,
        selectedBodyParts: List<String>,
        selectedEquipment: List<String>,
        difficulty: String,
        exerciseCount: Int,
        durationMinutes: Int,
        aiReason: String,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    onCreateTemplatePlan: (List<LocalDate>, Template) -> Unit
) {
    val text = remember(lang) { TrainingPlanCreationText(lang) }
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    val bodyPartGroups = remember(lang) { ExerciseCatalog.groupedBodyPartLabels(lang) }
    val bodyPartExerciseCounts = remember(allExercises, lang) { allExercises.groupingBy { it.getBodyPart(lang) }.eachCount() }
    val bodyPartAutoLabel = text.aiChooseBodyPart
    val bodyPartGroupsWithAi = remember(lang, bodyPartAutoLabel, bodyPartGroups) {
        listOf(text.aiSelectionGroupTitle to listOf(bodyPartAutoLabel)) + bodyPartGroups
    }
    val equipmentAutoLabel = text.aiChooseEquipment
    val equipmentGroupsWithAi = remember(lang, equipmentAutoLabel) {
        listOf(text.aiSelectionGroupTitle to listOf(equipmentAutoLabel)) + ExerciseCatalog.groupedEquipmentLabels(lang)
    }

    val state by trainingPlanCreationViewModel.uiState.collectAsState()
    trainingPlanCreationViewModel.ensureDefaults(defaultIntensity = text.medium)

    val prompt = state.prompt
    val weekCount = state.weekCount
    val selectedDays = state.selectedDays
    val selectedBodyParts = state.selectedBodyParts
    val selectedEquipment = state.selectedEquipment
    val bodyPartSectionExpanded = state.bodyPartSectionExpanded
    val equipmentSectionExpanded = state.equipmentSectionExpanded
    val durationMinutes = state.durationMinutes
    val exerciseCount = state.exerciseCount
    val intensity = state.intensity.ifBlank { text.medium }
    val isGenerating = state.isGenerating
    val errorMessage = state.errorMessage
    var showDurationDialog by remember { mutableStateOf(false) }

    val selectedDates = remember(today, weekCount, selectedDays) {
        buildPlanDates(startDate = today, weekCount = weekCount, selectedDays = selectedDays)
    }
    val inputEnabled = !isGenerating
    val canGenerate = selectedDates.isNotEmpty() && allExercises.isNotEmpty() && !isGenerating

    TimeMetricDialog(
        showDialog = showDurationDialog,
        title = text.durationTitle,
        initialValueMinutes = durationMinutes.toFloat(),
        minValueMinutes = 1f,
        maxValueMinutes = 300f,
        stepValueMinutes = 1f,
        onDismiss = { showDurationDialog = false },
        onSave = { minutes ->
            trainingPlanCreationViewModel.setDurationMinutes(minutes.toInt())
            showDurationDialog = false
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = 32.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.value.open() } }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = text.title,
                        style = MaterialTheme.typography.headlineLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                    )
                },
                modifier = Modifier.border(
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                    shape = CutCornerShape(4.dp)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(2.dp)) }
                item { PlanHelpCard(text) }
                item {
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = trainingPlanCreationViewModel::setPrompt,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        label = { Text(text.promptLabel) },
                        placeholder = { Text(text.promptPlaceholder) },
                        shape = RoundedCornerShape(12.dp),
                        enabled = inputEnabled
                    )
                }
                item { PlanPeriodCard(text, weekCount, inputEnabled) { trainingPlanCreationViewModel.setWeekCount(it) } }
                item { TrainingDaysCard(text, selectedDays, inputEnabled) { trainingPlanCreationViewModel.setSelectedDays(it) } }
                item {
                    MultiChoiceCardsSectionState(
                        title = text.bodyPartTitle,
                        optionGroups = bodyPartGroupsWithAi,
                        selected = if (selectedBodyParts.isEmpty()) listOf(bodyPartAutoLabel) else selectedBodyParts,
                        iconForOption = { option -> if (isAiChooseOption(option, bodyPartAutoLabel)) R.drawable.ic_reward_fg_ai_template else getIconForBodyPart(option) },
                        onToggle = { option ->
                            trainingPlanCreationViewModel.setSelectedBodyParts(
                                if (isAiChooseOption(option, bodyPartAutoLabel)) {
                                    emptyList()
                                } else if (selectedBodyParts.contains(option)) {
                                    selectedBodyParts - option
                                } else {
                                    selectedBodyParts + option
                                }
                            )
                        },
                        enabled = inputEnabled,
                        expanded = bodyPartSectionExpanded,
                        onExpandedChange = trainingPlanCreationViewModel::setBodyPartSectionExpanded,
                        exerciseCounts = bodyPartExerciseCounts
                    )
                }
                item {
                    MultiChoiceCardsSectionState(
                        title = text.equipmentTitle,
                        optionGroups = equipmentGroupsWithAi,
                        selected = if (selectedEquipment.isEmpty()) listOf(equipmentAutoLabel) else selectedEquipment,
                        iconForOption = { option -> safeEquipmentIcon(option, equipmentAutoLabel) },
                        onToggle = { option ->
                            trainingPlanCreationViewModel.setSelectedEquipment(
                                if (isAiChooseOption(option, equipmentAutoLabel)) {
                                    emptyList()
                                } else if (selectedEquipment.contains(option)) {
                                    selectedEquipment - option
                                } else {
                                    selectedEquipment + option
                                }
                            )
                        },
                        enabled = inputEnabled,
                        expanded = equipmentSectionExpanded,
                        onExpandedChange = trainingPlanCreationViewModel::setEquipmentSectionExpanded
                    )
                }
                item { PlanDurationCard(text, text.durationValue(durationMinutes), inputEnabled) { showDurationDialog = true } }
                item { PlanExerciseCountCard(text, exerciseCount, inputEnabled) { trainingPlanCreationViewModel.setExerciseCount(it) } }
                item { PlanIntensityCard(text, intensity, inputEnabled) { trainingPlanCreationViewModel.setIntensity(it) } }
                item { PlanPreviewCard(text, selectedDates, weekCount, durationMinutes, exerciseCount, intensity, selectedBodyParts) }
                item { PlanGenerationStateCard(text, isGenerating, errorMessage) }
                item { Spacer(Modifier.height(94.dp)) }
            }

            Button(
                onClick = {
                    if (!canGenerate) return@Button
                    trainingPlanCreationViewModel.setGenerating(true)
                    val reason = text.plannedReason(durationMinutes, exerciseCount, intensity)
                    onCreateAiPlan(
                        selectedDates,
                        text.modelPrompt(prompt, weekCount, selectedDays, durationMinutes, exerciseCount, intensity),
                        selectedBodyParts,
                        selectedEquipment,
                        intensity,
                        exerciseCount,
                        durationMinutes,
                        reason,
                        {
                            trainingPlanCreationViewModel.reset(defaultIntensity = text.medium)
                            onDismiss()
                        },
                        { message ->
                            trainingPlanCreationViewModel.setError(message)
                        }
                    )
                },
                enabled = canGenerate,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                Text(if (isGenerating) text.generatingAction else text.generateAction)
            }
        }
    }
}

private fun buildPlanDates(startDate: LocalDate, weekCount: Int, selectedDays: Set<DayOfWeek>): List<LocalDate> {
    if (selectedDays.isEmpty()) return emptyList()
    val endExclusive = startDate.plusWeeks(weekCount.toLong())
    val result = mutableListOf<LocalDate>()
    var date = startDate
    while (date.isBefore(endExclusive)) {
        if (date.dayOfWeek in selectedDays) result += date
        date = date.plusDays(1)
    }
    return result
}

@Composable
private fun PlanHelpCard(text: TrainingPlanCreationText) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(modifier = Modifier.size(46.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_reward_fg_ai_template), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = text.helpTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = text.helpBody, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

@Composable
private fun PlanPeriodCard(text: TrainingPlanCreationText, weekCount: Int, enabled: Boolean, onValueChange: (Int) -> Unit) {
    PlanSectionCard(title = text.periodTitle, subtitle = text.periodSubtitle) {
        Text(text = text.weeksValue(weekCount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Slider(
            value = weekCount.toFloat(),
            enabled = enabled,
            onValueChange = { onValueChange(it.toInt().coerceIn(1, 12)) },
            valueRange = 1f..12f,
            steps = 10,
            colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary, inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant, thumbColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun TrainingDaysCard(text: TrainingPlanCreationText, selectedDays: Set<DayOfWeek>, enabled: Boolean, onValueChange: (Set<DayOfWeek>) -> Unit) {
    PlanSectionCard(title = text.trainingDaysTitle, subtitle = text.trainingDaysSubtitle) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DayOfWeek.values().toList().chunked(4).forEach { rowDays ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowDays.forEach { day ->
                        val selected = day in selectedDays
                        TrainingDayOptionCard(
                            label = text.dayShort(day),
                            selected = selected,
                            enabled = enabled,
                            onClick = { onValueChange(if (selected) selectedDays - day else selectedDays + day) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - rowDays.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun TrainingDayOptionCard(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        onClick = onClick,
        enabled = enabled
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PlanDurationCard(text: TrainingPlanCreationText, value: String, enabled: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        onClick = onClick,
        enabled = enabled
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))) {
                Box(contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.ic_time), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp)) }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = text.durationTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = text.tapToChangeDuration, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_arrow_right), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(23.dp))
                }
            }
        }
    }
}

@Composable
private fun PlanExerciseCountCard(text: TrainingPlanCreationText, exerciseCount: Int, enabled: Boolean, onValueChange: (Int) -> Unit) {
    PlanSectionCard(title = text.exerciseCountTitle, subtitle = text.exerciseCountSubtitle) {
        Text(text = text.exerciseCountValue(exerciseCount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Slider(
            value = exerciseCount.toFloat(),
            enabled = enabled,
            onValueChange = { onValueChange(it.toInt().coerceIn(1, 15)) },
            valueRange = 1f..15f,
            steps = 13,
            colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary, inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant, thumbColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun PlanIntensityCard(text: TrainingPlanCreationText, selected: String, enabled: Boolean, onSelect: (String) -> Unit) {
    PlanSectionCard(title = text.intensityTitle, subtitle = text.intensitySubtitle) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PlanIntensityOption(text.light, R.drawable.ic_smile_easy, selected == text.light, enabled, { onSelect(text.light) }, Modifier.weight(1f))
            PlanIntensityOption(text.medium, R.drawable.ic_smile_normal, selected == text.medium, enabled, { onSelect(text.medium) }, Modifier.weight(1f))
            PlanIntensityOption(text.hard, R.drawable.ic_smile_hard, selected == text.hard, enabled, { onSelect(text.hard) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PlanIntensityOption(title: String, icon: Int, selected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val iconColor = when (icon) {
        R.drawable.ic_smile_easy -> EASY_COLOR
        R.drawable.ic_smile_normal -> NORMAL_COLOR
        R.drawable.ic_smile_hard -> HARD_COLOR
        else -> MaterialTheme.colorScheme.primary
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) iconColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(width = if (selected) 2.dp else 1.dp, color = if (selected) iconColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        onClick = onClick,
        enabled = enabled
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painter = painterResource(id = icon), contentDescription = title, tint = iconColor, modifier = Modifier.size(30.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = if (selected) iconColor else MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PlanPreviewCard(text: TrainingPlanCreationText, dates: List<LocalDate>, weekCount: Int, durationMinutes: Int, exerciseCount: Int, intensity: String, selectedBodyParts: List<String>) {
    PlanSectionCard(title = text.previewTitle, subtitle = text.previewSubtitle) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanPreviewMiniCard(label = text.previewWeeksLabel, value = text.weeksValue(weekCount), modifier = Modifier.weight(1f))
                PlanPreviewMiniCard(label = text.previewWorkoutsLabel, value = text.workoutsValue(dates.size), modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanPreviewMiniCard(label = text.previewDurationLabel, value = text.durationValue(durationMinutes), modifier = Modifier.weight(1f))
                PlanPreviewMiniCard(label = text.previewExercisesLabel, value = text.exerciseCountValue(exerciseCount), modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanPreviewMiniCard(label = text.previewIntensityLabel, value = intensity, modifier = Modifier.weight(1f))
                PlanPreviewMiniCard(label = text.previewMuscleLabel, value = text.musclePreviewValue(selectedBodyParts), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PlanPreviewMiniCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PlanGenerationStateCard(text: TrainingPlanCreationText, isGenerating: Boolean, error: String?) {
    val isError = error != null
    val tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.14f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = tint.copy(alpha = 0.12f)) {
                Box(contentAlignment = Alignment.Center) { Icon(painterResource(if (isError) R.drawable.ic_delete else R.drawable.ic_reward_fg_ai_template), contentDescription = null, tint = tint, modifier = Modifier.size(24.dp)) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = when { isGenerating -> text.generatingTitle; isError -> text.errorTitle; else -> text.readyTitle }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isError) tint else MaterialTheme.colorScheme.onSurface)
                Text(text = error ?: if (isGenerating) text.generatingBody else text.readyBody, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PlanSectionCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

private data class TrainingPlanCreationText(val lang: String) {
    val title = when (lang) { "ru" -> "Создание плана"; "be" -> "Стварэнне плана"; else -> "Create plan" }
    val helpTitle = when (lang) { "ru" -> "Создай долгосрочный план"; "be" -> "Ствары доўгатэрміновы план"; else -> "Create a long-term plan" }
    val helpBody = when (lang) { "ru" -> "Выбери период, дни тренировок, мышцы, оборудование и параметры нагрузки."; "be" -> "Выберы перыяд, дні трэніровак, мышцы, абсталяванне і параметры нагрузкі."; else -> "Choose period, training days, muscles, equipment and load parameters." }
    val promptLabel = when (lang) { "ru" -> "Цель плана"; "be" -> "Мэта плана"; else -> "Plan goal" }
    val promptPlaceholder = when (lang) { "ru" -> "Например: 4 недели для набора мышц, акцент на грудь и спину"; "be" -> "Напрыклад: 4 тыдні для набору мышцаў, акцэнт на грудзі і спіне"; else -> "Example: 4 weeks for muscle gain, focus on chest and back" }
    val periodTitle = when (lang) { "ru" -> "Период"; "be" -> "Перыяд"; else -> "Plan period" }
    val periodSubtitle = when (lang) { "ru" -> "Сколько недель нужно спланировать"; "be" -> "Колькі тыдняў трэба спланаваць"; else -> "How many weeks to schedule" }
    val trainingDaysTitle = when (lang) { "ru" -> "Дни тренировок"; "be" -> "Дні трэніровак"; else -> "Training days" }
    val trainingDaysSubtitle = when (lang) { "ru" -> "Выбери дни недели для тренировок"; "be" -> "Выберы дні тыдня для трэніровак"; else -> "Choose weekly workout days" }
    val bodyPartTitle = when (lang) { "ru" -> "Группы мышц"; "be" -> "Групы мышцаў"; else -> "Muscle groups" }
    val equipmentTitle = when (lang) { "ru" -> "Оборудование"; "be" -> "Абсталяванне"; else -> "Equipment" }
    val aiChooseBodyPart = when (lang) { "ru" -> "AI выберет мышцы"; "be" -> "AI выбера мышцы"; else -> "Let AI choose" }
    val aiChooseEquipment = when (lang) { "ru" -> "AI выберет оборудование"; "be" -> "AI выбера абсталяванне"; else -> "Let AI choose" }
    val aiSelectionGroupTitle = when (lang) { "ru" -> "Автоматический выбор"; "be" -> "Аўтаматычны выбар"; else -> "Automatic selection" }
    val durationTitle = when (lang) { "ru" -> "Длительность"; "be" -> "Працягласць"; else -> "Duration" }
    val tapToChangeDuration = when (lang) { "ru" -> "Нажми, чтобы изменить"; "be" -> "Націсні, каб змяніць"; else -> "Tap to change" }
    val exerciseCountTitle = when (lang) { "ru" -> "Количество упражнений"; "be" -> "Колькасць практыкаванняў"; else -> "Exercise count" }
    val exerciseCountSubtitle = when (lang) { "ru" -> "Одно значение для каждой тренировки"; "be" -> "Адно значэнне для кожнай трэніроўкі"; else -> "One value for each workout" }
    val intensityTitle = when (lang) { "ru" -> "Интенсивность"; "be" -> "Інтэнсіўнасць"; else -> "Intensity" }
    val intensitySubtitle = when (lang) { "ru" -> "Уровень нагрузки для плана"; "be" -> "Узровень нагрузкі для плана"; else -> "Target load for the plan" }
    val previewTitle = when (lang) { "ru" -> "Предпросмотр"; "be" -> "Папярэдні прагляд"; else -> "Plan preview" }
    val previewSubtitle = when (lang) { "ru" -> "Что будет создано"; "be" -> "Што будзе створана"; else -> "What will be created" }
    val previewWeeksLabel = when (lang) { "ru" -> "Период"; "be" -> "Перыяд"; else -> "Period" }
    val previewWorkoutsLabel = when (lang) { "ru" -> "Тренировки"; "be" -> "Трэніроўкі"; else -> "Workouts" }
    val previewDurationLabel = when (lang) { "ru" -> "Длительность"; "be" -> "Працягласць"; else -> "Duration" }
    val previewExercisesLabel = when (lang) { "ru" -> "Упражнения"; "be" -> "Практыкаванні"; else -> "Exercises" }
    val previewIntensityLabel = when (lang) { "ru" -> "Интенсивность"; "be" -> "Інтэнсіўнасць"; else -> "Intensity" }
    val previewMuscleLabel = when (lang) { "ru" -> "Мышцы"; "be" -> "Мышцы"; else -> "Muscle" }
    val anyMuscleValue = when (lang) { "ru" -> "Любые"; "be" -> "Любыя"; else -> "Any" }
    val readyTitle = when (lang) { "ru" -> "План готов к генерации"; "be" -> "План гатовы да генерацыі"; else -> "Plan ready" }
    val readyBody = when (lang) { "ru" -> "Проверь параметры и создай расписание тренировок."; "be" -> "Правер параметры і ствары расклад трэніровак."; else -> "Review parameters and create the workout schedule." }
    val generatingTitle = when (lang) { "ru" -> "Генерация плана"; "be" -> "Генерацыя плана"; else -> "Generating plan" }
    val generatingBody = when (lang) { "ru" -> "AI создаёт тренировки для выбранных дат."; "be" -> "AI стварае трэніроўкі для выбраных дат."; else -> "AI is creating workouts for selected dates." }
    val errorTitle = when (lang) { "ru" -> "Не удалось создать план"; "be" -> "Не атрымалася стварыць план"; else -> "Plan generation failed" }
    val generateAction = when (lang) { "ru" -> "Создать план"; "be" -> "Стварыць план"; else -> "Generate plan" }
    val generatingAction = when (lang) { "ru" -> "Создание..."; "be" -> "Стварэнне..."; else -> "Generating..." }
    val light = when (lang) { "ru" -> "Лёгкая"; "be" -> "Лёгкая"; else -> "Light" }
    val medium = when (lang) { "ru" -> "Средняя"; "be" -> "Сярэдняя"; else -> "Medium" }
    val hard = when (lang) { "ru" -> "Сложная"; "be" -> "Складаная"; else -> "Hard" }
    fun weeksValue(value: Int) = when (lang) { "ru" -> "$value нед."; "be" -> "$value тыд."; else -> "$value weeks" }
    fun exerciseCountValue(value: Int) = when (lang) { "ru" -> "$value упражнений"; "be" -> "$value практыкаванняў"; else -> "$value exercises" }
    fun durationValue(totalMinutes: Int): String = when { totalMinutes >= 60 && totalMinutes % 60 == 0 -> "${totalMinutes / 60} h"; totalMinutes >= 60 -> "${totalMinutes / 60} h ${totalMinutes % 60} min"; else -> "$totalMinutes min" }
    fun workoutsValue(value: Int) = when (lang) { "ru" -> "$value тренировок"; "be" -> "$value трэніровак"; else -> "$value workouts" }
    fun musclesCountValue(value: Int) = when (lang) { "ru" -> "$value групп"; "be" -> "$value груп"; else -> "$value groups" }
    fun musclePreviewValue(selectedBodyParts: List<String>) = when {
        selectedBodyParts.isEmpty() -> anyMuscleValue
        selectedBodyParts.size == 1 -> selectedBodyParts.first()
        else -> musclesCountValue(selectedBodyParts.size)
    }
    fun plannedReason(duration: Int, count: Int, intensity: String) = when (lang) { "ru" -> "План • ${durationValue(duration)} • ${exerciseCountValue(count)} • $intensity"; "be" -> "План • ${durationValue(duration)} • ${exerciseCountValue(count)} • $intensity"; else -> "Plan • ${durationValue(duration)} • ${exerciseCountValue(count)} • $intensity" }
    fun dayShort(day: DayOfWeek) = when (lang) { "ru" -> when (day) { DayOfWeek.MONDAY -> "Пн"; DayOfWeek.TUESDAY -> "Вт"; DayOfWeek.WEDNESDAY -> "Ср"; DayOfWeek.THURSDAY -> "Чт"; DayOfWeek.FRIDAY -> "Пт"; DayOfWeek.SATURDAY -> "Сб"; DayOfWeek.SUNDAY -> "Вс" }; "be" -> when (day) { DayOfWeek.MONDAY -> "Пн"; DayOfWeek.TUESDAY -> "Аў"; DayOfWeek.WEDNESDAY -> "Ср"; DayOfWeek.THURSDAY -> "Чц"; DayOfWeek.FRIDAY -> "Пт"; DayOfWeek.SATURDAY -> "Сб"; DayOfWeek.SUNDAY -> "Нд" }; else -> day.name.take(3).lowercase().replaceFirstChar { it.uppercase() } }
    fun modelPrompt(userPrompt: String, weeks: Int, days: Set<DayOfWeek>, duration: Int, count: Int, intensity: String): String {
        val daysText = days.sortedBy { it.value }.joinToString { it.name }
        val base = when (lang) { "ru" -> "Создай долгосрочный план тренировок на $weeks недель. Дни: $daysText. Длительность каждой тренировки: ${durationValue(duration)}. Упражнений в тренировке: $count. Интенсивность: $intensity."; "be" -> "Ствары доўгатэрміновы план трэніровак на $weeks тыдняў. Дні: $daysText. Працягласць кожнай трэніроўкі: ${durationValue(duration)}. Практыкаванняў у трэніроўцы: $count. Інтэнсіўнасць: $intensity."; else -> "Create a long-term workout plan for $weeks weeks. Days: $daysText. Workout duration: ${durationValue(duration)}. Exercises per workout: $count. Intensity: $intensity." }
        return if (userPrompt.isBlank()) base else "$userPrompt\n$base"
    }
}
