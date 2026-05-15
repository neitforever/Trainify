package com.example.motivationcalendarapi.ui.exercise

import Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExerciseCatalog
import com.example.motivationcalendarapi.model.getIconForEquipment
import com.example.motivationcalendarapi.ui.dialogs.ErrorDialog
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentSelectionScreen(
    navController: NavController,
    exerciseId: String,
    viewModel: ExerciseViewModel,
    paddingValues: Float = 0f,
    lang: String
) {
    val tempExercise by viewModel.tempExercise.collectAsState()
    val showErrorDialog = remember { mutableStateOf(false) }

    var existingExercise by remember(exerciseId) { mutableStateOf<Exercise?>(null) }

    LaunchedEffect(exerciseId) {
        existingExercise = viewModel.getExerciseById(exerciseId)
    }

    val sourceExercise = if (tempExercise?.id == exerciseId) tempExercise else existingExercise

    val selectedKeyState = remember(sourceExercise?.id, sourceExercise?.equipmentLocalized) {
        mutableStateOf(
            ExerciseCatalog.equipment.firstOrNull { option ->
                option.localized["en"] == sourceExercise?.equipmentLocalized?.get("en")
            }?.key ?: ""
        )
    }

    val context = LocalContext.current

    ErrorDialog(
        showError = showErrorDialog.value,
        onDismiss = { showErrorDialog.value = false }
    )

    Scaffold(
        topBar = {
            TopAppBar(
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
                            text = Screen.EquipmentSelection.getTitle(context),
                            style = MaterialTheme.typography.displaySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val selectedOption = ExerciseCatalog.equipment.firstOrNull {
                                it.key == selectedKeyState.value
                            }

                            if (selectedOption == null) {
                                showErrorDialog.value = true
                                return@IconButton
                            }

                            if (tempExercise?.id == exerciseId) {
                                viewModel.updateTempExercise {
                                    it.copy(equipmentLocalized = selectedOption.localized)
                                }
                            } else {
                                viewModel.updateExerciseEquipment(
                                    id = exerciseId,
                                    newEquipment = selectedOption.localized
                                )
                            }

                            navController.popBackStack()
                        }
                    ) {
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
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.available_equipment),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(R.string.barbell_dumbbells),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
            }

            items(ExerciseCatalog.equipment) { option ->
                val isSelected = option.key == selectedKeyState.value

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { selectedKeyState.value = option.key },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = getIconForEquipment(option.localized["en"].orEmpty())
                            ),
                            contentDescription = option.getLabel(lang),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )

                        Text(
                            text = option.getLabel(lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedKeyState.value = option.key }
                        )
                    }
                }
            }

            item {
                Spacer(
                    modifier = Modifier.absolutePadding(bottom = 200.dp)
                )
            }
        }
    }
}