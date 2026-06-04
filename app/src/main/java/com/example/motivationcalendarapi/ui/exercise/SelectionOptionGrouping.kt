package com.example.motivationcalendarapi.ui.exercise

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.model.LocalizedOption
import com.example.motivationcalendarapi.model.LocalizedOptionGroup

data class SelectionOptionSection(
    val title: String,
    val options: List<LocalizedOption>
)

@Composable
fun SelectionGroupTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 8.dp, bottom = 8.dp)
    )
}

fun groupedPrioritizedSelectionOptions(
    groups: List<LocalizedOptionGroup>,
    selectedKey: String?,
    suggestedKey: String?,
    lang: String
): List<SelectionOptionSection> {
    return groups.mapNotNull { group ->
        val options = group.options
        if (options.isEmpty()) {
            null
        } else {
            SelectionOptionSection(title = group.getTitle(lang), options = options)
        }
    }
}
