package com.ahmedkhalifa.motionmix.ui.screens.inbox

import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.EventObserver
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.Conversation
import com.ahmedkhalifa.motionmix.ui.composable.ChatListItem
import com.ahmedkhalifa.motionmix.ui.composable.ConversationListItem
import com.ahmedkhalifa.motionmix.ui.composable.ConversationsErrorContent
import com.ahmedkhalifa.motionmix.ui.composable.ConversationsList
import com.ahmedkhalifa.motionmix.ui.composable.ConversationsLoadingContent
import com.ahmedkhalifa.motionmix.ui.composable.ConversationsTopBar
import com.ahmedkhalifa.motionmix.ui.composable.EmptyConversationsContent
import com.ahmedkhalifa.motionmix.ui.theme.AppMainColor
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun ConversationsListScreen(
    onConversationClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    viewModel: ConversationListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val createConversationEventObserver = EventObserver<Conversation>(
        onLoading = { isLoading = true },
        onSuccess = { conversation ->
            isLoading = false
            Toast.makeText(
                context,
                context.getString(R.string.conversation_created_successfully),
                Toast.LENGTH_SHORT
            ).show()
            onConversationClick(conversation.id)
        },
        onError = { errorMessage ->
            isLoading = false
            Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
        }
    )

    LaunchedEffect(viewModel.createConversationEvent) {
        viewModel.createConversationEvent.collect { event ->
            event.getContentIfNotHandled()?.let { resource ->
                when (resource) {
                    is Resource.Loading -> createConversationEventObserver.emit(Event(Resource.Loading<Conversation>()))
                    is Resource.Success -> {
                        resource.data?.let { conversation ->
                            createConversationEventObserver.emit(Event(Resource.Success(conversation)))
                        }
                    }
                    is Resource.Error -> {
                        resource.message?.let { message ->
                            createConversationEventObserver.emit(Event(Resource.Error(message)))
                        }
                    }
                    is Resource.Init -> {}
                }
            }
        }
    }

    ConversationsListScreenContent(
        uiState = uiState,
        isLoading = isLoading,
        onNewChatClick = onNewChatClick,
        onConversationClick = onConversationClick,
        onRetry = { viewModel.clearError() }
    )
}

@Composable
fun ConversationsListScreenContent(
    uiState: ConversationListUiState,
    isLoading: Boolean,
    onNewChatClick: () -> Unit,
    onConversationClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            val result = snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Retry",
                withDismissAction = true
            )
            if (result == SnackbarResult.ActionPerformed) {
                onRetry()
            }
        }
    }

    Scaffold(
        topBar = {
            ConversationsTopBar(
                onNewChatClick = onNewChatClick,
                onSearchClick = {}
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = AppMainColor
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.new_chat),
                    tint = Color.White
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
                isLoading || uiState.isLoading -> {
                    ConversationsLoadingContent()
                }
                uiState.error != null -> {
                    ConversationsErrorContent(
                        error = uiState.error,
                        onRetry = onRetry
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



