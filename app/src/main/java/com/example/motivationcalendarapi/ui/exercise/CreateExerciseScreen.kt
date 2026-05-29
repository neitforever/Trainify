package com.example.motivationcalendarapi.ui.exercise

import LoadingView
import Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.model.getIconForEquipment
import com.example.motivationcalendarapi.ui.dialogs.ErrorDialog
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExerciseScreen(
    navController: NavController,
    exerciseViewModel: ExerciseViewModel,
    lang: String
) {
    val tempExercise by exerciseViewModel.tempExercise.collectAsState()
    val showErrorDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (tempExercise == null) {
            exerciseViewModel.initializeNewExercise(UUID.randomUUID().toString())
        }
    }

    ErrorDialog(
        showError = showErrorDialog.value,
        onDismiss = { showErrorDialog.value = false }
    )

    fun isExerciseValid(exercise: Exercise?): Boolean {
        if (exercise == null) return false

        val hasName =
            exercise.nameLocalized["en"]?.isNotBlank() == true &&
                    exercise.nameLocalized["ru"]?.isNotBlank() == true &&
                    exercise.nameLocalized["be"]?.isNotBlank() == true

        val hasBodyPart = exercise.bodyPartLocalized.values.any { it.isNotBlank() }
        val hasEquipment = exercise.equipmentLocalized.values.any { it.isNotBlank() }

        val hasInstructions =
            exercise.instructionsLocalized["en"]?.any { it.isNotBlank() } == true &&
                    exercise.instructionsLocalized["ru"]?.any { it.isNotBlank() } == true &&
                    exercise.instructionsLocalized["be"]?.any { it.isNotBlank() } == true

        return hasName && hasBodyPart && hasEquipment && hasInstructions
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = 20.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        exerciseViewModel.clearTempExercise()
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onBackground
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
                            text = Screen.CreateExercise.getTitle(context),
                            style = MaterialTheme.typography.displaySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
            FloatingActionButton(
                onClick = {
                    if (isExerciseValid(tempExercise)) {
                        exerciseViewModel.finalizeNewExercise(lang = lang,
                            onSuccess = {
                                navController.popBackStack()
                            },
                            onError = {
                                showErrorDialog.value = true
                            })
                        navController.popBackStack()
                    } else {
                        showErrorDialog.value = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .navigationBarsPadding()
                    .size(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_save),
                    contentDescription = stringResource(R.string.save),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    ) { paddingValues ->
        tempExercise?.let { exercise ->
            val nameFilled = exercise.nameLocalized["en"]?.isNotBlank() == true &&
                    exercise.nameLocalized["ru"]?.isNotBlank() == true &&
                    exercise.nameLocalized["be"]?.isNotBlank() == true

            val equipmentFilled = exercise.equipmentLocalized.values.any { it.isNotBlank() }
            val bodyPartFilled = exercise.bodyPartLocalized.values.any { it.isNotBlank() }

            val instructionsFilled =
                exercise.instructionsLocalized["en"]?.any { it.isNotBlank() } == true &&
                        exercise.instructionsLocalized["ru"]?.any { it.isNotBlank() } == true &&
                        exercise.instructionsLocalized["be"]?.any { it.isNotBlank() } == true

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                RowSection(
                    title = stringResource(R.string.name),
                    value = exercise.getName(lang).ifBlank { stringResource(R.string.not_set) },
                    isFilled = exercise.getName(lang).isNotBlank(),
                    onClick = {
                        navController.navigate("${Screen.EditExerciseName.route}/${exercise.id}")
                    }
                )

                DividerSection()

                RowSectionWithIcon(
                    title = stringResource(R.string.equipment),
                    value = exercise.getEquipment(lang).ifBlank { stringResource(R.string.not_set) },
                    iconRes = if (exercise.getEquipment(lang).isNotBlank()) {
                        getIconForEquipment(exercise.getEquipment(lang))
                    } else {
                        R.drawable.ic_dumbbell
                    },
                    onClick = {
                        navController.navigate("${Screen.EquipmentSelection.route}/${exercise.id}")
                    },
                    isFilled = equipmentFilled
                )

                DividerSection()

                RowSectionWithIcon(
                    title = stringResource(R.string.body_part),
                    value = exercise.getBodyPart(lang).ifBlank { stringResource(R.string.not_set) },
                    iconRes = if (exercise.getBodyPart(lang).isNotBlank()) {
                        getIconForBodyPart(exercise.getBodyPart(lang))
                    } else {
                        R.drawable.ic_body_upper_arms
                    },
                    onClick = {
                        navController.navigate("${Screen.BodyPartSelection.route}/${exercise.id}")
                    },
                    isFilled = bodyPartFilled
                )

                DividerSection()

                InstructionsSection(
                    title = stringResource(R.string.instructions),
                    instructions = exercise.getInstructions(lang),
                    emptyText = stringResource(R.string.not_set),
                    isFilled = exercise.getInstructions(lang).isNotEmpty(),
                    onClick = {
                        navController.navigate("${Screen.EditExerciseInstructions.route}/${exercise.id}")
                    }
                )

                Spacer(
                    modifier = Modifier.absolutePadding(bottom = 200.dp)
                )
            }
        } ?: LoadingView()
    }
}

@Composable
private fun RowSection(
    title: String,
    value: String,
    onClick: () -> Unit,
    isFilled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFilled) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ),
            border = BorderStroke(
                width = if (isFilled) 2.dp else 1.dp,
                color = if (isFilled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun RowSectionWithIcon(
    title: String,
    value: String,
    iconRes: Int,
    onClick: () -> Unit,
    isFilled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFilled) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ),
            border = BorderStroke(
                width = if (isFilled) 2.dp else 1.dp,
                color = if (isFilled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(end = 8.dp)
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InstructionsSection(
    title: String,
    instructions: List<String>,
    emptyText: String,
    isFilled: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFilled) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ),
            border = BorderStroke(
                width = if (isFilled) 2.dp else 1.dp,
                color = if (isFilled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (instructions.isEmpty()) {
                    Text(
                        text = emptyText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    instructions.forEachIndexed { index, item ->
                        Text(
                            text = "${index + 1}. $item",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DividerSection() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 0.dp),
        thickness = 2.dp,
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
    )
}
