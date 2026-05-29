package com.example.motivationcalendarapi.ui.exercise

import Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.ui.dialogs.ErrorDialog
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseNameScreen(
    navController: NavController,
    exerciseId: String,
    viewModel: ExerciseViewModel,
    lang: String
) {
    val tempExercise by viewModel.tempExercise.collectAsState()
    val showErrorDialog = remember { mutableStateOf(false) }

    var existingExercise by remember(exerciseId) { mutableStateOf<Exercise?>(null) }

    LaunchedEffect(exerciseId) {
        existingExercise = viewModel.getExerciseById(exerciseId)
    }

    val sourceExercise = if (tempExercise?.id == exerciseId) tempExercise else existingExercise

    val enState = remember(sourceExercise?.id) {
        mutableStateOf(sourceExercise?.nameLocalized?.get("en") ?: "")
    }
    val ruState = remember(sourceExercise?.id) {
        mutableStateOf(sourceExercise?.nameLocalized?.get("ru") ?: "")
    }
    val beState = remember(sourceExercise?.id) {
        mutableStateOf(sourceExercise?.nameLocalized?.get("be") ?: "")
    }

    val context = LocalContext.current

    ErrorDialog(
        showError = showErrorDialog.value,
        onDismiss = { showErrorDialog.value = false }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = 20.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                            text = Screen.EditExerciseName.getTitle(context),
                            style = MaterialTheme.typography.displaySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val updatedMap = mapOf(
                            "en" to enState.value.trim(),
                            "ru" to ruState.value.trim(),
                            "be" to beState.value.trim()
                        )

                        val isValid = updatedMap.values.all { it.isNotBlank() }

                        if (!isValid) {
                            showErrorDialog.value = true
                            return@IconButton
                        }

                        if (tempExercise?.id == exerciseId) {
                            viewModel.updateTempExercise { it.copy(nameLocalized = updatedMap) }
                        } else {
                            viewModel.updateExerciseName(exerciseId, updatedMap)
                        }

                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_save),
                            contentDescription = stringResource(R.string.save),
                            tint = MaterialTheme.colorScheme.primary,
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.exercise_name_guidelines),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.use_descriptive_names),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(R.string.include_equipment_when_relevant),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = stringResource(R.string.keep_it_short_but_informative),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            OutlinedTextField(
                value = enState.value,
                onValueChange = { enState.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("English") },
                placeholder = { Text("Biceps Curl") },
                singleLine = false
            )

            OutlinedTextField(
                value = ruState.value,
                onValueChange = { ruState.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("Русский") },
                placeholder = { Text("Сгибание рук на бицепс") },
                singleLine = false
            )

            OutlinedTextField(
                value = beState.value,
                onValueChange = { beState.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("Беларуская") },
                placeholder = { Text("Згінанне рук на біцэпс") },
                singleLine = false
            )

            Spacer(
                modifier = Modifier.absolutePadding(bottom = 400.dp)
            )
        }
    }
}
