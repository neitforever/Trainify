package com.example.motivationcalendarapi

import GoogleAuthClient
import NavGraph
import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.example.motivationcalendarapi.notifications.NotificationConstants
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.motivationcalendarapi.database.WorkoutDatabase
import com.example.motivationcalendarapi.repositories.BodyProgressFirestoreRepository
import com.example.motivationcalendarapi.repositories.BodyProgressRepository
import com.example.motivationcalendarapi.repositories.ExerciseFirestoreRepository
import com.example.motivationcalendarapi.repositories.ExerciseRepository
import com.example.motivationcalendarapi.notifications.NotificationHelper
import com.example.motivationcalendarapi.notifications.NotificationScheduler
import com.example.motivationcalendarapi.repositories.NotificationSettingsDataStore
import com.example.motivationcalendarapi.viewmodel.NotificationSettingsViewModel
import com.example.motivationcalendarapi.viewmodel.NotificationSettingsViewModelFactory
import com.example.motivationcalendarapi.repositories.MainRepository
import com.example.motivationcalendarapi.repositories.TemplateFirestoreRepository
import com.example.motivationcalendarapi.utils.ClearFocusOnKeyboardDismiss
import com.example.motivationcalendarapi.repositories.TimerDataStore
import com.example.motivationcalendarapi.repositories.WorkoutFirestoreRepository
import com.example.motivationcalendarapi.repositories.WorkoutRepository
import com.example.motivationcalendarapi.repositories.technique.ExerciseTechniqueVideoRepository
import com.example.motivationcalendarapi.repositories.ai.ExerciseSelectionSuggestionRepository
import com.example.motivationcalendarapi.repositories.analysis.ExerciseAnalysisRepository
import com.example.motivationcalendarapi.ui.theme.MotivationCalendarAPITheme
import com.example.motivationcalendarapi.viewmodel.AiExerciseGenerationViewModel
import com.example.motivationcalendarapi.viewmodel.AiTemplateGenerationViewModel
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import com.example.motivationcalendarapi.viewmodel.BodyProgressViewModel
import com.example.motivationcalendarapi.viewmodel.BodyProgressViewModelFactory
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.analysis.ExerciseAnalysisViewModel
import com.example.motivationcalendarapi.viewmodel.analysis.ExerciseAnalysisViewModelFactory
import com.example.motivationcalendarapi.viewmodel.EquipmentRecognitionViewModel
import com.example.motivationcalendarapi.viewmodel.MainViewModel
import com.example.motivationcalendarapi.viewmodel.MainViewModelFactory
import com.example.motivationcalendarapi.viewmodel.WorkoutSettingsViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutSettingsViewModelFactory
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings

class MainActivity : ComponentActivity() {

    private var notificationDestination by mutableStateOf<String?>(null)

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannels(this)
        notificationDestination = intent.getStringExtra(NotificationConstants.EXTRA_DESTINATION)
        NotificationScheduler.scheduleWorkoutReminder(this)
        NotificationScheduler.scheduleWeightProgressReminder(this)


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
            val notificationSettingsDataStore = NotificationSettingsDataStore(applicationContext)
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

            val notificationSettingsViewModel: NotificationSettingsViewModel = viewModel(
                factory = NotificationSettingsViewModelFactory(notificationSettingsDataStore, applicationContext)
            )


            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(applicationContext)
            )

            val languageCode = mainViewModel.getSavedLanguageCode() ?: "en"
            mainViewModel.setLanguage(languageCode, this)

            val exerciseRepository = ExerciseRepository(
                db,
                exerciseFirestoreRepo,
                FirebaseAuth.getInstance()
            )
            Log.d("languageCode", languageCode)

            val lang = when (languageCode) {
                "ru" -> "ru"
                "be" -> "be"
                else -> "en"
            }
            val exerciseTechniqueVideoRepository = ExerciseTechniqueVideoRepository(db)
            val exerciseSelectionSuggestionRepository = ExerciseSelectionSuggestionRepository()
            val exerciseViewModel = ExerciseViewModel(
                exerciseRepository,
                exerciseTechniqueVideoRepository,
                exerciseSelectionSuggestionRepository
            )
            val exerciseAnalysisRepository = ExerciseAnalysisRepository(
                workoutRepository = workoutRepository,
                bodyProgressRepository = bodyProgressRepository
            )
            val exerciseAnalysisViewModel: ExerciseAnalysisViewModel = viewModel(
                factory = ExerciseAnalysisViewModelFactory(exerciseAnalysisRepository)
            )
            val equipmentRecognitionViewModel: EquipmentRecognitionViewModel = viewModel()
            val aiExerciseGenerationViewModel: AiExerciseGenerationViewModel = viewModel()
            val aiTemplateGenerationViewModel: AiTemplateGenerationViewModel = viewModel()
            val navController = rememberNavController()
            val currentNotificationDestination = notificationDestination
            var drawerState = mutableStateOf(rememberDrawerState(initialValue = DrawerValue.Closed))


            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) {}

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !NotificationHelper.hasNotificationPermission(context)
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }


            val googleAuthClient = remember { GoogleAuthClient(context = this@MainActivity) }
            val authViewModel = remember(googleAuthClient) {
                AuthViewModel(googleAuthClient, bodyProgressRepository, workoutRepository)
            }
            val userState = authViewModel.userState.collectAsState()

            LaunchedEffect(currentNotificationDestination, userState.value) {
                if (currentNotificationDestination != null &&
                    userState.value is AuthViewModel.UserState.Authenticated
                ) {
                    navController.navigate(currentNotificationDestination) {
                        launchSingleTop = true
                        restoreState = true
                    }
                    notificationDestination = null
                }
            }

            LaunchedEffect(Unit) {
                if (userState.value is AuthViewModel.UserState.Authenticated) {
                    workoutViewModel.syncAllData()
                }


            }

            MotivationCalendarAPITheme(mainViewModel = mainViewModel) {
                    ClearFocusOnKeyboardDismiss()
                    NavGraph(
                    navHostController = navController,
                    navController = navController,
                    workoutViewModel,
                    exerciseViewModel,
                    exerciseAnalysisViewModel,
                    mainViewModel,
                    drawerState,
                    authViewModel,
                    bodyProgressViewModel = bodyProgressViewModel,
                    workoutSettingsViewModel = workoutSettingsViewModel,
                    equipmentRecognitionViewModel = equipmentRecognitionViewModel,
                    aiExerciseGenerationViewModel = aiExerciseGenerationViewModel,
                        aiTemplateGenerationViewModel = aiTemplateGenerationViewModel,
                        notificationSettingsViewModel = notificationSettingsViewModel,
                        lang = lang
                    )
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationDestination = intent.getStringExtra(NotificationConstants.EXTRA_DESTINATION)
    }

    override fun onStart() {
        super.onStart()
    }
}