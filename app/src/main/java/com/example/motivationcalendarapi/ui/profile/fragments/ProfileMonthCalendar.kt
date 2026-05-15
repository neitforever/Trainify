package com.example.motivationcalendarapi.ui.profile.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.DifficultyLevel
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProfileMonthCalendar(
    month: YearMonth,
    workoutDifficulties: Map<LocalDate, DifficultyLevel>,
    modifier: Modifier = Modifier
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.atDay(1)
    val startOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7
    val days = List(6 * 7) { index ->
        if (index >= startOffset && index < startOffset + daysInMonth) {
            index - startOffset + 1
        } else {
            null
        }
    }


    val context = LocalContext.current
    val currentLocale = context.resources.configuration.locales[0] ?: Locale.getDefault()

    val monthName = remember(month, currentLocale) {
        month.month.getDisplayName(TextStyle.SHORT, currentLocale)
    }
    val formattedYear = remember(month) {
        (month.year % 100).toString().padStart(2, '0')
    }

    Column(modifier = modifier.padding(4.dp)) {
        Text(
            text = "${monthName.uppercase(currentLocale)} $formattedYear",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(
                stringResource(R.string.monday),
                stringResource(R.string.tuesday),
                stringResource(R.string.wednesday),
                stringResource(R.string.thursday),
                stringResource(R.string.friday),
                stringResource(R.string.saturday),
                stringResource(R.string.sunday)
            ).forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(days.size) { index ->
                val day = days[index]
                if (day != null) {
                    val date = firstDayOfMonth.plusDays(day.toLong() - 1)
                    val difficulty = workoutDifficulties[date]

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(
                                color = when (difficulty) {
                                    DifficultyLevel.HARD -> HARD_COLOR
                                    DifficultyLevel.NORMAL -> NORMAL_COLOR
                                    DifficultyLevel.EASY -> EASY_COLOR
                                    null -> MaterialTheme.colorScheme.surface
                                },
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                    )
                } else {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                }
            }
        }
    }
}