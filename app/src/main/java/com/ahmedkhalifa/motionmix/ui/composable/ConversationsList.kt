package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahmedkhalifa.motionmix.ui.screens.inbox.ConversationUiModel

@Composable
fun ConversationsList(
    conversations: List<ConversationUiModel>,
    onConversationClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            count = conversations.size,
            key = { index -> conversations[index].id }
        ) { index ->
            val conversation = conversations[index]
            ConversationListItem(
                conversation = conversation,
                onClick = { onConversationClick(conversation.id) }
            )
        }
    }
}
