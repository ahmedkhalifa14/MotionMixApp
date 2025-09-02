package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun ChatListItem(
    userImage: String,
    userName: String,
    lastMessage: String,
    lastMessageTime: String
) {
    Row(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        CircularImage(userImage)
        Column() {
            Text(
                userName,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            SpacerVertical16()
            Row {
                Text(
                    lastMessage,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                )
                Text(
                    lastMessageTime,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                )

            }
        }

    }
}