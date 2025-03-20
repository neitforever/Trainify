package com.example.motivationcalendarapi.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    drawerState: MutableState<androidx.compose.material3.DrawerState>
) {
    Log.d("asdasd","123456")

    val coroutineScope = rememberCoroutineScope()
//    LaunchedEffect(isSignIn) {
//        navController.navigate(Screen.AddWorkout.route)
//    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        OutlinedButton(onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                drawerState.value.close()
                authViewModel.signIn()
                drawerState.value.close()
            }
        }) {
            Text(
                text = "Sign In With Google",
                fontSize = 16.sp,
                modifier = Modifier.padding(
                    horizontal = 24.dp, vertical = 4.dp
                )
            )
        }

    }

}
