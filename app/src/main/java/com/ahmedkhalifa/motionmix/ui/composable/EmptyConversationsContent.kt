package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.ui.theme.AppMainColor

import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun EmptyConversationsContent(
    onNewChatClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = stringResource(R.string.no_conversations),
                tint = AppMainColor,
                modifier = Modifier.size(80.dp)
            )
            SpacerVertical24()
            Text(
                text = stringResource(R.string.no_conversations_yet),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            SpacerVertical8()
            Text(
                text = stringResource(R.string.start_conversation),
                fontFamily = Montserrat,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,

                )
            SpacerVertical24()
            Button(
                onClick = onNewChatClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppMainColor
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                SpacerHorizontal8()
                Text(
                    text = stringResource(R.string.start_chat),
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}
