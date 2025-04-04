package com.example.motivationcalendarapi.ui

import GoogleAuthClient
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    navController: NavController,

    ) {
    val userState = authViewModel.userState.collectAsState()
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        authViewModel.checkAuthState()
    }
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {

//        var count by remember { mutableStateOf(0) }
//        val scope = rememberCoroutineScope()
//        var autoIncrementJob by remember { mutableStateOf<Job?>(null) }
//        var isLongPress by remember { mutableStateOf(false) }
//        Box(
//            modifier = Modifier
//                .size(200.dp)
//                .background(Color.LightGray)
//                .pointerInput(Unit) {
//                    detectTapGestures(
//                        onPress = {
//                            isLongPress = false
//                            autoIncrementJob = scope.launch {
//                                delay(500) // Начальная задержка для определения долгого нажатия
//                                isLongPress = true
//                                while (true) {
//                                    count += 10
//                                    delay(500) // Интервал между инкрементами
//                                }
//                            }
//                            try {
//
//                                autoIncrementJob?.join()
//                                if (!isLongPress) {
//                                    count += 10
//                                }
//                            } catch (_: Exception) {
//                            }
//                        },
//                        onTap = {
//                            autoIncrementJob?.cancel()
//                            isLongPress = false
//
//                        }
//                    )
//                },
//            contentAlignment = Alignment.Center
//        ) {
//            Text(text = count.toString(), fontSize = 40.sp)
//        }

        Text(authViewModel.getCurrentUser()?.displayName.toString())
        Text(authViewModel.getCurrentUser()?.uid.toString())
        Text(authViewModel.getCurrentUser()?.email.toString())
        Text(authViewModel.getCurrentUser()?.phoneNumber.toString())
        OutlinedButton(onClick = { navController.navigate(Screen.BodyProgress.route) }) {
            Text("Body Progress")
        }
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(authViewModel.getCurrentUser()?.photoUrl)
                    .crossfade(true)
                    .build()
            ),
            contentDescription = null
        )

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

    }
}

