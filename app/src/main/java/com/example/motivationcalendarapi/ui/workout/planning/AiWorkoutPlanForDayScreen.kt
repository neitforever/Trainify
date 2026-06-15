package com.example.motivationcalendarapi.ui.workout.planning

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.dialogs.TimeMetricDialog
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.example.motivationcalendarapi.viewmodel.AiWorkoutPlanForDayViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

private fun Modifier.aiWorkoutNoRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    clickable(
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiWorkoutPlanForDayScreen(
    dateMillis: Long,
    workoutViewModel: WorkoutViewModel,
    exerciseViewModel: ExerciseViewModel,
    aiWorkoutPlanForDayViewModel: AiWorkoutPlanForDayViewModel,
    navController: NavController,
    drawerState: MutableState<DrawerState>,
    lang: String
) {
    val date = remember(dateMillis) { Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate() }
    val text = remember(lang) { AiWorkoutPlanForDayText(lang) }
    val scope = rememberCoroutineScope()
    val allExercises by exerciseViewModel.getAllExercises().collectAsState(initial = emptyList())
    val state by aiWorkoutPlanForDayViewModel.uiState.collectAsState()
    var showDurationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(text.fullBody, text.medium) {
        aiWorkoutPlanForDayViewModel.ensureDefaults(
            defaultFocus = text.fullBody,
            defaultIntensity = text.medium
        )
    }

    val inputEnabled = !state.isGenerating
    val durationMinutes = state.customHours * 60 + state.customMinutes
    val durationLabel = text.durationValue(durationMinutes)
    val focus = state.focus.ifBlank { text.fullBody }
    val intensity = state.intensity.ifBlank { text.medium }

    TimeMetricDialog(
        showDialog = showDurationDialog,
        title = text.customDurationTitle,
        initialValueMinutes = durationMinutes.toFloat(),
        minValueMinutes = 1f,
        maxValueMinutes = 300f,
        stepValueMinutes = 1f,
        onDismiss = { showDurationDialog = false },
        onSave = { minutes ->
            val total = minutes.toInt().coerceIn(1, 300)
            aiWorkoutPlanForDayViewModel.setDuration(total)
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
                    IconButton(
                        onClick = { scope.launch { drawerState.value.open() } },
                        modifier = Modifier.size(48.dp)
                    ) {
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
                modifier = Modifier.borderLikeTopBar()
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(2.dp)) }
                item { AiDayHelpCard(text = text) }
                item {
                    OutlinedTextField(
                        value = state.prompt,
                        onValueChange = aiWorkoutPlanForDayViewModel::setPrompt,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        label = { Text(text.promptLabel) },
                        placeholder = { Text(text.promptPlaceholder) },
                        shape = RoundedCornerShape(12.dp),
                        enabled = inputEnabled
                    )
                }
                item { FocusSelectionCard(text = text, selected = focus, enabled = inputEnabled, onSelect = aiWorkoutPlanForDayViewModel::setFocus) }
                item { DurationCard(text = text, value = durationLabel, enabled = inputEnabled, onClick = { showDurationDialog = true }) }
                item {
                    ExerciseCountCard(
                        text = text,
                        minExercises = state.minExercises,
                        maxExercises = state.maxExercises,
                        enabled = inputEnabled,
                        onValueChange = aiWorkoutPlanForDayViewModel::setExerciseRange
                    )
                }
                item { IntensitySelectionCard(text = text, selected = intensity, enabled = inputEnabled, onSelect = aiWorkoutPlanForDayViewModel::setIntensity) }
                item {
                    AiWorkoutGenerationStateCard(
                        text = text,
                        isGenerating = state.isGenerating,
                        error = state.generationError
                    )
                }
                item { Spacer(Modifier.height(120.dp)) }
            }

            FloatingActionButton(
                onClick = {
                    if (state.isGenerating) return@FloatingActionButton
                    aiWorkoutPlanForDayViewModel.setGenerating(true)
                    val modelPrompt = text.modelPrompt(
                        userPrompt = state.prompt,
                        focus = focus,
                        duration = durationLabel,
                        intensity = intensity
                    )
                    workoutViewModel.createAiGeneratedWorkoutForDate(
                        date = date,
                        prompt = modelPrompt,
                        selectedBodyParts = if (focus == text.fullBody || focus == text.recovery) emptyList() else listOf(focus),
                        selectedEquipment = emptyList(),
                        difficulty = intensity,
                        minExercises = state.minExercises,
                        maxExercises = state.maxExercises,
                        durationMinutes = durationMinutes,
                        lang = lang,
                        localExercises = allExercises,
                        aiReason = text.plannedCardReason(
                            focus = focus,
                            duration = durationLabel,
                            intensity = intensity
                        ),
                        onComplete = {
                            aiWorkoutPlanForDayViewModel.resetForm(
                                defaultFocus = text.fullBody,
                                defaultIntensity = text.medium
                            )
                            navController.popBackStack()
                        },
                        onError = { error ->
                            aiWorkoutPlanForDayViewModel.setGenerationError(error)
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 16.dp)
                    .size(64.dp)
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_circle),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.borderLikeTopBar(): Modifier = border(
    border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
    shape = CutCornerShape(4.dp)
)

@Composable
private fun AiDayHelpCard(text: AiWorkoutPlanForDayText) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(modifier = Modifier.size(46.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_reward_fg_ai_exercise), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
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
private fun FocusSelectionCard(text: AiWorkoutPlanForDayText, selected: String, enabled: Boolean, onSelect: (String) -> Unit) {
    AiDaySectionCard(title = text.focusTitle, subtitle = text.focusSubtitle) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FocusOption(text.fullBody, R.drawable.ic_body_waist, selected == text.fullBody, enabled, { onSelect(text.fullBody) }, Modifier.weight(1f))
            FocusOption(text.upperBody, R.drawable.ic_body_chest, selected == text.upperBody, enabled, { onSelect(text.upperBody) }, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FocusOption(text.lowerBody, R.drawable.ic_body_upper_legs, selected == text.lowerBody, enabled, { onSelect(text.lowerBody) }, Modifier.weight(1f))
            FocusOption(text.recovery, R.drawable.ic_heart_pulse, selected == text.recovery, enabled, { onSelect(text.recovery) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FocusOption(title: String, icon: Int, selected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(74.dp).aiWorkoutNoRippleClickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.36f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(painterResource(icon), contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun DurationCard(text: AiWorkoutPlanForDayText, value: String, enabled: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().aiWorkoutNoRippleClickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_time), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = text.durationTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = text.tapToChangeDuration, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseCountCard(
    text: AiWorkoutPlanForDayText,
    minExercises: Int,
    maxExercises: Int,
    enabled: Boolean,
    onValueChange: (Int, Int) -> Unit
) {
    AiDaySectionCard(title = text.exerciseCountTitle, subtitle = text.exerciseCountSubtitle) {
        Text(
            text = text.exerciseCountValue(minExercises, maxExercises),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        RangeSlider(
            value = minExercises.toFloat()..maxExercises.toFloat(),
            enabled = enabled,
            onValueChange = { range ->
                val safeStart = range.start.toInt().coerceIn(1, 15)
                val safeEnd = range.endInclusive.toInt().coerceIn(safeStart, 15)
                onValueChange(safeStart, safeEnd)
            },
            valueRange = 1f..15f,
            steps = 0,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                thumbColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun IntensitySelectionCard(text: AiWorkoutPlanForDayText, selected: String, enabled: Boolean, onSelect: (String) -> Unit) {
    AiDaySectionCard(title = text.intensityTitle, subtitle = text.intensitySubtitle) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IntensityOption(text.light, R.drawable.ic_smile_easy, selected == text.light, enabled, { onSelect(text.light) }, Modifier.weight(1f))
            IntensityOption(text.medium, R.drawable.ic_smile_normal, selected == text.medium, enabled, { onSelect(text.medium) }, Modifier.weight(1f))
            IntensityOption(text.hard, R.drawable.ic_smile_hard, selected == text.hard, enabled, { onSelect(text.hard) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun IntensityOption(title: String, icon: Int, selected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val iconColor = when (icon) {
        R.drawable.ic_smile_easy -> EASY_COLOR
        R.drawable.ic_smile_normal -> NORMAL_COLOR
        R.drawable.ic_smile_hard -> HARD_COLOR
        else -> MaterialTheme.colorScheme.primary
    }
    Card(
        modifier = modifier.aiWorkoutNoRippleClickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) iconColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) iconColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(painter = painterResource(id = icon), contentDescription = title, tint = iconColor, modifier = Modifier.size(30.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = if (selected) iconColor else MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AiWorkoutGenerationStateCard(text: AiWorkoutPlanForDayText, isGenerating: Boolean, error: String?) {
    val isError = error != null
    val icon = when {
        isGenerating -> R.drawable.ic_reward_fg_ai_exercise
        isError -> R.drawable.ic_close
        else -> R.drawable.ic_reward_fg_ai_exercise
    }
    val tint = when {
        isError -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        border = BorderStroke(1.dp, if (isError) tint.copy(alpha = 0.24f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.30f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
                    border = BorderStroke(1.dp, tint.copy(alpha = 0.12f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        } else {
                            Icon(painterResource(icon), contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = when {
                            isGenerating -> text.generatingTitle
                            isError -> text.generationErrorTitle
                            else -> text.generationControlTitle
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isError) tint else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = error ?: if (isGenerating) text.generatingBody else text.generationControlBody,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AiDaySectionCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
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

private data class AiWorkoutPlanForDayText(val lang: String) {
    val title = when (lang) { "ru" -> "AI-тренировка"; "be" -> "AI-трэніроўка"; else -> "AI workout" }
    val helpTitle = when (lang) { "ru" -> "Опиши нужную тренировку"; "be" -> "Апішы патрэбную трэніроўку"; else -> "Describe the workout" }
    val helpBody = when (lang) { "ru" -> "Модель подберёт тренировку с учётом истории, нагрузки и выбранных параметров."; "be" -> "Мадэль падбярэ трэніроўку з улікам гісторыі, нагрузкі і параметраў."; else -> "The model will use history, workload and your settings." }
    val generatingTitle = when (lang) { "ru" -> "Генерация тренировки"; "be" -> "Генерацыя трэніроўкі"; else -> "Generating workout" }
    val generatingBody = when (lang) { "ru" -> "AI подбирает упражнения, длительность и нагрузку для выбранного дня."; "be" -> "AI падбірае практыкаванні, працягласць і нагрузку для выбранага дня."; else -> "AI is selecting exercises, duration and load for the selected day." }
    val generationErrorTitle = when (lang) { "ru" -> "Не удалось сгенерировать"; "be" -> "Не атрымалася згенераваць"; else -> "Generation failed" }
    val generationControlTitle = when (lang) { "ru" -> "Параметры подготовлены"; "be" -> "Параметры падрыхтаваны"; else -> "Parameters prepared" }
    val generationControlBody = when (lang) { "ru" -> "Нажми кнопку генерации внизу справа. Во время запроса экран заблокирует повторный запуск."; "be" -> "Націсні кнопку генерацыі ўнізе справа. Падчас запыту экран заблакіруе паўторны запуск."; else -> "Press the generate button in the bottom right. While the request is running, repeat generation is blocked." }
    val promptLabel = when (lang) { "ru" -> "Что ты хочешь получить?"; "be" -> "Што ты хочаш атрымаць?"; else -> "What do you want?" }
    val promptPlaceholder = when (lang) { "ru" -> "Например: лёгкая тренировка на верх тела, без ног, около 45 минут"; "be" -> "Напрыклад: лёгкая трэніроўка на верх цела, без ног, каля 45 хвілін"; else -> "Example: light upper-body workout, no legs, about 45 minutes" }
    val focusTitle = when (lang) { "ru" -> "Фокус"; "be" -> "Фокус"; else -> "Focus" }
    val focusSubtitle = when (lang) { "ru" -> "Выбери основное направление тренировки"; "be" -> "Выберы асноўны кірунак трэніроўкі"; else -> "Choose the main workout focus" }
    val durationTitle = when (lang) { "ru" -> "Длительность"; "be" -> "Працягласць"; else -> "Duration" }
    val tapToChangeDuration = when (lang) { "ru" -> "Нажми, чтобы изменить"; "be" -> "Націсні, каб змяніць"; else -> "Tap to change" }
    val exerciseCountTitle = when (lang) { "ru" -> "Количество упражнений"; "be" -> "Колькасць практыкаванняў"; else -> "Exercise count" }
    val exerciseCountSubtitle = when (lang) { "ru" -> "Диапазон упражнений для будущей тренировки"; "be" -> "Дыяпазон практыкаванняў для будучай трэніроўкі"; else -> "Exercise range for the future workout" }
    val intensityTitle = when (lang) { "ru" -> "Интенсивность"; "be" -> "Інтэнсіўнасць"; else -> "Intensity" }
    val intensitySubtitle = when (lang) { "ru" -> "Уровень нагрузки для будущей тренировки"; "be" -> "Узровень нагрузкі для будучай трэніроўкі"; else -> "Target load for the future workout" }
    val fullBody = when (lang) { "ru" -> "Всё тело"; "be" -> "Усё цела"; else -> "Full body" }
    val upperBody = when (lang) { "ru" -> "Верх"; "be" -> "Верх"; else -> "Upper" }
    val lowerBody = when (lang) { "ru" -> "Низ"; "be" -> "Ніз"; else -> "Lower" }
    val recovery = when (lang) { "ru" -> "Восстановление"; "be" -> "Аднаўленне"; else -> "Recovery" }
    val customDurationTitle = when (lang) { "ru" -> "Длительность"; "be" -> "Працягласць"; else -> "Duration" }
    val light = when (lang) { "ru" -> "Лёгкая"; "be" -> "Лёгкая"; else -> "Light" }
    val medium = when (lang) { "ru" -> "Средняя"; "be" -> "Сярэдняя"; else -> "Medium" }
    val hard = when (lang) { "ru" -> "Сложная"; "be" -> "Складаная"; else -> "Hard" }
    fun plannedCardReason(focus: String, duration: String, intensity: String): String = when (lang) {
        "ru" -> "$focus • $duration • $intensity"
        "be" -> "$focus • $duration • $intensity"
        else -> "$focus • $duration • $intensity"
    }

    fun modelPrompt(userPrompt: String, focus: String, duration: String, intensity: String): String {
        val mainRequest = userPrompt.trim()
        val base = when (lang) {
            "ru" -> "Сгенерируй тренировку на выбранный день. Фокус: $focus. Длительность: $duration. Интенсивность: $intensity."
            "be" -> "Згенеруй трэніроўку на выбраны дзень. Фокус: $focus. Працягласць: $duration. Інтэнсіўнасць: $intensity."
            else -> "Generate a workout for the selected day. Focus: $focus. Duration: $duration. Intensity: $intensity."
        }
        return if (mainRequest.isBlank()) base else "$mainRequest\n$base"
    }

    fun durationValue(totalMinutes: Int): String = when {
        totalMinutes >= 60 && totalMinutes % 60 == 0 -> "${totalMinutes / 60} h"
        totalMinutes >= 60 -> "${totalMinutes / 60} h ${totalMinutes % 60} min"
        else -> "$totalMinutes min"
    }
    fun exerciseCountValue(min: Int, max: Int) = when (lang) {
        "ru" -> "$min–$max упражнений"
        "be" -> "$min–$max практыкаванняў"
        else -> "$min–$max exercises"
    }
}
