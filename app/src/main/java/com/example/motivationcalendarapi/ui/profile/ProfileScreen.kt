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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
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
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
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

    val fs = Firebase.firestore
    val allWorkouts by workoutViewModel.allWorkouts.collectAsState()
    val totalWorkouts by workoutViewModel.allWorkouts.collectAsState()
    val totalReps by workoutViewModel.totalReps.collectAsState()
    val totalWeight by workoutViewModel.totalWeight.collectAsState()


    LaunchedEffect(Unit) {
        authViewModel.checkAuthState()
        workoutViewModel.loadWorkouts()
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 64.dp), horizontalAlignment = Alignment.CenterHorizontally
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

        Text(authViewModel.getCurrentUser()?.displayName.toString())
        Text(authViewModel.getCurrentUser()?.uid.toString())
        Text(authViewModel.getCurrentUser()?.email.toString())
        Text(authViewModel.getCurrentUser()?.phoneNumber.toString())
        Divider()

        ProfileCalendarView(
            workouts = allWorkouts,
            workoutViewModel = workoutViewModel,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem(DifficultyLevel.EASY, "Easy")
            LegendItem(DifficultyLevel.NORMAL, "Medium")
            LegendItem(DifficultyLevel.HARD, "Hard")
        }

        Text(
            text = "Total workouts: ${totalWorkouts.size}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(4.dp)
        )

        Text(
            text = "Total reps: ${totalReps}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(4.dp)
        )

        Text(
            text = "Total weight lifted: ${"%.1f".format(totalWeight)} kg",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(4.dp)
        )

        OutlinedButton(onClick = { navController.navigate(Screen.BodyProgress.route) }) {
            Text("Body Progress")
        }


        OutlinedButton(onClick = {
            coroutineScope.launch {
                authViewModel.signOut()
                navController.navigate(Screen.Auth.route)
            }
        }) {
            Text(
                text = "logout",
                fontSize = 16.sp,
                modifier = Modifier.padding(
                    horizontal = 24.dp, vertical = 4.dp
                )
            )

        }

        Spacer(modifier = Modifier.absolutePadding(bottom = 200.dp))

    }
}




