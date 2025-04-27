package com.example.motivationcalendarapi.ui.workout.fragments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.model.Template

@Composable
fun TemplatesListSection(
    templates: List<Template>,
    onTemplateSelected: (Template) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(templates) { template ->
            TemplateSelectionItem(
                template = template,
                onTemplateClick = { onTemplateSelected(it) }
            )
        }
    }
}

@Composable
fun TemplateSelectionItem(
    template: Template,
    onTemplateClick: (Template) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onTemplateClick(template) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Упражнений: ${template.exercises.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}