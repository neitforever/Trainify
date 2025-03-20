package com.example.motivationcalendarapi.ui.fragments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.SetStatus

@Composable
fun StatusIcon(
    status: SetStatus,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            SetStatus.NONE -> Icon(
                painter = painterResource(R.drawable.ic_empty),
                contentDescription = "Empty",
                modifier = Modifier.size(24.dp)
            )

            SetStatus.WARMUP -> Icon(
                painter = painterResource(R.drawable.ic_warm_up),
                contentDescription = "Warm-up",
                modifier = Modifier.size(24.dp)
            )

            SetStatus.FAILED -> Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "Failed",
                modifier = Modifier.size(24.dp)
            )

            SetStatus.COMPLETED -> Icon(
                painter = painterResource(R.drawable.ic_complete),
                contentDescription = "Completed",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}