package com.example.motivationcalendarapi.ui.settings.language_settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.settings.language_settings.fragments.LanguageCard
import com.example.motivationcalendarapi.viewmodel.MainViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun LanguageSettingsScreen(
    paddingValues: Dp, mainViewModel: MainViewModel, navController: NavController
) {
    val context = LocalContext.current
    val currentLanguage = mainViewModel.getSavedLanguageCode()

    val isRussianSelected = currentLanguage == "ru"
    val isEnglishSelected = currentLanguage == "en"
    val isBelarusianSelected = currentLanguage == "be"


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .padding(top = paddingValues)
    ) {
        LanguageCard(
            title = stringResource(R.string.russian),
            languageCode = "ru",
            isSelected = isRussianSelected,
            onClick = {
                mainViewModel.setLanguage(languageCode = "ru", context = context)
                navController.popBackStack()
            })

        LanguageCard(
            title = stringResource(R.string.english),
            languageCode = "en",
            isSelected = isEnglishSelected,
            onClick = {
                mainViewModel.setLanguage(languageCode = "en", context = context)
                navController.popBackStack()
            })

        LanguageCard(
            title = stringResource(R.string.belarusian),
            languageCode = "be",
            isSelected = isBelarusianSelected,
            onClick = {
                mainViewModel.setLanguage(languageCode = "be", context = context)
                navController.popBackStack()
            })
    }
}


