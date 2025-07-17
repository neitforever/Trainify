package com.example.motivationcalendarapi

import GoogleAuthClient
import NavGraph
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
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
import java.util.*

class MainActivity : ComponentActivity() {

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val db = WorkoutDatabase.getDatabase(context)
            val auth = FirebaseAuth.getInstance()
            val workoutFirestoreRepo = WorkoutFirestoreRepository()
            val bodyProgressFirestoreRepo = BodyProgressFirestoreRepository()
            val templateFirestoreRepo = TemplateFirestoreRepository()
            val exerciseFirestoreRepo = ExerciseFirestoreRepository()
            val firestore = remember { FirebaseFirestore.getInstance() }
            firestore.firestoreSettings = firestoreSettings {}

            val workoutRepository = WorkoutRepository(
                db,
                workoutFirestoreRepo,
                templateFirestoreRepo,
                exerciseFirestoreRepo,
                auth
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


            val mainViewModel = MainViewModel(mainRepository)

            val exerciseRepository = ExerciseRepository(
                db,
                exerciseFirestoreRepo,
                FirebaseAuth.getInstance()
            )

            val currentLanguage = mainViewModel.appLanguage
            val lang = when (currentLanguage) {
                "ru" -> "ru"
                "be" -> "be"
                else -> "en"
            }
            val exerciseViewModel = ExerciseViewModel(
                exerciseRepository, auth
            )
            val navController = rememberNavController()
            var drawerState = mutableStateOf(rememberDrawerState(initialValue = DrawerValue.Closed))

            val googleAuthClient = GoogleAuthClient(context = this)
            val authViewModel =
                AuthViewModel(googleAuthClient, bodyProgressRepository, workoutRepository)
            val userState = authViewModel.userState.collectAsState()

            LaunchedEffect(Unit) {
                if (userState.value is AuthViewModel.UserState.Authenticated) {
                    workoutViewModel.syncAllData()
                }

                mainViewModel.recreateActivity = {
                    recreate()
                }
            }


            MotivationCalendarAPITheme(mainViewModel = mainViewModel) {
                NavGraph(
                    navHostController = navController,
                    navController = navController,
                    workoutViewModel,
                    exerciseViewModel,
                    mainViewModel,
                    drawerState,
                    authViewModel,
                    bodyProgressViewModel = bodyProgressViewModel,
                    workoutSettingsViewModel = workoutSettingsViewModel,
                    lang = lang
                )
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE)
        val language = prefs.getString("app_language", "system") ?: "system"

        super.attachBaseContext(wrapContext(newBase, language))
    }

    private fun wrapContext(context: Context, languageCode: String): Context {
        if (languageCode == "system") {
            return context
        }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    override fun onStart() {
        super.onStart()
    }
}