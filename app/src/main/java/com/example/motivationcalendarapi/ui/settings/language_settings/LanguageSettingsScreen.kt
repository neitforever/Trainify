package com.example.motivationcalendarapi.ui.settings.language_settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.settings.language_settings.fragments.LanguageCard
import com.example.motivationcalendarapi.viewmodel.MainViewModel

@Composable
fun LanguageSettingsScreen(
    paddingValues: Dp,
    mainViewModel: MainViewModel,
) {
    val currentLanguage = mainViewModel.appLanguage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LanguageCard(
            title = stringResource(R.string.system_default),
            languageCode = "system",
            isSelected = currentLanguage == "system",
            onClick = { mainViewModel.changeLanguage("system") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LanguageCard(
            title = stringResource(R.string.russian),
            languageCode = "ru",
            isSelected = currentLanguage == "ru",
            onClick = { mainViewModel.changeLanguage("ru") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LanguageCard(
            title = stringResource(R.string.english),
            languageCode = "en",
            isSelected = currentLanguage == "en",
            onClick = { mainViewModel.changeLanguage("en") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LanguageCard(
            title = stringResource(R.string.belarusian),
            languageCode = "be",
            isSelected = currentLanguage == "be",
            onClick = { mainViewModel.changeLanguage("be") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(
                R.string.current_language,
                when (currentLanguage) {
                    "ru" -> stringResource(R.string.russian)
                    "en" -> stringResource(R.string.english)
                    "be" -> stringResource(R.string.belarusian)
                    else -> stringResource(R.string.system_default)
                }
            ),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
