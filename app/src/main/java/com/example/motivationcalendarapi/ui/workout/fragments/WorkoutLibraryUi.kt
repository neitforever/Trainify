package com.example.motivationcalendarapi.ui.workout.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.ExerciseCatalog
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.localizedName
import java.text.Collator
import java.util.Locale

enum class WorkoutLibraryMode {
    EXERCISES,
    TEMPLATES
}

data class WorkoutLibrarySection<T>(
    val key: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val items: List<T>
)

@Composable
fun WorkoutLibrarySwitcher(
    selectedMode: WorkoutLibraryMode,
    onModeSelected: (WorkoutLibraryMode) -> Unit,
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
        WorkoutLibraryMode.entries.forEach { mode ->
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
                        WorkoutLibraryMode.EXERCISES -> stringResource(R.string.exercises)
                        WorkoutLibraryMode.TEMPLATES -> stringResource(R.string.templates)
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
fun WorkoutLibrarySectionHeader(
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
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

fun buildWorkoutBodyPartSections(bodyParts: List<String>, lang: String): List<WorkoutLibrarySection<String>> {
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
            WorkoutLibrarySection(
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
        groupedSections + WorkoutLibrarySection(
            key = OTHER_SECTION_KEY,
            title = otherSectionTitle(lang),
            description = exerciseSectionDescription(OTHER_SECTION_KEY, lang),
            iconRes = sectionIcon(OTHER_SECTION_KEY),
            items = ungrouped
        )
    }
}

fun buildWorkoutTemplateSections(templates: List<Template>, lang: String): List<WorkoutLibrarySection<Template>> {
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
            WorkoutLibrarySection(
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
            else -> "Смешанные планы"
        }
        "be", "by" -> when (key) {
            "torso" -> "Акцэнт на верх цела"
            "arms_shoulders" -> "Рукі і плечавы пояс"
            "legs_glutes" -> "Ногі і ніжняя частка цела"
            "cardio" -> "Кардыё і вынослівасць"
            else -> "Змешаныя планы"
        }
        else -> when (key) {
            "torso" -> "Upper body focus"
            "arms_shoulders" -> "Arms and shoulders"
            "legs_glutes" -> "Legs and lower body"
            "cardio" -> "Cardio and endurance"
            else -> "Mixed plans"
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
