package com.example.motivationcalendarapi.ui.profile.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.model.DifficultyLevel
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR


@Composable
fun LegendItem(level: DifficultyLevel, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(
                    color = when (level) {
                        DifficultyLevel.EASY -> EASY_COLOR
                        DifficultyLevel.NORMAL -> NORMAL_COLOR
                        DifficultyLevel.HARD -> HARD_COLOR
                    },
                    shape = MaterialTheme.shapes.extraSmall
                )
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}