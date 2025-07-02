package com.example.motivationcalendarapi.ui.settings.theme_settings

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.settings.theme_settings.fragments.ColorPalettePreview
import com.example.motivationcalendarapi.ui.settings.theme_settings.fragments.ThemeCard
import com.example.motivationcalendarapi.ui.theme.DarkColorScheme
import com.example.motivationcalendarapi.ui.theme.LightColorScheme
import com.example.motivationcalendarapi.viewmodel.MainViewModel
import com.example.motivationcalendarapi.viewmodel.MainViewModelFactory

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ThemeSettingsScreen(
    context: Context,
    navController: NavController,
    paddingValues: Dp
) {
    val mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory(context))
    val isDarkTheme = mainViewModel.isDarkTheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ThemeCard(
                title = stringResource(R.string.light),
                colors = LightColorScheme,
                onClick = { mainViewModel.setTheme(false) }
            )

            Spacer(modifier = Modifier.width(16.dp))

            ThemeCard(
                title = stringResource(R.string.dark),
                colors = DarkColorScheme,
                onClick = { mainViewModel.setTheme(true) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.current_theme,
                    stringResource(if (isDarkTheme) R.string.dark else R.string.light)
                ),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            ColorPalettePreview(
                colors = if (isDarkTheme) DarkColorScheme else LightColorScheme,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedContent(
                targetState = isDarkTheme,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut()) using SizeTransform(clip = false)
                }
            ) { isDark ->
                if (isDark) {
                    Text(
                        text = stringResource(R.string.dark_theme_reduces_eye_strain_in_low_light_conditions),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(R.string.light_theme_provides_better_readability_in_bright_environments),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}







