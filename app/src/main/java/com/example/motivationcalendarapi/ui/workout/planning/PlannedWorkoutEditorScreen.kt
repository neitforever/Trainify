package com.example.motivationcalendarapi.ui.workout.planning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Surface
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.DrawerState
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.workout.fragments.ExerciseSelectionBottomSheet
import com.example.motivationcalendarapi.ui.workout.fragments.WorkoutNameTextField
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.motivationcalendar.ui.ExerciseCard
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannedWorkoutEditorScreen(
    dateMillis: Long,
    plannedWorkoutId: String?,
    workoutViewModel: WorkoutViewModel,
    exerciseViewModel: ExerciseViewModel,
    navController: NavController,
    drawerState: MutableState<DrawerState>,
    lang: String
) {
    val selectedExercises by workoutViewModel.selectedExercises.collectAsState()
    val exerciseSetsMap by workoutViewModel.exerciseSetsMap.collectAsState()
    val workoutName by workoutViewModel.workoutName.collectAsState()
    val plannedWorkouts by workoutViewModel.plannedWorkouts.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isSheetOpen = remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val date = remember(dateMillis) { Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate() }
    val realPlanId = plannedWorkoutId?.takeIf { it != "new" }

    LaunchedEffect(realPlanId, plannedWorkouts) {
        val plan = realPlanId?.let { id -> plannedWorkouts.firstOrNull { it.id == id } }
        if (plan != null) workoutViewModel.loadPlannedWorkoutForEditor(plan)
        else workoutViewModel.prepareManualPlannedWorkoutDraft()
    }

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
                    Box(modifier = Modifier.fillMaxWidth().padding(start = 4.dp)) {
                        Text(
                            text = when (lang) { "ru" -> "План тренировки"; "be" -> "План трэніроўкі"; else -> "Workout plan" },
                            style = MaterialTheme.typography.headlineLarge,
                            maxLines = 1,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                },
                modifier = Modifier.border(
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                    shape = CutCornerShape(4.dp)
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .wrapContentSize(Alignment.BottomEnd)
                    .pointerInput(Unit) { detectTapGestures { isMenuExpanded = false } }
            ) {
                Row {
                    AnimatedVisibility(
                        visible = isMenuExpanded,
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                workoutViewModel.resetWorkout()
                                navController.popBackStack()
                            },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(painterResource(R.drawable.ic_delete), contentDescription = null, modifier = Modifier.size(34.dp))
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = isMenuExpanded,
                        enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                workoutViewModel.saveDraftAsPlannedWorkout(date, realPlanId, lang)
                                navController.popBackStack()
                            },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(painterResource(R.drawable.ic_save), contentDescription = null, modifier = Modifier.size(34.dp))
                        }
                    }
                    FloatingActionButton(
                        onClick = { isMenuExpanded = !isMenuExpanded },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            painter = painterResource(if (isMenuExpanded) R.drawable.ic_close else R.drawable.ic_menu),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) { detectTapGestures { isMenuExpanded = false } }
        ) {
            Spacer(Modifier.height(8.dp))
            WorkoutNameTextField(
                workoutName = workoutName,
                onValueChange = { if (it.length <= 20) workoutViewModel.setWorkoutName(it) },
                keyboardController = keyboardController
            )
            Spacer(Modifier.height(10.dp))
            ExerciseSelectionBottomSheet(
                isSheetOpen = isSheetOpen,
                sheetState = sheetState,
                exerciseViewModel = exerciseViewModel,
                workoutViewModel = workoutViewModel,
                navController = navController,
                lang = lang
            )

            if (selectedExercises.isEmpty()) {
                EmptyWorkoutPlanCard(lang = lang, modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp))
            }

            selectedExercises.forEachIndexed { index, exercise ->
                val currentGroupId = exercise.supersetGroupId
                val isSupersetFirst = currentGroupId == null || index == 0 || selectedExercises[index - 1].supersetGroupId != currentGroupId
                val isSupersetLast = currentGroupId == null || index == selectedExercises.lastIndex || selectedExercises[index + 1].supersetGroupId != currentGroupId
                val blockStart = if (currentGroupId == null) index else selectedExercises.indexOfFirst { it.supersetGroupId == currentGroupId }
                val blockEnd = if (currentGroupId == null) index else selectedExercises.indexOfLast { it.supersetGroupId == currentGroupId }
                val supersetNumber = currentGroupId?.let { groupId -> selectedExercises.take(index + 1).mapNotNull { it.supersetGroupId }.distinct().indexOf(groupId) + 1 }

                ExerciseCard(
                    index = index,
                    exercise = exercise,
                    exerciseSets = exerciseSetsMap[index] ?: emptyList(),
                    onAddSetClick = { workoutViewModel.addExerciseSet(it) },
                    onRepClick = { _, _ -> },
                    onWeightClick = { _, _ -> },
                    onMoveUp = { workoutViewModel.moveExerciseUp(index) },
                    onMoveDown = { workoutViewModel.moveExerciseDown(index) },
                    onTimeClick = { _, _ -> },
                    onResistanceClick = { _, _ -> },
                    onInclineClick = { _, _ -> },
                    canMoveUp = blockStart > 0,
                    canMoveDown = blockEnd < selectedExercises.lastIndex,
                    workoutViewModel = workoutViewModel,
                    navController = navController,
                    lang = lang,
                    supersetLabel = supersetNumber?.let { stringResource(R.string.superset_number_format, it) },
                    isSupersetFirst = isSupersetFirst,
                    isSupersetLast = isSupersetLast,
                    supersetBlockStartIndex = blockStart,
                    supersetBlockEndIndex = blockEnd,
                    onDeleteExercise = { workoutViewModel.removeExercise(index) },
                    onDeleteSetClick = { exerciseIndex, setIndex -> workoutViewModel.removeExerciseSet(exerciseIndex, setIndex) },
                    showMaxSetMenu = false
                )
            }
            WorkoutPlanContentCard(
                hasExercises = selectedExercises.isNotEmpty(),
                lang = lang,
                onClick = { isSheetOpen.value = true },
                modifier = Modifier.padding(top = 10.dp, start = 2.dp, end = 2.dp)
            )
            Spacer(Modifier.height(96.dp))
        }
    }
}

@Composable
private fun EmptyWorkoutPlanCard(lang: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_dumbbell), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            }
            Text(
                text = when (lang) { "ru" -> "План пока пуст"; "be" -> "План пакуль пусты"; else -> "Plan is empty" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = when (lang) { "ru" -> "Собери будущую тренировку из упражнений."; "be" -> "Збяры будучую трэніроўку з практыкаванняў."; else -> "Build a future workout from exercises." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun WorkoutPlanContentCard(
    hasExercises: Boolean,
    lang: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_dumbbell), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = when (lang) { "ru" -> if (hasExercises) "Дополнить план" else "Собрать план"; "be" -> if (hasExercises) "Дапоўніць план" else "Сабраць план"; else -> if (hasExercises) "Extend plan" else "Build plan" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (lang) { "ru" -> if (hasExercises) "Добавьте ещё упражнения" else "Выберите упражнения"; "be" -> if (hasExercises) "Дадайце яшчэ практыкаванні" else "Выберыце практыкаванні"; else -> if (hasExercises) "Add more exercises" else "Choose exercises" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
