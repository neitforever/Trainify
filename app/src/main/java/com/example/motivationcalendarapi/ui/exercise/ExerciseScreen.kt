import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.ExerciseCatalog
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.localizedName
import com.example.motivationcalendarapi.ui.dialogs.DeleteTemplateDialog
import com.example.motivationcalendarapi.ui.exercise.fragments.CollapsibleBodyPartItem
import com.example.motivationcalendarapi.ui.exercise.fragments.ExerciseItem
import com.example.motivationcalendarapi.ui.exercise.fragments.TemplateItem
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.launch

private const val EXERCISE_PAGE_SWIPE_THRESHOLD_PX = 80f

enum class ExerciseLibraryMode {
    EXERCISES,
    TEMPLATES;

    val pageIndex: Int
        get() = when (this) {
            EXERCISES -> 0
            TEMPLATES -> 1
        }

    companion object {
        fun fromPageIndex(pageIndex: Int): ExerciseLibraryMode {
            return when (pageIndex) {
                1 -> TEMPLATES
                else -> EXERCISES
            }
        }
    }
}

data class LibrarySection<T>(
    val key: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val items: List<T>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    navController: NavController,
    exerciseViewModel: ExerciseViewModel,
    workoutViewModel: WorkoutViewModel,
    drawerState: androidx.compose.runtime.MutableState<DrawerState>,
    paddingTopValues: Dp,
    lang: String
) {
    val bodyParts by exerciseViewModel.getBodyPartsLocalized(lang).collectAsState(initial = emptyList())
    val bodyPartSections = remember(bodyParts, lang) {
        buildBodyPartSections(bodyParts = bodyParts, lang = lang)
    }

    val templates by workoutViewModel.templates.collectAsState(initial = emptyList())
    val templateSections = remember(templates, lang) {
        buildTemplateSections(templates = templates, lang = lang)
    }

    val expandedBodyParts = remember { mutableStateMapOf<String, Boolean>() }
    var selectedMode by remember { mutableStateOf(ExerciseLibraryMode.EXERCISES) }
    val pagerState = rememberPagerState(
        initialPage = ExerciseLibraryMode.EXERCISES.pageIndex,
        pageCount = { ExerciseLibraryMode.entries.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val drawerSwipeConnection = remember(pagerState, drawerState) {
        object : NestedScrollConnection {
            private var accumulatedRightDrag = 0f

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput &&
                    pagerState.currentPage == ExerciseLibraryMode.EXERCISES.pageIndex
                ) {
                    if (available.x > 0f) {
                        accumulatedRightDrag += available.x
                    } else if (available.x < 0f) {
                        accumulatedRightDrag = 0f
                    }
                }

                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (pagerState.currentPage == ExerciseLibraryMode.EXERCISES.pageIndex &&
                    accumulatedRightDrag > EXERCISE_PAGE_SWIPE_THRESHOLD_PX
                ) {
                    drawerState.value.open()
                }

                accumulatedRightDrag = 0f
                return Velocity.Zero
            }
        }
    }
    val favoriteExercises by exerciseViewModel.getFavoriteExercises().collectAsState(initial = emptyList())
    val isExercisesRefreshing by exerciseViewModel.isRefreshingExercisesFromFirestore.collectAsState()
    val isTemplatesRefreshing by workoutViewModel.isRefreshingTemplatesFromFirestore.collectAsState()
    val isRefreshing = isExercisesRefreshing || isTemplatesRefreshing
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedTemplateForDeletion by remember { mutableStateOf<Template?>(null) }

    LaunchedEffect(Unit) {
        workoutViewModel.loadTemplates()
    }

    LaunchedEffect(pagerState.currentPage) {
        selectedMode = ExerciseLibraryMode.fromPageIndex(pagerState.currentPage)
    }

    DeleteTemplateDialog(
        showDialog = showDeleteDialog,
        onDismiss = {
            showDeleteDialog = false
            selectedTemplateForDeletion = null
        },
        onConfirm = {
            selectedTemplateForDeletion?.let {
                workoutViewModel.deleteTemplate(it)
                showDeleteDialog = false
                selectedTemplateForDeletion = null
            }
        }
    )

    if (bodyParts.isEmpty()) {
        LoadingView()
    } else {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                exerciseViewModel.refreshMissingExercisesFromFirestore()
                workoutViewModel.refreshTemplatesFromFirestore()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingTopValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                ExerciseLibrarySwitcher(
                    selectedMode = selectedMode,
                    onModeSelected = { mode ->
                        selectedMode = mode
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(mode.pageIndex)
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .padding(top = 6.dp)
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .nestedScroll(drawerSwipeConnection),
                    userScrollEnabled = true
                ) { page ->
                    when (ExerciseLibraryMode.fromPageIndex(page)) {
                        ExerciseLibraryMode.EXERCISES -> {
                            ExerciseLibraryExercisesPage(
                                bodyPartSections = bodyPartSections,
                                expandedBodyParts = expandedBodyParts,
                                exerciseViewModel = exerciseViewModel,
                                favoriteExercises = favoriteExercises,
                                navController = navController,
                                lang = lang
                            )
                        }

                        ExerciseLibraryMode.TEMPLATES -> {
                            ExerciseLibraryTemplatesPage(
                                templateSections = templateSections,
                                navController = navController,
                                lang = lang,
                                onDeleteTemplate = { template ->
                                    selectedTemplateForDeletion = template
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ExerciseLibraryExercisesPage(
    bodyPartSections: List<LibrarySection<String>>,
    expandedBodyParts: MutableMap<String, Boolean>,
    exerciseViewModel: ExerciseViewModel,
    favoriteExercises: List<com.example.motivationcalendarapi.model.Exercise>,
    navController: NavController,
    lang: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        bodyPartSections.forEach { section ->
            item(key = "exercise_section_${section.key}") {
                LibrarySectionHeader(
                    title = section.title,
                    description = section.description,
                    count = section.items.size,
                    iconRes = section.iconRes,
                    lang = lang,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 5.dp)
                )
            }

            section.items.forEach { bodyPart ->
                item(key = "body_part_$bodyPart") {
                    val isExpanded = expandedBodyParts[bodyPart] == true

                    CollapsibleBodyPartItem(
                        bodyPart = bodyPart,
                        isExpanded = isExpanded,
                        onClick = { expandedBodyParts[bodyPart] = !isExpanded }
                    )

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(
                            expandFrom = Alignment.Top,
                            initialHeight = { 0 }
                        ),
                        exit = fadeOut() + shrinkVertically(
                            shrinkTowards = Alignment.Top
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val exercises by exerciseViewModel
                            .getExercisesLocalizedByBodyPart(bodyPart, lang)
                            .collectAsState(initial = emptyList())

                        Column(modifier = Modifier.fillMaxWidth()) {
                            exercises.sortedBy { it.getName(lang).lowercase(localeForLanguage(lang)) }
                                .forEach { exercise ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                navController.navigate("${Screen.ExerciseDetailView.route}/${exercise.id}")
                                            }
                                            .padding(start = 16.dp, end = 16.dp)
                                    ) {
                                        ExerciseItem(
                                            exercise = exercise,
                                            onItemClick = {
                                                navController.navigate("exercise_detail/${exercise.id}")
                                            },
                                            onFavoriteClick = { exerciseViewModel.toggleFavorite(exercise) },
                                            isFavorite = favoriteExercises.any { it.id == exercise.id },
                                            lang = lang
                                        )
                                    }
                                }
                        }
                    }
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

@Composable
private fun ExerciseLibraryTemplatesPage(
    templateSections: List<LibrarySection<Template>>,
    navController: NavController,
    lang: String,
    onDeleteTemplate: (Template) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        if (templateSections.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_templates_found),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            templateSections.forEach { section ->
                item(key = "template_section_${section.key}") {
                    LibrarySectionHeader(
                        title = section.title,
                        description = section.description,
                        count = section.items.size,
                        iconRes = section.iconRes,
                        lang = lang,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 5.dp)
                    )
                }

                section.items.forEach { template ->
                    item(key = "template_${template.id}") {
                        TemplateItem(
                            template = template,
                            onClick = {
                                navController.navigate("${Screen.TemplateDetailView.route}/${template.id}")
                            },
                            onDelete = { onDeleteTemplate(template) },
                            navController = navController,
                            lang = lang
                        )
                    }
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

@Composable
private fun ExerciseLibrarySwitcher(
    selectedMode: ExerciseLibraryMode,
    onModeSelected: (ExerciseLibraryMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ExerciseLibraryMode.entries.forEach { mode ->
            val selected = mode == selectedMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (mode) {
                        ExerciseLibraryMode.EXERCISES -> stringResource(R.string.exercises)
                        ExerciseLibraryMode.TEMPLATES -> stringResource(R.string.templates)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun LibrarySectionHeader(
    title: String,
    description: String,
    count: Int,
    iconRes: Int,
    lang: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.86f),
                modifier = Modifier.size(23.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                maxLines = 3,
                overflow = TextOverflow.Clip,
                modifier = Modifier.padding(top = 1.dp)
            )
        }

        Text(
            text = sectionCountLabel(count, lang),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.62f))
                .padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

private fun buildBodyPartSections(bodyParts: List<String>, lang: String): List<LibrarySection<String>> {
    val locale = localeForLanguage(lang)
    val collator = Collator.getInstance(locale).apply { strength = Collator.PRIMARY }

    val availableByAlias = linkedMapOf<String, String>()
    bodyParts
        .filter { it.isNotBlank() }
        .forEach { sourceName ->
            availableByAlias[sourceName.normalizedBodyPartKey(locale)] = sourceName
        }

    val used = mutableSetOf<String>()

    val groupedSections = ExerciseCatalog.bodyPartGroups.mapNotNull { group ->
        val groupedBodyParts = group.options.mapNotNull { option ->
            val aliases = buildList {
                add(option.key)
                addAll(option.localized.values)
            }

            val matched = aliases
                .asSequence()
                .mapNotNull { alias -> availableByAlias[alias.normalizedBodyPartKey(locale)] }
                .firstOrNull { it !in used }

            matched?.also { used.add(it) }
        }

        if (groupedBodyParts.isEmpty()) {
            null
        } else {
            LibrarySection(
                key = group.key,
                title = group.getTitle(lang),
                description = exerciseSectionDescription(group.key, lang),
                iconRes = sectionIcon(group.key),
                items = groupedBodyParts.sortedWith { first, second -> collator.compare(first, second) }
            )
        }
    }

    val ungrouped = bodyParts
        .filter { it.isNotBlank() && it !in used }
        .sortedWith { first, second -> collator.compare(first, second) }

    return if (ungrouped.isEmpty()) {
        groupedSections
    } else {
        groupedSections + LibrarySection(
            key = OTHER_SECTION_KEY,
            title = otherSectionTitle(lang),
            description = exerciseSectionDescription(OTHER_SECTION_KEY, lang),
            iconRes = sectionIcon(OTHER_SECTION_KEY),
            items = ungrouped
        )
    }
}

private fun buildTemplateSections(templates: List<Template>, lang: String): List<LibrarySection<Template>> {
    if (templates.isEmpty()) return emptyList()

    val locale = localeForLanguage(lang)
    val collator = Collator.getInstance(locale).apply { strength = Collator.PRIMARY }
    val groupedTemplates = linkedMapOf<String, MutableList<Template>>()

    ExerciseCatalog.bodyPartGroups.forEach { group ->
        groupedTemplates[group.key] = mutableListOf()
    }
    groupedTemplates[OTHER_SECTION_KEY] = mutableListOf()

    templates.forEach { template ->
        val sectionKey = dominantTemplateSectionKey(template = template, lang = lang, locale = locale)
        groupedTemplates.getOrPut(sectionKey) { mutableListOf() }.add(template)
    }

    return groupedTemplates.mapNotNull { (key, sectionTemplates) ->
        if (sectionTemplates.isEmpty()) {
            null
        } else {
            LibrarySection(
                key = key,
                title = templateSectionTitle(key, lang),
                description = templateSectionDescription(key, lang),
                iconRes = sectionIcon(key),
                items = sectionTemplates.sortedWith { first, second ->
                    collator.compare(first.localizedName(lang).trim(), second.localizedName(lang).trim())
                }
            )
        }
    }
}

private fun dominantTemplateSectionKey(template: Template, lang: String, locale: Locale): String {
    val counts = mutableMapOf<String, Int>()

    template.exercises.forEach { extendedExercise ->
        val bodyPart = extendedExercise.exercise.getBodyPart(lang)
            .ifBlank { extendedExercise.exercise.getBodyPart("en") }
            .ifBlank { extendedExercise.exercise.getBodyPart("ru") }
            .ifBlank { extendedExercise.exercise.getBodyPart("be") }

        val key = findCatalogSectionKeyForBodyPart(bodyPart, locale)
        counts[key] = (counts[key] ?: 0) + 1
    }

    return counts.entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { sectionOrderWeight(it.key) }
        )
        .firstOrNull()
        ?.key ?: OTHER_SECTION_KEY
}

private fun findCatalogSectionKeyForBodyPart(bodyPart: String, locale: Locale): String {
    val normalized = bodyPart.normalizedBodyPartKey(locale)
    if (normalized.isBlank()) return OTHER_SECTION_KEY

    ExerciseCatalog.bodyPartGroups.forEach { group ->
        group.options.forEach { option ->
            val aliases = buildList {
                add(option.key)
                addAll(option.localized.values)
            }

            if (aliases.any { it.normalizedBodyPartKey(locale) == normalized }) {
                return group.key
            }
        }
    }

    return OTHER_SECTION_KEY
}

private fun sectionOrderWeight(key: String): Int {
    return ExerciseCatalog.bodyPartGroups.indexOfFirst { it.key == key }.let { index ->
        if (index == -1) Int.MAX_VALUE else index
    }
}

private fun sectionIcon(key: String): Int {
    return when (key) {
        "torso" -> R.drawable.ic_body_chest
        "arms_shoulders" -> R.drawable.ic_body_shoulders
        "legs_glutes" -> R.drawable.ic_body_upper_legs
        "cardio" -> R.drawable.ic_body_cardio
        else -> R.drawable.ic_dumbbell
    }
}

private fun exerciseSectionDescription(key: String, lang: String): String {
    return when (lang.lowercase()) {
        "ru" -> when (key) {
            "torso" -> "Грудь, спина, пресс"
            "arms_shoulders" -> "Плечи, руки, предплечья"
            "legs_glutes" -> "Бёдра, ягодицы, икры"
            "cardio" -> "Кардио и выносливость"
            else -> "Прочие группы мышц"
        }
        "be", "by" -> when (key) {
            "torso" -> "Грудзі, спіна, прэс"
            "arms_shoulders" -> "Плечы, рукі, перадплеччы"
            "legs_glutes" -> "Сцёгны, ягадзіцы, ікры"
            "cardio" -> "Кардыё і вынослівасць"
            else -> "Іншыя групы мышцаў"
        }
        else -> when (key) {
            "torso" -> "Chest, back, abs"
            "arms_shoulders" -> "Shoulders, arms, forearms"
            "legs_glutes" -> "Thighs, glutes, calves"
            "cardio" -> "Cardio and endurance"
            else -> "Other body parts"
        }
    }
}

private fun templateSectionTitle(key: String, lang: String): String {
    ExerciseCatalog.bodyPartGroups.firstOrNull { it.key == key }?.let { return it.getTitle(lang) }
    return otherSectionTitle(lang)
}

private fun templateSectionDescription(key: String, lang: String): String {
    return when (lang.lowercase()) {
        "ru" -> when (key) {
            "torso" -> "Акцент на верх тела"
            "arms_shoulders" -> "Руки и плечевой пояс"
            "legs_glutes" -> "Ноги и нижняя часть тела"
            "cardio" -> "Кардио и выносливость"
            else -> "Смешанные шаблоны"
        }
        "be", "by" -> when (key) {
            "torso" -> "Акцэнт на верх цела"
            "arms_shoulders" -> "Рукі і плечавы пояс"
            "legs_glutes" -> "Ногі і ніжняя частка цела"
            "cardio" -> "Кардыё і вынослівасць"
            else -> "Змешаныя шаблоны"
        }
        else -> when (key) {
            "torso" -> "Upper body focus"
            "arms_shoulders" -> "Arms and shoulders"
            "legs_glutes" -> "Legs and lower body"
            "cardio" -> "Cardio and endurance"
            else -> "Mixed templates"
        }
    }
}

private fun otherSectionTitle(lang: String): String {
    return when (lang.lowercase()) {
        "ru" -> "Другие"
        "be", "by" -> "Іншыя"
        else -> "Other"
    }
}

private fun sectionCountLabel(count: Int, lang: String): String {
    return when (lang.lowercase()) {
        "ru" -> "$count шт."
        "be", "by" -> "$count шт."
        else -> "$count"
    }
}

private fun String.normalizedBodyPartKey(locale: Locale): String {
    return trim().lowercase(locale)
}

private fun localeForLanguage(lang: String): Locale {
    return when (lang.lowercase()) {
        "ru" -> Locale("ru")
        "be", "by" -> Locale("be")
        else -> Locale.ENGLISH
    }
}

private const val OTHER_SECTION_KEY = "other"
