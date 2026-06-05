package com.example.motivationcalendarapi.ui.exercise.ai

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.model.getIconForEquipment
import com.example.motivationcalendarapi.repositories.ai.GeminiAiGenerationApi
import com.example.motivationcalendarapi.repositories.ai.GeneratedTemplateDraft
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import com.example.motivationcalendarapi.viewmodel.AiExerciseGenerationViewModel
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch
import java.util.UUID


internal fun String.withFirstUppercase(): String = replaceFirstChar { char ->
    if (char.isLowerCase()) char.titlecase() else char.toString()
}


@Composable
internal fun SelectorRowWithIcon(
    title: String,
    value: String,
    iconRes: Int,
    isFilled: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    optionGroups: List<Pair<String, List<String>>>,
    optionIcon: (String) -> Int,
    onSelected: (String) -> Unit,
    optionContainerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    selectedOption: String = "",
    enabled: Boolean = true,
    supportingText: String? = null
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 210),
        label = "$title arrow rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        supportingText?.takeIf { it.isNotBlank() }?.let { text ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SelectableHeaderCard(
            value = value,
            iconRes = iconRes,
            isFilled = isFilled,
            rotation = rotation,
            onClick = { if (enabled) onExpandedChange(!expanded) },
            enabled = enabled
        )

        if (expanded && enabled) {
            GroupedInlineOptionsList(
                optionGroups = optionGroups,
                optionIcon = optionIcon,
                optionContainerColor = optionContainerColor,
                selectedOptions = listOf(selectedOption).filter { it.isNotBlank() },
                onSelected = { option ->
                    onSelected(option)
                    onExpandedChange(false)
                }
            )
        }
    }
}

@Composable
internal fun DifficultySelector(
    selected: String,
    onSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.difficulty_level),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DifficultyCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.easy),
                iconRes = R.drawable.ic_smile_easy,
                selected = selected == stringResource(R.string.easy),
                onClick = { if (enabled) onSelected(it) },
                enabled = enabled
            )
            DifficultyCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.medium),
                iconRes = R.drawable.ic_smile_normal,
                selected = selected == stringResource(R.string.medium),
                onClick = { if (enabled) onSelected(it) },
                enabled = enabled
            )
            DifficultyCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.hard),
                iconRes = R.drawable.ic_smile_hard,
                selected = selected == stringResource(R.string.hard),
                onClick = { if (enabled) onSelected(it) },
                enabled = enabled
            )
        }
    }
}

@Composable
internal fun DifficultyCard(
    modifier: Modifier,
    title: String,
    iconRes: Int,
    selected: Boolean,
    onClick: (String) -> Unit,
    enabled: Boolean = true
) {
    val iconColor = when (iconRes) {
        R.drawable.ic_smile_easy -> EASY_COLOR
        R.drawable.ic_smile_normal -> NORMAL_COLOR
        R.drawable.ic_smile_hard -> HARD_COLOR
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.clickable(enabled = enabled) { onClick(title) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) iconColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) iconColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) iconColor else MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AiScaffold(
    title: String,
    navController: NavController,
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable (topPadding: androidx.compose.ui.unit.Dp) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = 32.dp),
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
                    Text(title, style = MaterialTheme.typography.displaySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                modifier = Modifier.padding(0.dp)
            )
        },
        floatingActionButton = { floatingActionButton?.invoke() }
    ) { paddingValues -> content(paddingValues.calculateTopPadding()) }
}

@Composable
internal fun AiTextField(value: String, onValueChange: (String) -> Unit, label: String, minLines: Int = 1, enabled: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label) },
        minLines = minLines,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
internal fun AiDropdown(title: String, selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = selected.ifBlank { stringResource(R.string.not_set) },
                modifier = Modifier.padding(14.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        onSelected(option)
                        expanded = false
                    })
                }
            }
        }
    }
}

@Composable
internal fun MultiChoiceSection(title: String, options: List<String>, selected: MutableList<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        options.forEach { option ->
            FilterChip(
                selected = selected.contains(option),
                onClick = { if (selected.contains(option)) selected.remove(option) else selected.add(option) },
                label = { Text(option) }
            )
        }
    }
}

@Composable
internal fun CardBlock(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            content()
        }
    }
}

