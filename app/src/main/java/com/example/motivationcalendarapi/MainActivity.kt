package com.example.motivationcalendarapi

import GoogleAuthClient
import NavGraph
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.motivationcalendarapi.repositories.ExerciseRepository
import com.example.motivationcalendarapi.repositories.MainRepository
import com.example.motivationcalendarapi.repositories.TimerDataStore
import com.example.motivationcalendarapi.repositories.WorkoutRepository
import com.example.motivationcalendarapi.tryy.BodyProgressRepository
import com.example.motivationcalendarapi.tryy.BodyProgressViewModel
import com.example.motivationcalendarapi.tryy.BodyProgressViewModelFactory
import com.example.motivationcalendarapi.ui.theme.MotivationCalendarAPITheme
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.MainViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModelFactory
import com.motivationcalendar.data.WorkoutDatabase

class MainActivity : ComponentActivity() {
    // private lateinit var auth: FirebaseAuth
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val db = WorkoutDatabase.getDatabase(LocalContext.current)

            val workoutRepository = WorkoutRepository(db)
            val exerciseRepository = ExerciseRepository(db)
            val mainRepository = MainRepository(context)
            val timerDataStore by lazy { TimerDataStore(applicationContext) }
            val bodyProgressRepository = BodyProgressRepository(db)
            val bodyProgressViewModel: BodyProgressViewModel = viewModel(
                factory = BodyProgressViewModelFactory(bodyProgressRepository)
            )
            val workoutViewModel: WorkoutViewModel = viewModel(
                factory = WorkoutViewModelFactory(
                    workoutRepository, timerDataStore
                )
            )

            val exerciseViewModel = ExerciseViewModel(
                exerciseRepository
            )
            val mainViewModel = MainViewModel(mainRepository)

            val navController = rememberNavController()
            var drawerState = mutableStateOf(rememberDrawerState(initialValue = DrawerValue.Closed))
            val coroutineScope = rememberCoroutineScope()

            val googleAuthClient = GoogleAuthClient(context = this)
            val authViewModel = AuthViewModel(googleAuthClient)
            val userState = authViewModel.userState.collectAsState()

            MotivationCalendarAPITheme(mainViewModel = mainViewModel) {
//                if (userState.value is AuthViewModel.UserState.Authenticated) {

                    NavGraph(
                        navHostController = navController,
                        navController = navController,
                        workoutViewModel,
                        mainViewModel,
                        exerciseViewModel,
                        drawerState,
                        googleAuthClient,
                        authViewModel,
                        bodyProgressViewModel = bodyProgressViewModel,
                    )
//                }
//            else {
//                    AuthScreen(authViewModel, navController)
//                }

            }
        }
    }

    override fun onStart() {
        super.onStart()

    }
}




