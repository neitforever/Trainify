package com.example.motivationcalendarapi.ui

import GoogleAuthClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseUser
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

        Text(authViewModel.getCurrentUser()?.displayName.toString())
        Text(authViewModel.getCurrentUser()?.uid.toString())
        Text(authViewModel.getCurrentUser()?.email.toString())
        Text(authViewModel.getCurrentUser()?.phoneNumber.toString())

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

