package com.example.motivationcalendarapi.ui.exercise

import LoadingView
import Screen
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.utils.dialogs.ErrorDialog
import com.example.motivationcalendarapi.utils.getIconForBodyPart
import com.example.motivationcalendarapi.utils.getIconForEquipment
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateExerciseScreen(
    navController: NavController, viewModel: ExerciseViewModel = viewModel()
) {
    val tempExercise by viewModel.tempExercise.collectAsState()
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (viewModel.tempExercise.value == null) {
            viewModel.initializeNewExercise(UUID.randomUUID().toString())
        }
    }

    BackHandler {
        viewModel.clearTempExercise()
        navController.popBackStack()
    }

    if (tempExercise == null) {
        LoadingView()
        return
    }

    Scaffold(topBar = {
        TopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ), title = {
            Text(
                text = Screen.CreateExercise.title,
                style = MaterialTheme.typography.displaySmall,
                maxLines = 1,
            )
        }, navigationIcon = {
            IconButton(onClick = {
                viewModel.clearTempExercise()
                navController.popBackStack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }, actions = {
            IconButton(onClick = {
                if (tempExercise?.let {
                        it.name.isNotBlank() && it.equipment.isNotBlank() && it.bodyPart.isNotBlank() && it.instructions.isNotEmpty()
                    } == true) {
                    viewModel.finalizeNewExercise()
                    navController.popBackStack()
                } else {
                    showError = true
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_save),
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }, modifier = Modifier.border(
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
            shape = CutCornerShape(4.dp)
        )
        )
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ErrorDialog(
                showError = showError,
                onDismiss = { showError = false}
                    )

            tempExercise?.let { exercise ->
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("${Screen.EditExerciseName.route}/${exercise.id}") }
                    .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Name",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
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
                    Text(
                        text = exercise.name.ifBlank { "Not set" },
                        style = MaterialTheme.typography.titleLarge,
                        color = if (exercise.name.isBlank()) MaterialTheme.colorScheme.outline
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                HorizontalDivider(
                    thickness = 2.dp, color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                )

                // Equipment Section
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("${Screen.EquipmentSelection.route}/${exercise.id}") }
                    .padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Equipment",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
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
                    if (exercise.equipment.isEmpty()) {
                        Text(
                            text = "Not set",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Card(
                            shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ), modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = getIconForEquipment(exercise.equipment)),
                                    contentDescription = "Equipment",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(end = 8.dp)
                                )
                                Text(
                                    text = exercise.equipment,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (exercise.bodyPart.isBlank()) MaterialTheme.colorScheme.outline
                                    else MaterialTheme.colorScheme.primary,
                                )
                            }}
                    }
                }
                HorizontalDivider(
                    thickness = 2.dp, color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                )

                // Body Part Section
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("${Screen.BodyPartSelection.route}/${exercise.id}") }
                    .padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Body Part",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
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
                    if (exercise.bodyPart.isEmpty()) {
                        Text(
                            text = "Not set",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Card(
                            shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ), modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = getIconForBodyPart(exercise.bodyPart)),
                                    contentDescription = "Body Part",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(end = 8.dp)
                                )
                        Text(
                            text = exercise.bodyPart,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (exercise.bodyPart.isBlank()) MaterialTheme.colorScheme.outline
                            else MaterialTheme.colorScheme.primary,
                        )
                    }}
                }}
//                HorizontalDivider(
//                    thickness = 2.dp, color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
//                )
//
//                // Secondary Muscles Section
//                Column(modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { navController.navigate("${Screen.SecondaryMusclesSelection.route}/${exercise.id}") }
//                    .padding(8.dp)) {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "Secondary Muscles",
//                            style = MaterialTheme.typography.headlineSmall,
//                            color = MaterialTheme.colorScheme.onSurface,
//                            modifier = Modifier.weight(1f)
//                        )
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_edit),
//                            contentDescription = "Edit",
//                            tint = MaterialTheme.colorScheme.tertiary,
//                            modifier = Modifier
//                                .size(28.dp)
//                                .padding(start = 8.dp)
//                        )
//                    }
//                    FlowRow(
//                        modifier = Modifier.padding(top = 8.dp),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        if (exercise.secondaryMuscles.isEmpty()) {
//                            Text(
//                                text = "Not set",
//                                style = MaterialTheme.typography.titleMedium,
//                                color = MaterialTheme.colorScheme.outline,
//                                modifier = Modifier.padding(top = 8.dp)
//                            )
//                        } else {
//                            exercise.secondaryMuscles
//                                .flatMap { muscleEntry ->
//                                    muscleEntry
//                                        .replace("[", "")
//                                        .replace("]", "")
//                                        .replace("\"", "")
//                                        .split(",")
//                                        .map { it.trim() }
//                                }
//                                .filter { it.isNotBlank() }
//                                .forEach { muscle ->
//                                    Card(
//                                        shape = RoundedCornerShape(8.dp),
//                                        colors = CardDefaults.cardColors(
//                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
//                                        ),
//                                        modifier = Modifier
//                                            .padding(4.dp)
//                                            .wrapContentWidth()
//                                    ) {
//                                        Text(
//                                            text = muscle,
//                                            style = MaterialTheme.typography.titleMedium,
//                                            color = MaterialTheme.colorScheme.primary,
//                                            modifier = Modifier.padding(12.dp)
//                                        )
//                                    }
//                                }
//                        }
//                    }
//                }
                HorizontalDivider(
                    thickness = 2.dp, color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                )

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("${Screen.EditExerciseInstructions.route}/${exercise.id}") }
                    .padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Instructions",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
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
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        if (exercise.instructions.isEmpty()) {
                            Text(
                                text = "Not set",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            exercise.instructions.forEachIndexed { index, instruction ->
                                Text(
                                    text = "${index + 1}. $instruction",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.absolutePadding(bottom = 200.dp))
        }
    }
}