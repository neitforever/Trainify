package com.example.motivationcalendarapi.ui.template

import LoadingView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTemplateNameScreen(
    navController: NavController,
    templateId: String,
    viewModel: WorkoutViewModel
) {
    val template by viewModel.getTemplateById(templateId).collectAsState(initial = null)
    var newName by remember { mutableStateOf(template?.name ?: "") }
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    LaunchedEffect(template) {
        template?.let {
            newName = it.name
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = Screen.EditTemplateName.getTitle(context),
                        style = MaterialTheme.typography.displaySmall,
                        maxLines = 1,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back),
                        tint = colorScheme.onPrimaryContainer
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        viewModel.updateTemplateName(templateId, newName)
                        navController.popBackStack()
                    },
                    enabled = newName.isNotEmpty()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_save),
                        contentDescription = stringResource(R.string.save),
                        tint = if (newName.isNotEmpty()) colorScheme.primary
                        else colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            modifier = Modifier.border(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                shape = CutCornerShape(4.dp)
            )
        )
    }) { padding ->
        if (template == null) {
            LoadingView()

        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = colorScheme.onSurface
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.workout_template_split),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurfaceVariant
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = colorScheme.primary,
                        unfocusedIndicatorColor = colorScheme.outlineVariant,
                        cursorColor = colorScheme.primary,
                        focusedTextColor = colorScheme.onSurface,
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.template_name_guidelines),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val examples = listOf(
                    stringResource(R.string.use_descriptive_names_template),
                    stringResource(R.string.include_training_type),
                    stringResource(R.string.keep_it_short_and_recognizable)
                )

                examples.forEach { example ->
                    Text(
                        text = example,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}