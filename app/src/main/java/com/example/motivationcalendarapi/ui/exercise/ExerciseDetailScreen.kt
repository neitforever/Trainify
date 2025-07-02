package com.example.motivationcalendarapi.ui.exercise

import LoadingView
import Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.ui.dialogs.DeleteExerciseDialog
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.model.getIconForEquipment
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exerciseId: String,
    viewModel: ExerciseViewModel = viewModel(),
) {
    val showDeleteDialog = remember { mutableStateOf(false) }
    val selectedExercise = remember { mutableStateOf<Exercise?>(null) }
    val favoriteExercises by viewModel.getFavoriteExercises().collectAsState(initial = emptyList())
    LaunchedEffect(exerciseId) {
        coroutineScope {
            launch(Dispatchers.IO) {
                selectedExercise.value = viewModel.getExerciseById(exerciseId)
                selectedExercise.value?.let { exercise ->
                }
            }
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ), navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }, title = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp)
                ) {
                    Text(
                        text = Screen.ExerciseDetailView.title,
                        style = MaterialTheme.typography.displaySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }, actions = {
                selectedExercise.value?.let { exercise ->
                    val isFavorite = favoriteExercises.any { it.id == exercise.id }
                    IconButton(onClick = { viewModel.toggleFavorite(exercise) }) {
                        Icon(
                            painter = painterResource(
                                id = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
                            ),
                            contentDescription = stringResource(R.string.favorite),
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }, modifier = Modifier.border(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                shape = CutCornerShape(4.dp)
            )
            )
        },
        floatingActionButton = {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.navigationBarsPadding()
            ) {
                FloatingActionButton(
                    onClick = { showDeleteDialog.value = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.delete_exercise),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        selectedExercise.value?.let { exercise ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("${Screen.EditExerciseName.route}/${exercise.id}") }
                    .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = exercise.name.replaceFirstChar { it.uppercase() },
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(start = 8.dp)
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 0.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                )

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, bottom = 8.dp, top = 16.dp, end = 8.dp)
                    .clickable { navController.navigate("${Screen.EquipmentSelection.route}/${exercise.id}") }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = stringResource(R.string.equipment),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 8.dp).weight(1f)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(start = 8.dp)
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ), modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                ), verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = getIconForEquipment(exercise.equipment)),
                                contentDescription = stringResource(R.string.equipment),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = exercise.equipment,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }







                HorizontalDivider(
                    modifier = Modifier.padding(start = 0.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                )


                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, bottom = 8.dp, top = 16.dp, end = 8.dp)
                    .clickable { navController.navigate("${Screen.BodyPartSelection.route}/${exercise.id}") }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = stringResource(R.string.body_part),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .weight(1f)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(start = 8.dp)
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ), modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                ), verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = getIconForBodyPart(exercise.bodyPart)),
                                contentDescription = stringResource(R.string.body_part),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = exercise.bodyPart,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
//                    HorizontalDivider(
//                        modifier = Modifier.padding(start = 0.dp), thickness = 2.dp,
//                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
//                    )
//
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(start = 8.dp, bottom = 8.dp, top = 16.dp, end = 8.dp)
//                            .clickable { navController.navigate("${Screen.SecondaryMusclesSelection.route}/${exercise.id}") }
//                    ) {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = "Secondary Muscles",
//                                style = MaterialTheme.typography.headlineSmall,
//                                color = MaterialTheme.colorScheme.onSurface,
//                                modifier = Modifier.padding(end = 8.dp).weight(1f),
//                            )
//                            Icon(
//                                painter = painterResource(id = R.drawable.ic_edit),
//                                contentDescription = "Edit",
//                                tint = MaterialTheme.colorScheme.tertiary,
//                                modifier = Modifier
//                                    .size(28.dp)
//                                    .padding(start = 8.dp)
//                            )
//                        }
//
//                        FlowRow(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 8.dp),
//                            horizontalArrangement = Arrangement.spacedBy(8.dp),
//                            verticalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            if (exercise.secondaryMuscles.isEmpty()) {
//                                Text(
//                                    text = "Not set",
//                                    style = MaterialTheme.typography.titleMedium,
//                                    color = MaterialTheme.colorScheme.outline,
//                                    modifier = Modifier.padding(top = 8.dp)
//                                )
//                            } else {
//                                exercise.secondaryMuscles
//                                    .flatMap { muscleEntry ->
//                                        muscleEntry
//                                            .replace("[", "")
//                                            .replace("]", "")
//                                            .replace("\"", "")
//                                            .split(",")
//                                            .map { it.trim() }
//                                    }
//                                    .filter { it.isNotBlank() }
//                                    .forEach { muscle ->
//                                        Card(
//                                            shape = RoundedCornerShape(8.dp),
//                                            colors = CardDefaults.cardColors(
//                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
//                                            ),
//                                            modifier = Modifier.wrapContentWidth()
//                                        ) {
//                                            Text(
//                                                text = muscle,
//                                                style = MaterialTheme.typography.titleMedium,
//                                                color = MaterialTheme.colorScheme.primary,
//                                                maxLines = 1,
//                                                overflow = TextOverflow.Ellipsis,
//                                                modifier = Modifier
//                                                    .padding(horizontal = 12.dp, vertical = 8.dp)
//                                            )
//                                        }
//                                    }
//                            }
//                        }
//                    }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 0.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                )
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, bottom = 8.dp, top = 16.dp, end = 8.dp)
                    .clickable { navController.navigate("${Screen.EditExerciseInstructions.route}/${exercise.id}") }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.instructions),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .weight(1f),
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(start = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    exercise.instructions.forEachIndexed { index, instruction ->
                        Text(
                            text = "${index + 1}. $instruction",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                    }
                }

                    Spacer(
                        modifier = Modifier
                            .absolutePadding(bottom = 200.dp)
                    )

            }
        } ?: LoadingView()
    }

    DeleteExerciseDialog (
        showDialog = showDeleteDialog.value,
        onDismiss = { showDeleteDialog.value = false },
        onConfirm = {
            showDeleteDialog.value = false
            selectedExercise.value?.let {
                viewModel.deleteExercise(it.id)
                navController.popBackStack()
            }
        }
    )

}
