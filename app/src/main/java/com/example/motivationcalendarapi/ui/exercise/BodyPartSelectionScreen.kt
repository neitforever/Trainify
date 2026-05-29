package com.example.motivationcalendarapi.ui.exercise

import Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExerciseCatalog
import com.example.motivationcalendarapi.model.LocalizedOption
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.ui.dialogs.ErrorDialog
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyPartSelectionScreen(
    navController: NavController,
    exerciseId: String,
    viewModel: ExerciseViewModel,
    paddingValues: Dp,
    lang: String
) {
    val tempExercise by viewModel.tempExercise.collectAsState()
    val suggestedBodyPartKey by viewModel.suggestedBodyPartKey.collectAsState()
    val showErrorDialog = remember { mutableStateOf(false) }

    var existingExercise by remember(exerciseId) { mutableStateOf<Exercise?>(null) }

    LaunchedEffect(exerciseId) {
        existingExercise = viewModel.getExerciseById(exerciseId)
    }

    val sourceExercise = if (tempExercise?.id == exerciseId) tempExercise else existingExercise
    val exerciseName = sourceExercise?.getName(lang).orEmpty()
        .ifBlank { sourceExercise?.getName("en").orEmpty() }

    LaunchedEffect(exerciseId, exerciseName, lang) {
        viewModel.clearBodyPartSuggestion()
        if (exerciseName.isNotBlank()) {
            viewModel.requestBodyPartSuggestion(
                exerciseName = exerciseName,
                lang = lang,
                options = ExerciseCatalog.bodyParts
            )
        }
    }

    val selectedKeyState = remember(sourceExercise?.id, sourceExercise?.bodyPartLocalized) {
        mutableStateOf(
            ExerciseCatalog.bodyParts.firstOrNull { option ->
                option.localized["en"] == sourceExercise?.bodyPartLocalized?.get("en")
            }?.key ?: ""
        )
    }

    val orderedBodyPartOptions = remember(selectedKeyState.value, suggestedBodyPartKey) {
        prioritizeSelectionOptions(
            options = ExerciseCatalog.bodyParts,
            selectedKey = selectedKeyState.value,
            suggestedKey = suggestedBodyPartKey
        )
    }

    val context = LocalContext.current

    fun saveSelectedBodyPart() {
        val selectedOption = ExerciseCatalog.bodyParts.firstOrNull { it.key == selectedKeyState.value }
        if (selectedOption == null) {
            showErrorDialog.value = true
            return
        }

        if (tempExercise?.id == exerciseId) {
            viewModel.updateTempExercise { it.copy(bodyPartLocalized = selectedOption.localized) }
        } else {
            viewModel.updateExerciseBodyPart(exerciseId, selectedOption.localized)
        }

        navController.popBackStack()
    }

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
                    Box(modifier = Modifier.fillMaxWidth().padding(start = 4.dp)) {
                        Text(
                            text = Screen.BodyPartSelection.getTitle(context),
                            style = MaterialTheme.typography.displaySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { saveSelectedBodyPart() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_save),
                            contentDescription = stringResource(R.string.save_body_part),
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
                SelectionInfoCard(
                    title = stringResource(R.string.selection_ai_help_title),
                    description = stringResource(R.string.body_part_selection_ai_hint),
                    modifier = Modifier.padding(bottom = 18.dp)
                )
            }

            items(orderedBodyPartOptions.chunked(3)) { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowOptions.forEach { option ->
                        BodyPartSelectionCard(
                            option = option,
                            lang = lang,
                            isSelected = option.key == selectedKeyState.value,
                            isAiSuggested = option.key == suggestedBodyPartKey,
                            onClick = {
                                if (option.key == selectedKeyState.value) {
                                    saveSelectedBodyPart()
                                } else {
                                    selectedKeyState.value = option.key
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - rowOptions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.absolutePadding(bottom = 200.dp)) }
        }
    }
}

@Composable
private fun BodyPartSelectionCard(
    option: LocalizedOption,
    lang: String,
    isSelected: Boolean,
    isAiSuggested: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = option.getLabel(lang).replaceFirstChar { firstChar ->
        if (firstChar.isLowerCase()) firstChar.titlecase() else firstChar.toString()
    }

    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isAiSuggested -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }
    val titleColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .height(126.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (isSelected || isAiSuggested) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (isAiSuggested) {
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .widthIn(min = 28.dp)
                            .background(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.ai_recommended_short),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = getIconForBodyPart(option.localized["en"].orEmpty())),
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

            }
        }
    }
}

private fun prioritizeSelectionOptions(
    options: List<LocalizedOption>,
    selectedKey: String?,
    suggestedKey: String?
): List<LocalizedOption> {
    val pinnedKeys = listOfNotNull(
        selectedKey?.takeIf { it.isNotBlank() },
        suggestedKey?.takeIf { it.isNotBlank() && it != selectedKey }
    )
    val pinned = pinnedKeys.mapNotNull { key -> options.firstOrNull { it.key == key } }
    val rest = options.filterNot { option -> pinnedKeys.contains(option.key) }
    return pinned + rest
}
