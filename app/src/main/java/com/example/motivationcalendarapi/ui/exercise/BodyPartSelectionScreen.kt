package com.example.motivationcalendarapi.ui.exercise

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
    val allBodyParts by viewModel.allBodyParts.collectAsState(initial = emptyList())
    var newBodyPart by remember { mutableStateOf(emptyMap<String, String>()) }

    LaunchedEffect(exerciseId) {
        coroutineScope {
            launch(Dispatchers.IO) {
                if (exerciseId == tempExercise?.id) {
//                    newBodyPart = tempExercise?.getBodyPart(lang) ?: ""
                } else {
                    val exercise = viewModel.getExerciseById(exerciseId)
//                    newBodyPart = exercise?.getBodyPart(lang) ?: ""
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(top = paddingValues)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .fillMaxSize()
    ) {
//        TextField(
//            value = newBodyPart,
//            onValueChange = { newBodyPart = it },
//            modifier = Modifier.fillMaxWidth(),
//            textStyle = MaterialTheme.typography.bodyLarge.copy(
//                color = MaterialTheme.colorScheme.onSurface,
//                fontSize = 18.sp
//            ),
//            placeholder = {
//                Text(
//                    text = stringResource(R.string.chest_back_legs),
//                    style = MaterialTheme.typography.bodyLarge,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            },
//            colors = TextFieldDefaults.colors(
//                focusedContainerColor = Color.Transparent,
//                unfocusedContainerColor = Color.Transparent,
//                disabledContainerColor = Color.Transparent,
//                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
//                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
//                cursorColor = MaterialTheme.colorScheme.primary,
//                focusedTextColor = MaterialTheme.colorScheme.onSurface,
//            ),
//            singleLine = true,
//            shape = MaterialTheme.shapes.medium
//        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (exerciseId == tempExercise?.id) {
//                    viewModel.updateTempExercise { it.copy(bodyPart = newBodyPart) }
                } else {
                    viewModel.updateExerciseBodyPart(exerciseId, newBodyPart)
                }
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = newBodyPart.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                contentColor = MaterialTheme.colorScheme.primary,
            )
        ) {
            Text(
                text = stringResource(R.string.save_body_part),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.available_body_parts),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )

//        LazyVerticalGrid(
//            columns = GridCells.Fixed(2),
//            contentPadding = PaddingValues(4.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            items(allBodyParts) { bodyPart ->
//                val isSelected = bodyPart == newBodyPart
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(80.dp)
//                        .clickable {
//                            newBodyPart = bodyPart
//                        }
//                        .animateContentSize()
//                        .padding(4.dp),
//                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
//                    else MaterialTheme.colorScheme.surfaceVariant,
//                    shape = CutCornerShape(8.dp),
//                    shadowElevation = 4.dp
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .padding(12.dp)
//                            .fillMaxWidth(),
//                        horizontalAlignment = Alignment.Start
//                    ) {
//                        Icon(
//                            painter = painterResource(id = getIconForBodyPart(bodyPart)),
//                            contentDescription = bodyPart,
//                            tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
//                            else MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.size(24.dp)
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            text = bodyPart,
//                            style = MaterialTheme.typography.bodyLarge.copy(
//                                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
//                                else MaterialTheme.colorScheme.onSurface,
//                                textAlign = TextAlign.Start,
//                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
//                            ),
//                            maxLines = 2,
//                            overflow = TextOverflow.Ellipsis
//                        )
//                    }
//                }
//            }
//            item {
//                Spacer(
//                    modifier = Modifier
//                        .absolutePadding(bottom = 200.dp)
//                )
//            }
//        }
    }
}