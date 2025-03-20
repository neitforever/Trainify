package com.motivationcalendar.ui

import LoadingView
import Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import formatTime
import formatTimestamp
import getWeekOfMonth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryDetailScreen(
    workoutId: Long?,
    viewModel: WorkoutViewModel,
    navController: NavController,
    drawerState: DrawerState
) {
    val workouts = viewModel.allWorkouts.collectAsState().value
    val workoutIndex = workouts.indexOfFirst { it.id == workoutId } + 1 // Номер тренировки
    val isLoading = viewModel.isLoadingWorkout.collectAsState().value

    val selectedWorkout = remember { mutableStateOf<Workout?>(null) }
    val newWorkoutName = remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    val workoutMonthYear =
        selectedWorkout.value?.timestamp?.let { formatTimestamp(it) } ?: "Unknown"


    LaunchedEffect(workoutId) {
        workoutId?.let { id ->
            CoroutineScope(Dispatchers.Default).launch {
                val workout = viewModel.getWorkoutById(id)
                selectedWorkout.value = workout
                newWorkoutName.value = workout?.name ?: ""
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()



    Scaffold(topBar = {
        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ), navigationIcon = {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        drawerState.open()
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
        }, title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
            ) {
                Text(
                    text = "$workoutIndex workout in $workoutMonthYear",
                    style = MaterialTheme.typography.displaySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }, modifier = Modifier.border(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                shape = CutCornerShape(4.dp)
            )
        )
    }, floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .navigationBarsPadding()
            ) {

                FloatingActionButton(
                    onClick = {
                        selectedWorkout.value?.let {
                            viewModel.deleteWorkout(it)
                            navController.navigate(Screen.WorkoutHistory.route) {
                                popUpTo(Screen.WorkoutHistory.route) { inclusive = true }
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete_outline),
                        contentDescription = "Delete",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                FloatingActionButton(
                    onClick = {
                        selectedWorkout.value?.let {
                            viewModel.updateWorkout(it.copy(name = newWorkoutName.value))
                            navController.popBackStack()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_save),
                        contentDescription = "Save",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

    },
//        bottomBar = {
//        BottomAppBar {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Button(onClick = {
//                    selectedWorkout.value?.let {
//                        viewModel.deleteWorkout(it)
//                        navController.navigate(Screen.WorkoutHistory.route) {
//                            popUpTo(Screen.WorkoutHistory.route) { inclusive = true }
//                        }
//                    }
//                }, modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = "Delete",
//                        style = MaterialTheme.typography.labelMedium,
//                        color = MaterialTheme.colorScheme.onPrimary
//                    )
//                }
//                Spacer(modifier = Modifier.width(16.dp))
//                Button(onClick = {
//                    selectedWorkout.value?.let {
//                        viewModel.updateWorkout(it.copy(name = newWorkoutName.value))
//                        navController.popBackStack()
//                    }
//                }, modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = "Save",
//                        style = MaterialTheme.typography.labelMedium,
//                        color = MaterialTheme.colorScheme.onPrimary
//                    )
//                }
//            }
//        }
//    }
    ) { paddingValues ->
        if (selectedWorkout.value != null) {
            Column(
                modifier = Modifier
                    .padding(16.dp, top = 0.dp)
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = newWorkoutName.value,
                    onValueChange = { newWorkoutName.value = it },
                    label = { Text("Workout name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                )
                Spacer(modifier = Modifier.height(8.dp))

                selectedWorkout.value?.let { workout ->
                    Text("Duration: ${formatTime(workout.duration)}")
                    Text("${getWeekOfMonth(workout.timestamp)} week in month")
                    Text("Workout in week $workoutIndex")
                    Spacer(modifier = Modifier.height(16.dp))

                    workout.exercises.forEachIndexed { index, extendedExercise ->
                        val exercise = extendedExercise.exercise
                        val sets = extendedExercise.sets

                        if (sets.isNotEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth()) {}
                            Text(
                                text = "${index + 1}. ${exercise.name}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Set",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Rep",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Weight",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }

                        sets.forEachIndexed { setIndex, set ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${setIndex + 1}",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${set.rep}",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${set.weight}",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LoadingView()
        }
    }
}














