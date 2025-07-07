package com.example.motivationcalendarapi.ui.exercise

import Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseInstructionsScreen(
    navController: NavController,
    exerciseId: String,
    viewModel: ExerciseViewModel
) {
    val tempExercise by viewModel.tempExercise.collectAsState()
    var newInstructions by remember { mutableStateOf("") }
    val context = LocalContext.current
    LaunchedEffect(exerciseId) {
        coroutineScope {
            launch(Dispatchers.IO) {
                if (exerciseId == tempExercise?.id) {
                    newInstructions = tempExercise?.instructions?.joinToString("\n") ?: ""
                } else {
                    val exercise = viewModel.getExerciseById(exerciseId)
                    newInstructions = exercise?.instructions?.joinToString("\n") ?: ""
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = Screen.EditExerciseInstructions.getTitle(context),
                            style = MaterialTheme.typography.displaySmall,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val instructionsList = newInstructions
                                .split("\n")
                                .filter { it.isNotBlank() }
                            if (exerciseId == tempExercise?.id) {
                                viewModel.updateTempExercise { it.copy(instructions = instructionsList) }
                            } else {
                                viewModel.updateExerciseInstructions(exerciseId, instructionsList)
                            }
                            navController.popBackStack()
                        },
                        enabled = newInstructions.isNotEmpty()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_save),
                            contentDescription = stringResource(R.string.save),
                            tint = if (newInstructions.isNotEmpty()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                modifier = Modifier.border(
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                    shape = CutCornerShape(4.dp)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            BasicTextField(
                value = newInstructions,
                onValueChange = { newInstructions = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CutCornerShape(4.dp)
                    )
                    .padding(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (newInstructions.isEmpty()) {
                            Text(
                                text = stringResource(R.string.enter_instructions),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.guidelines),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            val tips = listOf(
                stringResource(R.string.start_each_instruction_with_a_verb),
                stringResource(R.string.keep_instructions_concise_and_clear),
                stringResource(R.string.use_one_line_per_step),
                stringResource(R.string.numbering_will_be_added_automatically)
            )

            tips.forEach { tip ->
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}