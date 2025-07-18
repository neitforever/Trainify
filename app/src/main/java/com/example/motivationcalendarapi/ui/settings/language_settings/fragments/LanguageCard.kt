package com.example.motivationcalendarapi.ui.settings.language_settings.fragments

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R

@Composable
fun LanguageCard(
    title: String, languageCode: String, isSelected: Boolean, onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 12.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }, contentColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 20.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = when (languageCode) {
                    "ru" -> R.drawable.ic_flag_ru
                    "en" -> R.drawable.ic_flag_en
                    "be" -> R.drawable.ic_flag_by
                    else -> R.drawable.ic_flag
                }),
                contentDescription = title,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )


            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

