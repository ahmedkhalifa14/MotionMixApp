package com.ahmedkhalifa.motionmix.ui.screens.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.ui.composable.ChatHeader
import com.ahmedkhalifa.motionmix.ui.composable.MessageInput
import com.ahmedkhalifa.motionmix.ui.composable.MessageItem

@Composable
fun ChatScreen(
    conversationId: String,
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sendMessageEvent by viewModel.sendMessageEvent.collectAsState(initial = Event(Resource.Init()))

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Handle send message events
    LaunchedEffect(sendMessageEvent) {
        sendMessageEvent.getContentIfNotHandled()?.let { resource ->
            when (resource) {
                is Resource.Success -> {
                    messageText = ""
                }
                is Resource.Error -> {
                    // Show error snackbar
                }
                else -> {}
            }
        }
    }

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Chat Header
        ChatHeader(
            onBackClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        )

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(
                count = uiState.messages.size,
                key = { index -> uiState.messages[index].id }
            ) { index ->
                val message = uiState.messages[index]
                MessageItem(
                    message = message,
                    isCurrentUser = message.senderId == uiState.currentUser,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Message Input
        MessageInput(
            value = messageText,
            onValueChange = { messageText = it },
            onSendClick = { viewModel.sendMessage(messageText) },
            onMediaClick = { uri, type -> viewModel.sendMediaMessage(uri, type) },
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Loading indicator
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
