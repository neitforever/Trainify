package com.example.motivationcalendarapi.ui.profile.fragments


import Screen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LogoutButton(
    coroutineScope: CoroutineScope,
    authViewModel: AuthViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = {
            coroutineScope.launch {
                authViewModel.signOut()
                navController.navigate(Screen.Auth.route)
            }
        },
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Text(
            text = stringResource(R.string.log_out),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
