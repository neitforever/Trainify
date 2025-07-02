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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.DifficultyLevel
import formatDate
import java.util.Locale

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
                    .padding(top = 12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
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
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))


                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = screen.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp).padding(start = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = stringResource(R.string.navigate),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}