@Composable
internal fun LocalizedStringEditor(title: String, values: Map<String, String>, onChange: (Map<String, String>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        listOf("en", "ru", "be").forEach { key ->
            AiTextField(values[key].orEmpty(), { value -> onChange(values.toMutableMap().apply { this[key] = value }) }, key)
        }
    }
}

@Composable
internal fun LocalizedListEditor(title: String, values: Map<String, List<String>>, onChange: (Map<String, List<String>>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        listOf("en", "ru", "be").forEach { key ->
            val joined = values[key].orEmpty().joinToString("\n")
            AiTextField(joined, { value ->
                onChange(values.toMutableMap().apply { this[key] = value.lines().map { it.trim() }.filter { it.isNotBlank() } })
            }, key, minLines = 3)
        }
    }
}

@Composable
internal fun DividerSection() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 0.dp),
        thickness = 2.dp,
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
    )
}


@Composable
internal fun AiExerciseHelpCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.ai_exercise_help_title),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            )
            val guidelines = listOf(
                stringResource(R.string.ai_exercise_help_item_1),
                stringResource(R.string.ai_exercise_help_item_2),
                stringResource(R.string.ai_exercise_help_item_3)
            )
            guidelines.forEach { guideline ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_complete),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = guideline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun SelectableHeaderCard(
    value: String,
    iconRes: Int,
    isFilled: Boolean,
    rotation: Float,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFilled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = if (isFilled) 2.dp else 1.dp,
            color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clickable(enabled = enabled) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp).padding(end = 8.dp)
            )
            Text(
                text = value.withFirstUppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_drop_down),
                contentDescription = stringResource(R.string.drop_down_list),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = rotation }
            )
        }
    }
}


@Composable
internal fun InlineOptionsList(
    options: List<String>,
    optionIcon: (String) -> Int,
    optionContainerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    selectedOptions: List<String> = emptyList(),
    onSelected: (String) -> Unit
) {
    GroupedInlineOptionsList(
        optionGroups = listOf("" to options),
        optionIcon = optionIcon,
        optionContainerColor = optionContainerColor,
        selectedOptions = selectedOptions,
        onSelected = onSelected
    )
}

@Composable
internal fun GroupedInlineOptionsList(
    optionGroups: List<Pair<String, List<String>>>,
    optionIcon: (String) -> Int,
    optionContainerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    selectedOptions: List<String> = emptyList(),
    onSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        optionGroups.forEach { (groupTitle, options) ->
            if (groupTitle.isNotBlank()) {
                Text(
                    text = groupTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                )
            }

            options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowOptions.forEach { option ->
                        CompactAiOptionCard(
                            option = option,
                            iconRes = optionIcon(option),
                            containerColor = optionContainerColor,
                            isSelected = selectedOptions.contains(option),
                            onClick = { onSelected(option) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowOptions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactAiOptionCard(
    option: String,
    iconRes: Int,
    containerColor: androidx.compose.ui.graphics.Color,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else containerColor
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .height(IntrinsicSize.Min)
            .heightIn(min = 82.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = option,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = option.withFirstUppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
internal fun MultiChoiceCardsSection(
    title: String,
    options: List<String>,
    selected: MutableList<String>,
    iconForOption: (String) -> Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        options.forEach { option ->
            val isSelected = selected.contains(option)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isSelected) selected.remove(option) else selected.add(option)
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = iconForOption(option)),
                        contentDescription = option,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_complete),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AiTemplateHelpCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.ai_template_help_title),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            )
            val guidelines = listOf(
                stringResource(R.string.ai_template_help_item_1),
                stringResource(R.string.ai_template_help_item_2),
                stringResource(R.string.ai_template_help_item_3)
            )
            guidelines.forEach { guideline ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_complete),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = guideline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
internal fun LoadingCard() {
    CardBlock(title = stringResource(R.string.generation_in_progress)) {
        Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
internal fun ErrorCard(message: String, isNetworkLike: Boolean = false) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = if (isNetworkLike) R.drawable.ic_info else R.drawable.ic_close),
                contentDescription = null,
                tint = colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(if (isNetworkLike) R.string.gemini_high_demand_title else R.string.recognition_error_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface
                )
            }
        }
    }
}
