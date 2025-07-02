package com.example.motivationcalendarapi.ui.settings.theme_settings.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R

@Composable
fun ColorPalettePreview(colors: ColorScheme, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ColorRow(name = stringResource(R.string.primary), color = colors.primary)
        ColorRow(name = stringResource(R.string.secondary), color = colors.secondary)
        ColorRow(name = stringResource(R.string.tertiary), color = colors.tertiary)
    }
}