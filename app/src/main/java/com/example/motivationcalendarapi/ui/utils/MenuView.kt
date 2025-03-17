package com.example.motivationcalendarapi.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R

@Composable
fun NavigationMenuView(navController: NavController, onItemClick: () -> Unit) {
    val items = listOf(
        Screen.AddWorkout, Screen.WorkoutHistory, Screen.ExercisesView, Screen.Settings,Screen.Profile
    )

    Column(
        modifier = Modifier.padding(top = 16.dp, start = 8.dp)
    ) {
        Text(
            text = "Menu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp, top = 32.dp)
        )

        items.forEach { screen ->
            Row(modifier = Modifier
                .clickable {
                    onItemClick()
                    navController.navigate(screen.route) {
                        if (screen.route == Screen.AddWorkout.route) {
                            popUpTo(Screen.AddWorkout.route) { inclusive = true }
                        } else {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                val iconResId = when (screen.route) {
                    Screen.Settings.route -> R.drawable.ic_settings
                    Screen.WorkoutHistory.route -> R.drawable.ic_history
                    Screen.AddWorkout.route -> R.drawable.ic_add
                    Screen.ExercisesView.route -> R.drawable.ic_list
                    else -> null
                }

                if (iconResId != null) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = "${screen.title} Icon",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = screen.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
            }
        }
    }
}

