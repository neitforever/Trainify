package com.example.motivationcalendarapi.ui.exercise.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R

@Composable
fun ExerciseDetailFabMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOpenTechnique: () -> Unit,
    onAnalyzeTechnique: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .navigationBarsPadding()
            .wrapContentSize(Alignment.BottomEnd)
    ) {
        if (isExpanded) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MatrixFabButton(
                    iconResId = R.drawable.ic_camera,
                    contentDescription = stringResource(R.string.technique_analysis),
                    onClick = onAnalyzeTechnique
                )

                MatrixFabButton(
                    iconResId = R.drawable.ic_delete,
                    contentDescription = stringResource(R.string.delete_exercise),
                    onClick = onDelete,
                    iconTint = MaterialTheme.colorScheme.errorContainer
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MatrixFabButton(
                    iconResId = R.drawable.ic_youtube,
                    contentDescription = stringResource(R.string.technique_watch_title),
                    onClick = onOpenTechnique,
                    iconTint = Color.Unspecified,
                    iconSize = 42
                )

                MatrixFabButton(
                    iconResId = R.drawable.ic_close,
                    contentDescription = stringResource(R.string.hide),
                    onClick = onToggle
                )
            }
        } else {
            MatrixFabButton(
                iconResId = R.drawable.ic_menu,
                contentDescription = stringResource(R.string.menu),
                onClick = onToggle
            )
        }
    }
}

@Composable
private fun MatrixFabButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconSize: Int = 36
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(64.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}
