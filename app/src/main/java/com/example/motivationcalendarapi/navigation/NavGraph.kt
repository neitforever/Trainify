import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.example.motivationcalendarapi.ui.exercise.ai.AiTemplateGeneratorScreen
import com.example.motivationcalendarapi.ui.exercise.ai.AiExerciseGeneratorScreen
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.AuthScreen
import com.example.motivationcalendarapi.ui.body_progress.BodyProgressScreen
import com.example.motivationcalendarapi.ui.exercise.BodyPartSelectionScreen
import com.example.motivationcalendarapi.ui.exercise.CreateExerciseScreen
import com.example.motivationcalendarapi.ui.exercise.EditExerciseInstructionsScreen
import com.example.motivationcalendarapi.ui.exercise.EditExerciseNameScreen
import com.example.motivationcalendarapi.ui.exercise.EquipmentSelectionScreen
import com.example.motivationcalendarapi.ui.exercise.ExerciseDetailScreen
import com.example.motivationcalendarapi.ui.exercise.SearchExerciseScreen
import com.example.motivationcalendarapi.ui.equipment_recognition.EquipmentRecognitionScreen
import com.example.motivationcalendarapi.ui.fragments.NavigationMenuView
import com.example.motivationcalendarapi.ui.profile.ProfileScreen
import com.example.motivationcalendarapi.ui.profile.rewards.RewardUnlockedOverlay
import com.example.motivationcalendarapi.ui.settings.notification_settings.NotificationSettingsScreen
import com.example.motivationcalendarapi.ui.settings.permission_settings.PermissionSettingsScreen
import com.example.motivationcalendarapi.viewmodel.NotificationSettingsViewModel
import com.example.motivationcalendarapi.ui.settings.SettingsScreen
import com.example.motivationcalendarapi.ui.settings.language_settings.LanguageSettingsScreen
import com.example.motivationcalendarapi.ui.settings.theme_settings.ThemeSettingsScreen
import com.example.motivationcalendarapi.ui.settings.workout_settings.WorkoutSettingsScreen
import com.example.motivationcalendarapi.ui.template.EditTemplateNameScreen
import com.example.motivationcalendarapi.ui.template.TemplateDetailScreen
import com.example.motivationcalendarapi.viewmodel.AiExerciseGenerationViewModel
import com.example.motivationcalendarapi.viewmodel.AiTemplateGenerationViewModel
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import com.example.motivationcalendarapi.viewmodel.BodyProgressViewModel
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.EquipmentRecognitionViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutSettingsViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.example.motivationcalendarapi.ui.workout.WorkoutScreen
import com.example.motivationcalendarapi.ui.workout.detail.WorkoutHistoryDetailScreen
import com.example.motivationcalendarapi.viewmodel.MainViewModel
import com.motivationcalendar.ui.WorkoutHistoryScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavGraph(
    navHostController: NavHostController,
    navController: NavController,
    workoutViewModel: WorkoutViewModel,
    exerciseViewModel: ExerciseViewModel,
    mainViewModel: MainViewModel,
    drawerState: MutableState<DrawerState>,
    authViewModel: AuthViewModel,
    bodyProgressViewModel: BodyProgressViewModel,
    workoutSettingsViewModel: WorkoutSettingsViewModel,
    equipmentRecognitionViewModel: EquipmentRecognitionViewModel,
    aiExerciseGenerationViewModel: AiExerciseGenerationViewModel,
    aiTemplateGenerationViewModel: AiTemplateGenerationViewModel,
    notificationSettingsViewModel: NotificationSettingsViewModel,
    lang: String
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val currentDestination = currentBackStackEntry?.destination
    val currentRoute = currentDestination?.route?.split("/")?.get(0)
    val coroutineScope = rememberCoroutineScope()
    var showCreateMenu by remember { mutableStateOf(false) }
    val rewards = workoutViewModel.rewards.collectAsState()
    val pendingRewardUnlockEvents = workoutViewModel.pendingRewardUnlockEvents.collectAsState()


    val userState = authViewModel.userState.collectAsState()
    val screens = listOf(
        Screen.WorkoutHistory,
        Screen.WorkoutDetail,
        Screen.ExercisesView,
        Screen.EquipmentRecognizer,
        Screen.AiExerciseGeneration,
        Screen.AiTemplateGeneration,
        Screen.Settings,
        Screen.ThemeSettings,
        Screen.WorkoutSettings,
        Screen.Auth,
        Screen.BodyProgress,
        Screen.TemplateDetailView,
        Screen.Profile,
        Screen.LanguageSettings,
        Screen.NotificationSettings,
        Screen.PermissionSettings
    )
    ModalNavigationDrawer(drawerState = drawerState.value, drawerContent = {
        if (userState.value is AuthViewModel.UserState.Authenticated) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .fillMaxHeight()
                    .fillMaxWidth(0.8f)
            ) {
                NavigationMenuView(
                    authViewModel = authViewModel, navController = navController, onItemClick = {
                        coroutineScope.launch {
                            drawerState.value.close()
                        }
                    })
            }
        }

    }) {
        if (showCreateMenu) {
            ModalBottomSheet(
                onDismissRequest = { showCreateMenu = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CreateActionCard(
                        title = context.getString(R.string.create_exercise_manually),
                        description = context.getString(R.string.create_exercise_manually_description),
                        iconRes = R.drawable.ic_add,
                        onClick = {
                            showCreateMenu = false
                            navController.navigate(Screen.CreateExercise.route)
                        }
                    )
                    CreateActionCard(
                        title = context.getString(R.string.create_ai_exercise),
                        description = context.getString(R.string.create_ai_exercise_description),
                        iconRes = R.drawable.ic_dumbbell,
                        onClick = {
                            showCreateMenu = false
                            navController.navigate(Screen.AiExerciseGeneration.route)
                        }
                    )
                    CreateActionCard(
                        title = context.getString(R.string.create_ai_template),
                        description = context.getString(R.string.create_ai_template_description),
                        iconRes = R.drawable.ic_template,
                        onClick = {
                            showCreateMenu = false
                            navController.navigate(Screen.AiTemplateGeneration.route)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Scaffold(
            topBar = {
                screens.forEach { it ->

                    if (it.route == currentRoute && it.route != Screen.Auth.route) {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            navigationIcon = {
                                when (it.route) {
                                    Screen.EquipmentSelection.route -> {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                                contentDescription = "Back",
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }

                                    }

                                    Screen.BodyPartSelection.route -> {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                                contentDescription = "Back",
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }

                                    }

                                    else -> {
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    drawerState.value.open()
                                                }
                                            }, modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_menu),
                                                contentDescription = "Open Menu",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(42.dp)
                                            )
                                        }
                                    }
                                }

                            },
                            title = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp)
                                ) {
                                    Text(
                                        text = if (it.route == Screen.EquipmentRecognizer.route) context.getString(R.string.equipment_recognizer_short) else it.getTitle(context),
                                        style = MaterialTheme.typography.displaySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            modifier = Modifier.border(
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                                shape = CutCornerShape(4.dp)
                            ),
                            actions = {
                                when (it.route) {
                                    Screen.ExerciseDetailView.route -> {
                                        Row {
                                            IconButton(onClick = { navController.popBackStack() }) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_close),
                                                    contentDescription = "Close",
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .align(Alignment.CenterVertically),
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    Screen.ExercisesView.route -> {
                                        Row {
                                            IconButton(onClick = { navController.navigate(Screen.SearchExercise.route) }) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_search),
                                                    contentDescription = "Search",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }

                            },

                            )
                    }
                }
            },
            floatingActionButton = {
                screens.forEach { it ->
                    if (it.route == currentRoute) {
                        when (it.route) {

                            Screen.ExercisesView.route -> {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.navigationBarsPadding()
                                ) {
                                    FloatingActionButton(
                                        onClick = { showCreateMenu = true },
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_add),
                                            contentDescription = "Add new exercise",
                                            modifier = Modifier.size(36.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
//        bottomBar = {
//            if (currentDestination?.route == Screen.ExercisesView.route) {
//                ExercisesBottomBar(pagerState)
//            }
//        }

        ) { paddingValue ->

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                NavHost(
                    navController = navHostController,
                    startDestination = when (userState.value is AuthViewModel.UserState.Authenticated) {
                        true -> Screen.AddWorkout.route
                        else -> Screen.Auth.route
                    }
                ) {

                    composable(Screen.AddWorkout.route) {
                        WorkoutScreen(
                            workoutViewModel,
                            exerciseViewModel,
                            navController,
                            drawerState,
                            lang = lang,
                            context = context
                        )
                    }
                    composable(Screen.WorkoutHistory.route) {

                        WorkoutHistoryScreen(
                            workoutViewModel,
                            navController,
                            paddingValues = paddingValue.calculateTopPadding()
                        )
                    }

                    composable(Screen.ExercisesView.route) {

                        ExerciseScreen(
                            navController,
                            exerciseViewModel,
                            workoutViewModel,
                            paddingTopValues = paddingValue.calculateTopPadding(),
                            lang = lang
                        )
                    }

                    composable(
                        Screen.TemplateDetailView.route + "/{templateId}",
                        arguments = listOf(navArgument("templateId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val templateId = backStackEntry.arguments?.getString("templateId")
                        TemplateDetailScreen(
                            templateId = templateId,
                            navController = navController,
                            paddingTopValues = paddingValue.calculateTopPadding(),
                            workoutViewModel = workoutViewModel,
                            exerciseViewModel = exerciseViewModel,
                            lang = lang
                        )
                    }

                    composable(Screen.Auth.route) {
                        AuthScreen(
                            authViewModel,
                            navController = navHostController,
                            drawerState
                        )
                    }

                    composable(
                        Screen.EditTemplateName.route + "/{templateId}",
                        arguments = listOf(navArgument("templateId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val templateId = backStackEntry.arguments?.getString("templateId")
                        EditTemplateNameScreen(
                            navController = navController,
                            templateId = templateId ?: "",
                            viewModel = workoutViewModel,
                            lang = lang
                        )
                    }

                    composable(Screen.SearchExercise.route) {
                        SearchExerciseScreen(
                            navController = navHostController,
                            viewModel = exerciseViewModel,
                            lang = lang
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            authViewModel,
                            navController,
                            workoutViewModel = workoutViewModel,
                            paddingValues = paddingValue.calculateTopPadding()
                        )
                    }

                    composable(Screen.EquipmentRecognizer.route) {
                        EquipmentRecognitionScreen(
                            navController = navController,
                            exerciseViewModel = exerciseViewModel,
                            recognitionViewModel = equipmentRecognitionViewModel,
                            workoutViewModel = workoutViewModel,
                            paddingTopValues = paddingValue.calculateTopPadding(),
                            lang = lang
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            navController,
                            paddingValues = paddingValue.calculateTopPadding()
                        )
                    }

                    composable(Screen.ThemeSettings.route) {
                        ThemeSettingsScreen(
                            context = context,
                            paddingValues = paddingValue.calculateTopPadding(),
                            mainViewModel = mainViewModel
                        )
                    }

                    composable(Screen.LanguageSettings.route) {
                        LanguageSettingsScreen(
                            mainViewModel = mainViewModel,
                            paddingValues = paddingValue.calculateTopPadding(),
                            navController = navController
                        )
                    }

                    composable(Screen.WorkoutSettings.route) {
                        WorkoutSettingsScreen(
                            viewModel = workoutSettingsViewModel,
                            paddingValues = paddingValue.calculateTopPadding()
                        )
                    }

                    composable(Screen.NotificationSettings.route) {
                        NotificationSettingsScreen(
                            viewModel = notificationSettingsViewModel,
                            paddingValues = paddingValue.calculateTopPadding()
                        )
                    }

                    composable(Screen.PermissionSettings.route) {
                        PermissionSettingsScreen(
                            paddingValues = paddingValue.calculateTopPadding()
                        )
                    }


                    composable(Screen.CreateExercise.route) {
                        CreateExerciseScreen(
                            navController = navHostController,
                            exerciseViewModel = exerciseViewModel,
                            lang = lang
                        )
                    }


                    composable(Screen.AiExerciseGeneration.route) {
                        AiExerciseGeneratorScreen(
                            navController = navHostController,
                            exerciseViewModel = exerciseViewModel,
                            workoutViewModel = workoutViewModel,
                            aiExerciseGenerationViewModel = aiExerciseGenerationViewModel,
                            paddingTopValues = paddingValue.calculateTopPadding(),
                            lang = lang
                        )
                    }

                    composable(Screen.AiTemplateGeneration.route) {
                        AiTemplateGeneratorScreen(
                            navController = navHostController,
                            exerciseViewModel = exerciseViewModel,
                            workoutViewModel = workoutViewModel,
                            aiTemplateGenerationViewModel = aiTemplateGenerationViewModel,
                            paddingTopValues = paddingValue.calculateTopPadding(),
                            lang = lang
                        )
                    }

                    composable(
                        Screen.EditExerciseName.route + "/{exerciseId}",
                        arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val exerciseId = backStackEntry.arguments?.getString("exerciseId")
                        EditExerciseNameScreen(
                            navController = navHostController,
                            exerciseId = exerciseId ?: "",
                            viewModel = exerciseViewModel,
                            lang = lang
                        )
                    }




                    composable(
                        Screen.BodyPartSelection.route + "/{exerciseId}",
                        arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val exerciseId = backStackEntry.arguments?.getString("exerciseId")
                        BodyPartSelectionScreen(
                            navController = navHostController,
                            exerciseId = exerciseId ?: "",
                            viewModel = exerciseViewModel,
                            paddingValues = paddingValue.calculateTopPadding(),
                            lang = lang
                        )
                    }


                    composable(
                        Screen.EquipmentSelection.route + "/{exerciseId}",
                        arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val exerciseId = backStackEntry.arguments?.getString("exerciseId")
                        EquipmentSelectionScreen(
                            navController = navHostController,
                            exerciseId = exerciseId ?: "",
                            viewModel = exerciseViewModel,
                            lang = lang
                        )
                    }

//            composable(
//                Screen.SecondaryMusclesSelection.route + "/{exerciseId}",
//                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
//            ) { backStackEntry ->
//                val exerciseId = backStackEntry.arguments?.getString("exerciseId")
//                SecondaryMusclesSelectionScreen(
//                    navController = navHostController,
//                    exerciseId = exerciseId ?: "",
//                    viewModel = exerciseViewModel
//                )
//            }

                    composable(
                        Screen.EditExerciseInstructions.route + "/{exerciseId}",
                        arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val exerciseId = backStackEntry.arguments?.getString("exerciseId")
                        EditExerciseInstructionsScreen(
                            navController = navHostController,
                            exerciseId = exerciseId ?: "",
                            viewModel = exerciseViewModel,
                            lang = lang
                        )
                    }

                    composable(
                        Screen.WorkoutDetail.route + "/{workoutId}",
                        arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val workoutId = backStackEntry.arguments?.getString("workoutId")
                        WorkoutHistoryDetailScreen(
                            workoutId = workoutId,
                            workoutViewModel = workoutViewModel,
                            navController = navController,
                            paddingValues = paddingValue.calculateTopPadding(),
                            lang = lang
                        )
                    }
                    composable(Screen.BodyProgress.route) {
                        val context = LocalContext.current
                        BodyProgressScreen(
                            viewModel = bodyProgressViewModel,
                            workoutViewModel = workoutViewModel,
                            context = context,
                            paddingValues = paddingValue.calculateTopPadding()
                        )
                    }



                    composable(
                        Screen.ExerciseDetailView.route + "/{exerciseId}",
                        arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val exerciseId = backStackEntry.arguments?.getString("exerciseId")
                        ExerciseDetailScreen(
                            navController = navController,
                            exerciseId = exerciseId ?: "",
                            viewModel = exerciseViewModel,
                            lang = lang
                        )
                    }
                }

                RewardUnlockedOverlay(
                    events = pendingRewardUnlockEvents.value,
                    rewards = rewards.value,
                    onShown = { workoutViewModel.markRewardUnlockEventShown(it) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = paddingValue.calculateTopPadding() + 8.dp)
                )
            }
        }
    }
}



@Composable
private fun CreateActionCard(
    title: String,
    description: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
