package com.example.motivationcalendarapi.ui.workout.fragments

import Screen
import com.example.motivationcalendarapi.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.ui.exercise.fragments.SearchBar
import com.example.motivationcalendarapi.ui.exercise.fragments.SearchResultsList
import com.example.motivationcalendarapi.utils.ClearFocusOnKeyboardDismiss
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectionBottomSheet(
    isSheetOpen: MutableState<Boolean>,
    sheetState: SheetState,
    exerciseViewModel: ExerciseViewModel,
    workoutViewModel: WorkoutViewModel,
    navController: NavController,
    lang: String
) {
    if (isSheetOpen.value) {
        val selectedExercises = remember { mutableStateListOf<Exercise>() }
        val addedExercises by workoutViewModel.selectedExercises.collectAsState()
        val templates by workoutViewModel.templates.collectAsState(initial = emptyList())
        var searchQuery by remember { mutableStateOf("") }
        var pendingTemplate by remember { mutableStateOf<Pair<Template, String>?>(null) }
        val allSearchResults by exerciseViewModel.searchExercises(searchQuery, lang)
            .collectAsState(initial = emptyList())
        val bodyParts by exerciseViewModel.getBodyPartsLocalized(lang)
            .collectAsState(initial = emptyList())
        val allExercises by exerciseViewModel.getAllExercises()
            .collectAsState(initial = emptyList())
        val favoriteExercises by exerciseViewModel.getFavoriteExercises()
            .collectAsState(initial = emptyList())
        val isExerciseLibraryReady by remember(bodyParts, allExercises) {
            derivedStateOf { bodyParts.isNotEmpty() && allExercises.isNotEmpty() }
        }

        val pagerState = rememberPagerState(initialPage = 0, pageCount = { WorkoutLibraryMode.entries.size })
        val coroutineScope = rememberCoroutineScope()
        val selectedMode = WorkoutLibraryMode.entries[pagerState.currentPage]
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val view = LocalView.current
        fun hideKeyboardAndClearFocus() {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            val imm = view.context.getSystemService(InputMethodManager::class.java)
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun closeSheet() {
            isSheetOpen.value = false
            selectedExercises.clear()
            searchQuery = ""
            pendingTemplate = null
            hideKeyboardAndClearFocus()
        }

        fun applyTemplate(template: Template, templateName: String) {
            workoutViewModel.setWorkoutName(templateName)
            workoutViewModel.addExercisesFromTemplate(template)
            closeSheet()
        }

        ClearFocusOnKeyboardDismiss()

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
                .map { WorkoutLibraryMode.entries[it] }
                .distinctUntilChanged()
                .collectLatest { mode ->
                    if (mode == WorkoutLibraryMode.TEMPLATES) {
                        hideKeyboardAndClearFocus()
                    }
                }
        }

        pendingTemplate?.let { (template, templateName) ->
            ReplaceWorkoutContentDialog(
                lang = lang,
                onDismiss = { pendingTemplate = null },
                onConfirm = { applyTemplate(template, templateName) }
            )
        }

        if (isExerciseLibraryReady) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { closeSheet() },
                contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) }
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            ) {
                WorkoutLibrarySwitcher(
                    selectedMode = selectedMode,
                    onModeSelected = { mode ->
                        if (mode == WorkoutLibraryMode.TEMPLATES) {
                            hideKeyboardAndClearFocus()
                        }
                        coroutineScope.launch {
                            pagerState.scrollToPage(mode.ordinal)
                            if (mode == WorkoutLibraryMode.TEMPLATES) {
                                hideKeyboardAndClearFocus()
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (WorkoutLibraryMode.entries[page]) {
                        WorkoutLibraryMode.EXERCISES -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                SearchBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp)
                                )

                                when {
                                    searchQuery.isNotEmpty() -> SearchResultsList(
                                        searchQuery = searchQuery,
                                        searchResults = allSearchResults,
                                        selectedExercises = selectedExercises,
                                        addedExercises = addedExercises.map { it.exercise },
                                        onExerciseSelected = { exercise ->
                                            if (selectedExercises.any { it.id == exercise.id }) {
                                                selectedExercises.removeAll { it.id == exercise.id }
                                            } else {
                                                selectedExercises.add(exercise)
                                            }
                                        },
                                        onAddAll = {
                                            selectedExercises.forEach { exercise ->
                                                workoutViewModel.addExercise(
                                                    ExtendedExercise(exercise, emptyList())
                                                )
                                            }
                                            closeSheet()
                                        },
                                        lang = lang
                                    )

                                    else -> BodyPartsList(
                                        bodyParts = bodyParts,
                                        allExercises = allExercises,
                                        favoriteExercises = favoriteExercises,
                                        selectedExercises = selectedExercises,
                                        addedExercises = addedExercises.map { it.exercise },
                                        onExerciseSelected = { exercise ->
                                            if (selectedExercises.any { it.id == exercise.id }) {
                                                selectedExercises.removeAll { it.id == exercise.id }
                                            } else {
                                                selectedExercises.add(exercise)
                                            }
                                        },
                                        onAddAll = {
                                            selectedExercises.forEach { exercise ->
                                                workoutViewModel.addExercise(
                                                    ExtendedExercise(exercise, emptyList())
                                                )
                                            }
                                            closeSheet()
                                        },
                                        lang = lang
                                    )
                                }
                            }
                        }

                        WorkoutLibraryMode.TEMPLATES -> {
                            TemplatesListSection(
                                templates = templates,
                                lang = lang,
                                onTemplateSelected = { template, templateName ->
                                    hideKeyboardAndClearFocus()
                                    if (addedExercises.isNotEmpty()) {
                                        pendingTemplate = template to templateName
                                    } else {
                                        applyTemplate(template, templateName)
                                    }
                                },
                                onViewDetails = { template ->
                                    hideKeyboardAndClearFocus()
                                    navController.navigate("${Screen.TemplateDetailView.route}/${template.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

}
@Composable
private fun ReplaceWorkoutContentDialog(
    lang: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val title = when (lang.lowercase()) {
        "ru" -> "Заменить упражнения"
        "be", "by" -> "Замяніць практыкаванні"
        else -> "Replace exercises"
    }

    val description = when (lang.lowercase()) {
        "ru" -> "В текущей тренировке уже есть упражнения. Если выбрать шаблон, текущий список будет очищен и заменён упражнениями из выбранного шаблона."
        "be", "by" -> "У бягучай трэніроўцы ўжо ёсць практыкаванні. Калі выбраць шаблон, бягучы спіс будзе ачышчаны і заменены практыкаваннямі з выбранага шаблону."
        else -> "This workout already contains exercises. If you choose a template, the current list will be cleared and replaced with exercises from the selected template."
    }

    val confirmText = when (lang.lowercase()) {
        "ru" -> "Заменить"
        "be", "by" -> "Замяніць"
        else -> "Replace"
    }

    val cancelText = when (lang.lowercase()) {
        "ru" -> "Отмена"
        "be", "by" -> "Адмена"
        else -> "Cancel"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_help_question),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        },
        text = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Justify
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = when (lang.lowercase()) {
                                "ru" -> "Это действие нельзя отменить автоматически."
                                "be", "by" -> "Гэта дзеянне нельга адмяніць аўтаматычна."
                                else -> "This action cannot be undone automatically."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = cancelText,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
