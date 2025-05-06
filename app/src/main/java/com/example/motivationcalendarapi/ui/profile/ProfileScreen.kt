package com.example.motivationcalendarapi.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.ui.profile.profile_calendar.ProfileCalendarView
import com.example.motivationcalendarapi.ui.profile.fragments.LegendRow
import com.example.motivationcalendarapi.ui.profile.fragments.LogoutButton
import com.example.motivationcalendarapi.ui.profile.fragments.ProfileHeader
import com.example.motivationcalendarapi.ui.profile.fragments.StatsRow
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    workoutViewModel: WorkoutViewModel,
    paddingValues: Dp,
) {
    val userState = authViewModel.userState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val allWorkouts by workoutViewModel.allWorkouts.collectAsState()
    val weekReps by workoutViewModel.weekReps.collectAsState()
    val weekWeight by workoutViewModel.weekWeight.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.checkAuthState()
        workoutViewModel.loadWorkouts()
    }

    Box(modifier = Modifier.fillMaxSize().padding(top = paddingValues)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProfileHeader(authViewModel, modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp))
                StatsRow(allWorkouts, weekReps, weekWeight, modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 4.dp))
            }

            item {
                ProfileCalendarView(
                    workouts = allWorkouts,
                    workoutViewModel = workoutViewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .padding(horizontal = 4.dp)
                        .height(180.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = MaterialTheme.shapes.medium,
                            clip = true
                        )
                )
                LegendRow(modifier = Modifier.padding(top = 12.dp).padding(horizontal = 8.dp))
            }
        }

        LogoutButton(
            coroutineScope = coroutineScope,
            authViewModel = authViewModel,
            navController = navController,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
    }
}

