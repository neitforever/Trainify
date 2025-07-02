package com.example.motivationcalendarapi.ui.exercise

import Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseNameScreen(
    navController: NavController, exerciseId: String, viewModel: ExerciseViewModel
) {
    val exercise = remember { mutableStateOf<Exercise?>(null) }
    val colorScheme = MaterialTheme.colorScheme

    val tempExercise by viewModel.tempExercise.collectAsState()
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(exerciseId) {
        coroutineScope {
            launch(Dispatchers.IO) {
                viewModel.tempExercise.value?.let { temp ->
                    if (temp.id == exerciseId) {
                        newName = temp.name
                    }
                } ?: run {
                    val exercise = viewModel.getExerciseById(exerciseId)
                    newName = exercise?.name ?: ""
                }
            }
        }
    }


    Scaffold(topBar = {
        TopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ), title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
            ) {
                Text(
                    text = Screen.EditExerciseName.title,
                    style = MaterialTheme.typography.displaySmall,
                    maxLines = 1,
                )
            }
        }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back),
                    tint = colorScheme.onPrimaryContainer
                )
            }
        }, actions = {
            IconButton(
                onClick = {
                    if (exerciseId == tempExercise?.id) {
                        viewModel.updateTempExercise { it.copy(name = newName) }
                    } else {
                        viewModel.updateExerciseName(exerciseId, newName)
                    }
                    navController.popBackStack()
                }, enabled = newName.isNotEmpty()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_save),
                    contentDescription = stringResource(R.string.save),
                    tint = if (newName.isNotEmpty()) colorScheme.primary
                    else colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }, modifier = Modifier.border(
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
            shape = CutCornerShape(4.dp)
        )
        )
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = newName,
                onValueChange = { newName = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = colorScheme.onSurface
                ),
                placeholder = {
                    Text(
                        text = stringResource(R.string.bench_press_squats),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = colorScheme.primary,
                    unfocusedIndicatorColor = colorScheme.outlineVariant,
                    cursorColor = colorScheme.primary,
                    focusedTextColor = colorScheme.onSurface,
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.exercise_name_guidelines),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = colorScheme.primary, fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            val examples = listOf(
                stringResource(R.string.use_descriptive_names),
                stringResource(R.string.include_equipment_when_relevant),
                stringResource(R.string.keep_it_short_but_informative)
            )

            examples.forEach { example ->
                Text(
                    text = example, style = MaterialTheme.typography.bodyMedium.copy(
                        color = colorScheme.onSurfaceVariant, lineHeight = 20.sp
                    ), modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}