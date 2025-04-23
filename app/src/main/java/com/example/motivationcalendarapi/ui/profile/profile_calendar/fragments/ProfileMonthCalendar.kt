package com.example.motivationcalendarapi.ui.profile.profile_calendar.fragments

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.model.DifficultyLevel
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun ProfileMonthCalendar(
    month: YearMonth,
    workoutDifficulties: Map<LocalDate, DifficultyLevel>,
    modifier: Modifier = Modifier
) {
    val currentDate = LocalDate.now()
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.atDay(1)
    val startOffset = (firstDayOfMonth.dayOfWeek.value - 1 )% 7
    val days = List(6 * 7) { index ->
        if (index >= startOffset && index < startOffset + daysInMonth) {
            index - startOffset + 1
        } else {
            null
        }
    }

    Column(modifier = modifier.padding(4.dp)) {
        Text(
            text = "${month.month.name.take(3).uppercase()} ${month.year.toString().takeLast(2)}",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
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