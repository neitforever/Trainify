package com.example.motivationcalendarapi.ui.exercise


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.exercise.fragments.ExerciseItem
import com.example.motivationcalendarapi.ui.exercise.fragments.NotFoundExerciseView
import com.example.motivationcalendarapi.ui.exercise.fragments.SearchBar
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchExerciseScreen(
    navController: NavController,
    viewModel: ExerciseViewModel,
    lang: String
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchExercises(searchQuery)
        .collectAsState(initial = emptyList())
    val favoriteExercises by viewModel.getFavoriteExercises().collectAsState(initial = emptyList())
    val listState = rememberLazyListState()
    val isScrolled = remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(topBar = {
        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ), title = {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding()
            )
        }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }, modifier = Modifier.border(
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
            shape = CutCornerShape(4.dp)
        ))

    }, floatingActionButton = {
        AnimatedVisibility(
            visible = isScrolled.value, enter = expandVertically(), exit = shrinkVertically()
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_up),
                    contentDescription = stringResource(R.string.scroll_to_top),
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }) { paddingValues ->
        if (searchResults.isEmpty()) {
            NotFoundExerciseView(searchQuery)
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                state = listState
            ) {
                items(searchResults) { exercise ->

                    ExerciseItem(exercise = exercise,
                        onItemClick = { navController.navigate("exercise_detail/${exercise.id}") },
                        onFavoriteClick = { viewModel.toggleFavorite(exercise) },
                        isFavorite = favoriteExercises.any { it.id == exercise.id },
                        lang = lang
                    )
                }
                item {
                    Spacer(
                        modifier = Modifier
                            .absolutePadding(bottom = 200.dp)
                    )
                }
            }
        }
    }
}

