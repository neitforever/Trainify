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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.motivationcalendarapi.database.WorkoutDatabase
import com.example.motivationcalendarapi.repositories.BodyProgressFirestoreRepository
import com.example.motivationcalendarapi.repositories.BodyProgressRepository
import com.example.motivationcalendarapi.repositories.ExerciseFirestoreRepository
import com.example.motivationcalendarapi.repositories.ExerciseRepository
import com.example.motivationcalendarapi.repositories.MainRepository
import com.example.motivationcalendarapi.repositories.TemplateFirestoreRepository
import com.example.motivationcalendarapi.repositories.TimerDataStore
import com.example.motivationcalendarapi.repositories.WorkoutFirestoreRepository
import com.example.motivationcalendarapi.repositories.WorkoutRepository
import com.example.motivationcalendarapi.ui.theme.MotivationCalendarAPITheme
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import com.example.motivationcalendarapi.viewmodel.BodyProgressViewModel
import com.example.motivationcalendarapi.viewmodel.BodyProgressViewModelFactory
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.MainViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutSettingsViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutSettingsViewModelFactory
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings

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
            val auth = FirebaseAuth.getInstance()
            val workoutFirestoreRepo = WorkoutFirestoreRepository()
            val bodyProgressFirestoreRepo = BodyProgressFirestoreRepository()
            val templateFirestoreRepo = TemplateFirestoreRepository()
            val exerciseFirestoreRepo = ExerciseFirestoreRepository()
            val firestore = remember { FirebaseFirestore.getInstance() }
            firestore.firestoreSettings = firestoreSettings{}

            val workoutRepository = WorkoutRepository(
                db,
                workoutFirestoreRepo,
                templateFirestoreRepo,
                exerciseFirestoreRepo,
                auth
            )
            val exerciseRepository = ExerciseRepository(
                db,
                exerciseFirestoreRepo,
                FirebaseAuth.getInstance()
            )


            val mainRepository = MainRepository(context)
            val bodyProgressRepository = BodyProgressRepository(db, bodyProgressFirestoreRepo, auth)

            val timerDataStore by lazy { TimerDataStore(applicationContext) }
            val bodyProgressViewModel: BodyProgressViewModel = viewModel(
                factory = BodyProgressViewModelFactory(bodyProgressRepository)
            )
            val workoutViewModel: WorkoutViewModel = viewModel(
                factory = WorkoutViewModelFactory(
                    workoutRepository, timerDataStore, mainRepository
                )
            )

            val workoutSettingsViewModel: WorkoutSettingsViewModel = viewModel(
                factory = WorkoutSettingsViewModelFactory(mainRepository)
            )


            val exerciseViewModel = ExerciseViewModel(
                exerciseRepository, auth
            )
            val mainViewModel = MainViewModel(mainRepository)

            val navController = rememberNavController()
            var drawerState = mutableStateOf(rememberDrawerState(initialValue = DrawerValue.Closed))

            val googleAuthClient = GoogleAuthClient(context = this)
            val authViewModel = AuthViewModel(googleAuthClient, bodyProgressRepository, workoutRepository)
            val userState = authViewModel.userState.collectAsState()

            LaunchedEffect(Unit) {
                if (userState.value is AuthViewModel.UserState.Authenticated) {
                    workoutViewModel.syncAllData()
                }
            }

            MotivationCalendarAPITheme(mainViewModel = mainViewModel) {
//                if (userState.value is AuthViewModel.UserState.Authenticated) {


                    NavGraph(
                        navHostController = navController,
                        navController = navController,
                        workoutViewModel,
                        exerciseViewModel,
                        drawerState,
                        authViewModel,
                        bodyProgressViewModel = bodyProgressViewModel,
                        workoutSettingsViewModel = workoutSettingsViewModel,
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



