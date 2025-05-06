package com.example.motivationcalendarapi.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    context: Context, navController: NavController, paddingValues: Dp
) {
    val items = listOf(
        Screen.ThemeSettings,
        Screen.WorkoutSettings,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .padding(top = paddingValues)
    ) {
        items.forEachIndexed { index, screen ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(screen.route) }
                    .padding(vertical = 12.dp),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.medium) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(
                                id = when (screen) {
                                    Screen.ThemeSettings -> R.drawable.ic_color_theme
                                    Screen.WorkoutSettings -> R.drawable.ic_dumbbell
                                    else -> R.drawable.ic_settings
                                }
                            ),
                            contentDescription = screen.title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = screen.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "Navigate",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (index != items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 0.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                )
            }
        }
    }
}