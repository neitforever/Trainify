package com.example.motivationcalendarapi.ui.profile.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.DifficultyLevel

@Composable
fun LegendRow(modifier: Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(DifficultyLevel.EASY, stringResource(R.string.easy))
        LegendItem(DifficultyLevel.NORMAL, stringResource(R.string.medium))
        LegendItem(DifficultyLevel.HARD, stringResource(R.string.hard))
    }
    Spacer(modifier = Modifier.height(24.dp))
}