package com.example.motivationcalendarapi.ui.fragments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.theme.BLACK_COLOR
import com.example.motivationcalendarapi.ui.theme.WHITE_COLOR
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import com.example.motivationcalendarapi.viewmodel.MainViewModel

@Composable
fun NavigationMenuView(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    navController: NavController,
    onItemClick: () -> Unit
) {
    val items = listOf(
        Screen.Profile, Screen.BodyProgress, Screen.AddWorkout,
        Screen.WorkoutHistory, Screen.ExercisesView, Screen.Settings,
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route?.split("/")?.get(0)

    val isDarkTheme = mainViewModel.isDarkTheme
    val color = if (isDarkTheme) BLACK_COLOR else WHITE_COLOR

    Column {
        if (authViewModel.getCurrentUser()?.email != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceDim)
                    .padding(top = 48.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(authViewModel.getCurrentUser()?.photoUrl)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mail),
                        contentDescription = "mail icon",
                        modifier = Modifier.size(28.dp),
                        tint = WHITE_COLOR
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = authViewModel.getCurrentUser()?.email.toString(),
                        color = WHITE_COLOR,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f).padding(bottom = 4.dp)
                    )
                }
            }
        }

        items.forEach { screen ->
            val isSelected = screen.route.split("/")[0] == currentRoute
            Row(
                modifier = Modifier
                    .clickable {
                        onItemClick()
                        navController.navigate(screen.route) {
                            if (screen.route == Screen.AddWorkout.route) {
                                popUpTo(Screen.AddWorkout.route) { inclusive = true }
                            } else {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    .padding(vertical = 8.dp, horizontal = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconResId = when (screen.route) {
                    Screen.Settings.route -> R.drawable.ic_settings
                    Screen.WorkoutHistory.route -> R.drawable.ic_history
                    Screen.AddWorkout.route -> R.drawable.ic_add_circle
                    Screen.ExercisesView.route -> R.drawable.ic_list
                    Screen.BodyProgress.route -> R.drawable.ic_progress
                    Screen.Profile.route -> R.drawable.ic_profile
                    else -> null
                }

                iconResId?.let {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = "${screen.title} Icon",
                        modifier = Modifier.size(28.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = screen.title,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}