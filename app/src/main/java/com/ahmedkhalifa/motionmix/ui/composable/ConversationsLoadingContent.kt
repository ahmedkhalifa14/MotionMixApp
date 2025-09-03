package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.ui.theme.AppMainColor
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun ConversationsLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = AppMainColor
            )
            SpacerVertical16()
            Text(
                text = stringResource(R.string.loading_conversations),
                fontFamily = Montserrat,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}