package com.example.motivationcalendarapi.ui.exercise.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.utils.BodyPart
import java.util.Locale

@Composable
fun CollapsibleBodyPartItem(
    bodyPart: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val bodyPartType = BodyPart.fromString(bodyPart)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = painterResource(id = bodyPartType.iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )


        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = bodyPart.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            },
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .weight(1f)
        )

        val rotation by animateFloatAsState(
            targetValue = if (isExpanded) 180f else 0f,
            animationSpec = tween(durationMillis = 210),
            label = "Icon Animation"
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_drop_down),
            contentDescription = "drop down list",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .graphicsLayer { rotationZ = rotation }
        )
    }
}
