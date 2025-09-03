package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahmedkhalifa.motionmix.ui.screens.inbox.ConversationUiModel

@Composable
fun ConversationListItem(
    conversation: ConversationUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ChatListItem(
            userImage = conversation.displayImage,
            userName = conversation.displayName,
            lastMessage = conversation.lastMessage,
            lastMessageTime = conversation.formattedLastMessageTime,
            unreadCount = conversation.unreadCount,
            modifier = Modifier.padding(16.dp)
        )
    }
}
