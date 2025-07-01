package com.example.motivationcalendarapi.ui.body_progress

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.example.motivationcalendarapi.model.BodyProgress
import com.example.motivationcalendarapi.ui.body_progress.fragments.ProgressItem
import com.example.motivationcalendarapi.ui.body_progress.fragments.WeightHistoryChart
import com.example.motivationcalendarapi.viewmodel.BodyProgressViewModel
import com.example.motivationcalendarapi.ui.dialogs.AddProgressDialog
import com.example.motivationcalendarapi.ui.dialogs.ProgressDetailDialog
import com.example.motivationcalendarapi.utils.saveImageToInternalStorage
import java.util.UUID


@Composable
fun BodyProgressScreen(
    viewModel: BodyProgressViewModel,
    navController: NavController,
    context: Context,
    paddingValues: Dp
) {

    val progressList by viewModel.allProgress.collectAsState(initial = emptyList())
    var weightInput by remember { mutableStateOf("") }
    var selectedProgress by remember { mutableStateOf<BodyProgress?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    if (progressList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues, bottom = 120.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(max = 400.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_body_upper_arms),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = stringResource(R.string.body_progress_track_progress),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.body_progress_tracking_description),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        val guidelines = listOf(
                            stringResource(R.string.measure_at_the_same_time_daily),
                            stringResource(R.string.use_a_calibrated_scale),
                            stringResource(R.string.wear_similar_clothing),
                            stringResource(R.string.track_weekly_for_best_results)
                        )

                        guidelines.forEach { guideline ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_complete),
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = guideline,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }



                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.surfaceVariant,
                        contentColor = colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = stringResource(R.string.add_weight),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.add_first_measurement),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }

                }

            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(top = paddingValues)
                .fillMaxSize()
        ) {

            item {
                if (progressList.size < 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.surfaceVariant)
                            .border(2.dp, colorScheme.onSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.body_progress_not_enough_data),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    WeightHistoryChart(
                        progressList = progressList,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(16.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.surfaceVariant)
                            .border(
                                2.dp,
                                colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    )
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = colorScheme.onSurface.copy(alpha = 0.5f),
                    thickness = 4.dp
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val lastWeight = progressList.firstOrNull()?.weight ?: 0.0
                    Text(
                        text = stringResource(R.string.current_weight),
                        style = MaterialTheme.typography.headlineMedium,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.weight_with_unit_precise, lastWeight),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.surfaceVariant,
                            contentColor = colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp, pressedElevation = 8.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = stringResource(R.string.edit_weight),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(R.string.update_weight),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.weight_history),
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp)
                )
            }
            items(progressList) { progress ->
                ProgressItem(
                    progress = progress,
                    onDelete = { viewModel.deleteProgress(progress) },
                    onClick = { selectedProgress = progress })
            }
            item {
                Spacer(
                    modifier = Modifier.absolutePadding(bottom = 200.dp)
                )
            }
        }
    }


    if (showAddDialog) {
        AddProgressDialog(
            showDialog = showAddDialog,
            initialWeight = progressList.firstOrNull()?.weight ?: 0.0,
            onDismiss = { showAddDialog = false },
            onSave = { weight, photoUri ->
                val photoPath = saveImageToInternalStorage(context, photoUri)
                viewModel.addProgress(
                    BodyProgress(
                        id = UUID.randomUUID().toString(),
                        weight = weight,
                        photoPath = photoPath
                    )
                )
            }
        )
    }

    selectedProgress?.let { progress ->
        ProgressDetailDialog(
            progress = progress, onDismiss = { selectedProgress = null })
    }
}