import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.motivationcalendarapi.ui.body_progress.BodyProgressScreen
import com.example.motivationcalendarapi.viewmodel.BodyProgressViewModel
import com.example.motivationcalendarapi.ui.AuthScreen
import com.example.motivationcalendarapi.ui.ProfileScreen
import com.example.motivationcalendarapi.ui.exercise.BodyPartSelectionScreen
import com.example.motivationcalendarapi.ui.exercise.CreateExerciseScreen
import com.example.motivationcalendarapi.ui.exercise.EditExerciseInstructionsScreen
import com.example.motivationcalendarapi.ui.exercise.EditExerciseNameScreen
import com.example.motivationcalendarapi.ui.exercise.EquipmentSelectionScreen
import com.example.motivationcalendarapi.ui.exercise.ExerciseDetailScreen
import com.example.motivationcalendarapi.ui.exercise.ExerciseScreen
import com.example.motivationcalendarapi.ui.exercise.SearchExerciseScreen
import com.example.motivationcalendarapi.ui.SettingsScreen
import com.example.motivationcalendarapi.ui.ThemeSettingsScreen
import com.example.motivationcalendarapi.ui.fragments.NavigationMenuView
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.MainViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.motivationcalendar.ui.AddWorkoutScreen
import com.motivationcalendar.ui.WorkoutHistoryDetailScreen
import com.motivationcalendar.ui.WorkoutHistoryScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavGraph(
    navHostController: NavHostController,
    navController: NavController,
    workoutViewModel: WorkoutViewModel,
    mainViewModel: MainViewModel,
    exerciseViewModel: ExerciseViewModel,
    drawerState: MutableState<DrawerState>,
    googleAuthClient: GoogleAuthClient,
    authViewModel: AuthViewModel,
    bodyProgressViewModel: BodyProgressViewModel,
    ) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val currentDestination = currentBackStackEntry?.destination
    val currentRoute = currentDestination?.route?.split("/")?.get(0)
    val coroutineScope = rememberCoroutineScope()

//    var isSignIn = rememberSaveable {
//        mutableStateOf(googleAuthClient.isSingedIn())
//    }


    val userState = authViewModel.userState.collectAsState()
    // val pagerState = rememberPagerState(pageCount = { 2 })

    val screens = listOf(
        Screen.WorkoutHistory,
        Screen.ExercisesView,
        Screen.Settings,
        Screen.ThemeSettings,
        Screen.EquipmentSelection,
        Screen.BodyPartSelection,
        Screen.Auth,
        Screen.BodyProgress
    )
    ModalNavigationDrawer(drawerState = drawerState.value, drawerContent = {
        if (userState.value is AuthViewModel.UserState.Authenticated) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .fillMaxHeight()
                    .fillMaxWidth(0.7f)
            ) {
                NavigationMenuView(navController = navController, onItemClick = {
                    coroutineScope.launch {
                        drawerState.value.close()
                    }
                })
            }
        }

    }) {
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
                                            imageVector = Icons.Filled.Menu,
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
                                    text = it.title,
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
                                    onClick = { navController.navigate(Screen.CreateExercise.route) },
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

                        AddWorkoutScreen(
                            workoutViewModel, exerciseViewModel, navController, drawerState
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
                            paddingTopValues = paddingValue.calculateTopPadding(),
                        )
                    }

                    composable(Screen.Auth.route) {
                        AuthScreen(
                            authViewModel,
                            navController = navHostController,
                            drawerState
                        )
                    }

                    composable(Screen.SearchExercise.route) {
                        SearchExerciseScreen(
                            navController = navHostController, viewModel = exerciseViewModel
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            authViewModel,
                            navController,

                            )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            context,
                            navController,
                            paddingValues = paddingValue.calculateTopPadding()
                        )
                    }

                    composable(Screen.ThemeSettings.route) {
                        ThemeSettingsScreen(
                            context,
                            navController,
                            paddingValues = paddingValue.calculateTopPadding()
                        )
                    }


                    composable(Screen.CreateExercise.route) {
                        CreateExerciseScreen(
                            navController = navHostController,
                            viewModel = exerciseViewModel
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
                            viewModel = exerciseViewModel
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
                            paddingValues = paddingValue.calculateTopPadding()
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
                            paddingValues = paddingValue.calculateTopPadding()
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
                            viewModel = exerciseViewModel
                        )
                    }

                    composable(
                        Screen.WorkoutDetail.route + "/{workoutId}",
                        arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
                    ) { stackEntry ->
                        val workoutId = stackEntry.arguments?.getLong("workoutId")
                        WorkoutHistoryDetailScreen(
                            workoutId, workoutViewModel, navController,
                            paddingValues = paddingValue.calculateTopPadding()
                        )
                    }
                    composable(Screen.BodyProgress.route) {
                        val context = LocalContext.current
                        BodyProgressScreen(
                            viewModel = bodyProgressViewModel,
                            navController = navHostController,
                            context = context,
                            paddingValues = paddingValue.calculateTopPadding()
                        )
                    }

                    composable(
                        Screen.ExerciseDetailView.route + "/{exerciseId}",
                        arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
                    ) { stackEntry ->
                        val exerciseId = stackEntry.arguments?.getString("exerciseId")
                        if (exerciseId != null) {
                            ExerciseDetailScreen(
                                navController, exerciseId, exerciseViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

