package com.example.motivationcalendarapi.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.motivationcalendarapi.model.DifficultyLevel
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.ui.profile.profile_calendar.ProfileCalendarView
import com.example.motivationcalendarapi.ui.profile.profile_calendar.fragments.LegendItem
import com.example.motivationcalendarapi.ui.profile.profile_calendar.fragments.LegendRow
import com.example.motivationcalendarapi.ui.profile.profile_calendar.fragments.LogoutButton
import com.example.motivationcalendarapi.ui.profile.profile_calendar.fragments.ProfileHeader
import com.example.motivationcalendarapi.ui.profile.profile_calendar.fragments.StatsRow
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    workoutViewModel: WorkoutViewModel,
) {
    val userState = authViewModel.userState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val allWorkouts by workoutViewModel.allWorkouts.collectAsState()
    val totalReps by workoutViewModel.totalReps.collectAsState()
    val totalWeight by workoutViewModel.totalWeight.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.checkAuthState()
        workoutViewModel.loadWorkouts()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 64.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProfileHeader(authViewModel, modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp))
                StatsRow(allWorkouts, totalReps, totalWeight, modifier = Modifier
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

