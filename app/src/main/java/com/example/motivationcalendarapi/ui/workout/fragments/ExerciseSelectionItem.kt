package com.example.motivationcalendarapi.ui.workout.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import java.util.Locale

@Composable
fun ExerciseSelectionItem(
    exercise: Exercise,
    isFavorite: Boolean,
    selectedOrder: Int?,
    onItemClick: () -> Unit,
    lang: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = if (selectedOrder != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedOrder?.toString() ?: exercise.getName(lang).first().uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = if (selectedOrder != null) MaterialTheme.colorScheme.background
                else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = exercise.getName(lang).replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            painter = painterResource(
                id = if (isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            ),
            contentDescription = stringResource(R.string.favorite),
            modifier = Modifier.size(24.dp),
            tint = if (isFavorite) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondary
        )
    }
}