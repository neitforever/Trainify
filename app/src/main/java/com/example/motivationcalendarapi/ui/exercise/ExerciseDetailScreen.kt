package com.example.motivationcalendarapi.ui.exercise

import LoadingView
import Screen
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.getCardType
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.model.getIconForEquipment
import com.example.motivationcalendarapi.ui.dialogs.DeleteExerciseDialog
import com.example.motivationcalendarapi.ui.exercise.analysis.ExerciseAnalysisSection
import com.example.motivationcalendarapi.ui.exercise.detail.ExerciseDetailFabMenu
import com.example.motivationcalendarapi.ui.exercise.detail.ExerciseInstructionsCard
import com.example.motivationcalendarapi.ui.exercise.detail.ExerciseMetricCard
import com.example.motivationcalendarapi.ui.exercise.detail.ExerciseTitleCard
import com.example.motivationcalendarapi.ui.exercise.detail.SimilarExercisesSection
import com.example.motivationcalendarapi.ui.exercise.detail.findSimilarExercises
import com.example.motivationcalendarapi.ui.exercise.detail.getTechniqueSearchName
import com.example.motivationcalendarapi.ui.exercise.technique.ExerciseTechniqueBottomSheet
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.analysis.ExerciseAnalysisViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exerciseId: String,
    viewModel: ExerciseViewModel,
    analysisViewModel: ExerciseAnalysisViewModel,
    drawerState: MutableState<DrawerState>,
    lang: String,
    currentLocale: Locale,
    context: Context
) {
    val showDeleteDialog = remember { mutableStateOf(false) }
    val selectedExercise by viewModel.getExerciseByIdFlow(exerciseId).collectAsState(initial = null)
    val favoriteExercises by viewModel.getFavoriteExercises().collectAsState(initial = emptyList())
    val allExercises by viewModel.getAllExercises().collectAsState(initial = emptyList())
    val techniqueVideosState by viewModel.techniqueVideosUiState.collectAsState()
    val selectedTechniqueVideo by viewModel.selectedTechniqueVideo.collectAsState()

    var showTechniqueSheet by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    val topBarScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = 32.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            topBarScope.launch { drawerState.value.open() }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu),
                            contentDescription = stringResource(R.string.menu),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp)
                    ) {
                        Text(
                            text = Screen.ExerciseDetailView.getTitle(context),
                            style = MaterialTheme.typography.displaySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    selectedExercise?.let { exercise ->
                        val isFavorite = favoriteExercises.any { it.id == exercise.id }
                        IconButton(
                            onClick = {
                                val updatedExercise = exercise.copy(favorite = isFavorite)
                                viewModel.toggleFavorite(updatedExercise)
                            }
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isFavorite) R.drawable.ic_favorite_filled
                                    else R.drawable.ic_favorite_border
                                ),
                                contentDescription = stringResource(R.string.favorite),
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                modifier = Modifier.border(
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                    shape = CutCornerShape(4.dp)
                )
            )
        },
        floatingActionButton = {
            ExerciseDetailFabMenu(
                isExpanded = isMenuExpanded,
                onToggle = { isMenuExpanded = !isMenuExpanded },
                onOpenTechnique = {
                    selectedExercise?.let { exercise ->
                        showTechniqueSheet = true
                        isMenuExpanded = false
                        viewModel.loadTechniqueVideos(
                            exerciseId = exercise.id,
                            exerciseName = exercise.getTechniqueSearchName(lang),
                            lang = lang
                        )
                    }
                },
                onAnalyzeTechnique = {
                    isMenuExpanded = false
                    Toast.makeText(
                        context,
                        context.getString(R.string.technique_analysis_placeholder),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onDelete = {
                    isMenuExpanded = false
                    showDeleteDialog.value = true
                }
            )
        }
    ) { paddingValues ->
        selectedExercise?.let { exercise ->
            val similarExercises = remember(allExercises, exercise.id, lang) {
                findSimilarExercises(
                    currentExercise = exercise,
                    allExercises = allExercises,
                    lang = lang
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .pointerInput(Unit) {
                        detectTapGestures { isMenuExpanded = false }
                    }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExerciseTitleCard(
                    exercise = exercise,
                    lang = lang,
                    onClick = {
                        navController.navigate("${Screen.EditExerciseName.route}/${exercise.id}")
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExerciseMetricCard(
                        title = stringResource(R.string.equipment),
                        primaryValue = exercise.getEquipment(lang),
                        secondaryValue = "",
                        iconResId = getIconForEquipment(exercise.equipmentLocalized),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            navController.navigate("${Screen.EquipmentSelection.route}/${exercise.id}")
                        }
                    )

                    ExerciseMetricCard(
                        title = stringResource(R.string.muscle_groups),
                        primaryValue = exercise.getBodyPart(lang),
                        secondaryValue = "",
                        iconResId = getIconForBodyPart(exercise.getBodyPart(lang)),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            navController.navigate("${Screen.BodyPartSelection.route}/${exercise.id}")
                        }
                    )
                }

                ExerciseInstructionsCard(
                    instructions = exercise.getInstructions(lang),
                    onClick = {
                        navController.navigate("${Screen.EditExerciseInstructions.route}/${exercise.id}")
                    }
                )

                SimilarExercisesSection(
                    exercises = similarExercises,
                    lang = lang,
                    onExerciseClick = { similarExercise ->
                        navController.navigate("${Screen.ExerciseDetailView.route}/${similarExercise.id}")
                    }
                )

                ExerciseAnalysisSection(
                    exerciseId = exercise.id,
                    cardType = exercise.getCardType(),
                    lang = lang,
                    currentLocale = currentLocale,
                    viewModel = analysisViewModel
                )

                Spacer(modifier = Modifier.absolutePadding(bottom = 200.dp))
            }
        } ?: LoadingView()
    }

    if (showTechniqueSheet) {
        selectedExercise?.let { exercise ->
            ExerciseTechniqueBottomSheet(
                exerciseName = exercise.getName(lang),
                videosState = techniqueVideosState,
                bodyPartIconResId = getIconForBodyPart(exercise.getBodyPart(lang)),
                selectedVideo = selectedTechniqueVideo,
                onVideoClick = { video -> viewModel.selectTechniqueVideo(video) },
                onRefreshClick = {
                    viewModel.loadTechniqueVideos(
                        exerciseId = exercise.id,
                        exerciseName = exercise.getTechniqueSearchName(lang),
                        lang = lang,
                        forceRefresh = true
                    )
                },
                onDismiss = {
                    showTechniqueSheet = false
                    viewModel.clearTechniqueVideosState()
                }
            )
        }
    }

    DeleteExerciseDialog(
        showDialog = showDeleteDialog.value,
        onDismiss = { showDeleteDialog.value = false },
        onConfirm = {
            showDeleteDialog.value = false
            selectedExercise?.let {
                viewModel.deleteExercise(it.id)
                navController.popBackStack()
            }
        }
    )
}
