package com.ahmedkhalifa.motionmix.ui.screens.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.Resource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.data.model.Conversation
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationsListScreen(
    onConversationClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    viewModel: ConversationListViewModel = hiltViewModel()
) {
    ConversationsListScreenContent(
        onConversationClick = onConversationClick,
        onNewChatClick = onNewChatClick,
        viewModel = viewModel
    )
}

@Composable
fun ConversationsListScreenContent(
    onConversationClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    viewModel: ConversationListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val createConversationEvent by viewModel.createConversationEvent.collectAsState(
        initial = Event(Resource.Init())
    )

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle create conversation events
    LaunchedEffect(createConversationEvent) {
        createConversationEvent.getContentIfNotHandled()?.let { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { conversation ->
                        onConversationClick(conversation.id)
                    }
                }
                is Resource.Error -> {
                    snackbarHostState.showSnackbar(
                        message = resource.message ?: "Failed to create conversation"
                    )
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            ConversationsTopBar(
                onNewChatClick = onNewChatClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { /* Retry logic if needed */ }
                    )
                }
                uiState.conversations.isEmpty() -> {
                    EmptyConversationsContent(
                        onNewChatClick = onNewChatClick
                    )
                }
                else -> {
                    ConversationsList(
                        conversations = uiState.conversations,
                        onConversationClick = onConversationClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationsTopBar(
    onNewChatClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Chats",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        actions = {
            IconButton(onClick = onNewChatClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "New Chat"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun ConversationsList(
    conversations: List<Conversation>,
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

@Composable
private fun ConversationListItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    // Get conversation display info
    val displayName = getConversationDisplayName(conversation)
    val displayImage = getConversationDisplayImage(conversation)
    val unreadCount = getUnreadCount(conversation)
    val lastMessageTime = formatLastMessageTime(conversation.lastMessageTime)

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
            userImage = displayImage,
            userName = displayName,
            lastMessage = conversation.lastMessage,
            lastMessageTime = lastMessageTime,
            unreadCount = unreadCount,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ChatListItem(
    userImage: String,
    userName: String,
    lastMessage: String,
    lastMessageTime: String,
    unreadCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Image
        CircularImage(
            imageUrl = userImage,
            modifier = Modifier.size(56.dp)
        )

        SpacerHorizontal16()

        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // User name and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = userName,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = lastMessageTime,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SpacerVertical8()

            // Last message and unread count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lastMessage,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (unreadCount > 0) {
                    SpacerHorizontal8()
                    UnreadCountBadge(count = unreadCount)
                }
            }
        }
    }
}

@Composable
private fun UnreadCountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            SpacerVertical16()
            Text(
                text = "Loading conversations...",
                fontFamily = Montserrat,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
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
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            SpacerVertical16()
            Text(
                text = "Something went wrong",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            SpacerVertical8()
            Text(
                text = error,
                fontFamily = Montserrat,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            SpacerVertical24()
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Try Again",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmptyConversationsContent(
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
                contentDescription = "No conversations",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            SpacerVertical24()
            Text(
                text = "No conversations yet",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            SpacerVertical8()
            Text(
                text = "Start a conversation with someone",
                fontFamily = Montserrat,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            SpacerVertical24()
            Button(
                onClick = onNewChatClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                SpacerHorizontal8()
                Text(
                    text = "Start Chat",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Helper functions
// Helper functions
private fun getConversationDisplayName(conversation: Conversation): String {
    // For 1-on-1 conversations, get the other participant's name
    val currentUserId = getCurrentUserId()
    val otherParticipantId = conversation.participants.firstOrNull { it != currentUserId }

    // This should be replaced with actual user name lookup
    return otherParticipantId?.let { getUserName(it) } ?: "Unknown User"
}

private fun getConversationDisplayImage(conversation: Conversation): String {
    // For 1-on-1 conversations, get the other participant's avatar
    val currentUserId = getCurrentUserId()
    val otherParticipantId = conversation.participants.firstOrNull { it != currentUserId }

    // This should be replaced with actual user avatar lookup
    return otherParticipantId?.let { getUserAvatar(it) } ?: ""
}


private fun getUnreadCount(conversation: Conversation): Int {
    // Get unread count for current user
    // You'd need to get the current user ID here
    val currentUserId = getCurrentUserId() // Implement this function
    return conversation.unreadCount[currentUserId] ?: 0
}

private fun formatLastMessageTime(timestamp: Long): String {
    if (timestamp == 0L) return ""

    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "now" // Less than 1 minute
        diff < 3600_000 -> "${diff / 60_000}m" // Less than 1 hour
        diff < 86400_000 -> { // Less than 1 day
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
        diff < 604800_000 -> { // Less than 1 week
            val formatter = SimpleDateFormat("E", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
        else -> {
            val formatter = SimpleDateFormat("MM/dd", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}

private fun getCurrentUserId(): String {
    // Implement this to get current user ID from your auth system
    return ""
}

// Helper functions to get user data - you'll need to implement these
private fun getUserName(userId: String): String {
    // This should fetch user name from your user repository/cache
    // For now, return a placeholder
    return "User"
}

private fun getUserAvatar(userId: String): String {
    // This should fetch user avatar URL from your user repository/cache
    // For now, return empty string
    return ""
}


// You'll need to implement these spacer composables if they don't exist
@Composable
fun SpacerVertical8() {
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SpacerVertical16() {
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun SpacerVertical24() {
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun SpacerHorizontal8() {
    Spacer(modifier = Modifier.width(8.dp))
}

@Composable
fun SpacerHorizontal16() {
    Spacer(modifier = Modifier.width(16.dp))
}

@Composable
fun CircularImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Profile picture",
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentScale = ContentScale.Crop,
        error = painterResource(id = R.drawable.app_logo), // Add a placeholder image
        placeholder = painterResource(id = R.drawable.app_logo)
    )
}