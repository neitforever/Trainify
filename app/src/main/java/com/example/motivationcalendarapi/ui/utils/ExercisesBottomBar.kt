package com.example.motivationcalendarapi.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExercisesBottomBar(pagerState: PagerState) {
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        Tab(
            selected = pagerState.currentPage == 0,
            onClick = { /* handled by pager */ },
            text = {
                Text(
                    text = "All Exercises",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        )
        Tab(
            selected = pagerState.currentPage == 1,
            onClick = { /* handled by pager */ },
            text = {
                Text(
                    text = "Favorites",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        )
    }
}