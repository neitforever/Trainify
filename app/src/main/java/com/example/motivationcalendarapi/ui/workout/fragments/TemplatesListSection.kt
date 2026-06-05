package com.example.motivationcalendarapi.ui.workout.fragments

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.localizedName

@Composable
fun TemplatesListSection(
    templates: List<Template>,
    lang: String,
    onTemplateSelected: (Template, String) -> Unit,
    onViewDetails: (Template) -> Unit
) {
    val templateSections = remember(templates, lang) {
        buildWorkoutTemplateSections(templates = templates, lang = lang)
    }

    if (templateSections.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_templates_found),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            templateSections.forEach { section ->
                item(key = "template_header_${section.key}") {
                    WorkoutLibrarySectionHeader(
                        title = section.title,
                        description = section.description,
                        count = section.items.size,
                        iconRes = section.iconRes,
                        lang = lang,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 5.dp)
                    )
                }

                items(section.items, key = { template -> template.id }) { template ->
                    val templateName = template.localizedName(lang)

                    TemplateSelectionItem(
                        template = template,
                        templateName = templateName,
                        onTemplateSelected = {
                            onTemplateSelected(template, templateName)
                        },
                        onViewDetails = onViewDetails
                    )
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

@Composable
fun TemplateSelectionItem(
    template: Template,
    templateName: String,
    onTemplateSelected: () -> Unit,
    onViewDetails: (Template) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = templateName.firstOrNull()?.uppercase() ?: "",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .clickable(onClick = onTemplateSelected),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = templateName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.exercises_count, template.exercises.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box {
            Icon(
                painter = painterResource(id = R.drawable.ic_dots),
                contentDescription = stringResource(R.string.template_menu),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { showMenu = true }
            )

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = stringResource(R.string.view_exercises),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = stringResource(R.string.view_exercises),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = {
                        onViewDetails(template)
                        showMenu = false
                    }
                )
            }
        }
    }
}
