package com.ahmedkhalifa.motionmix.ui.screens.inbox


import androidx.compose.runtime.Composable

//
//import android.widget.Toast
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.material3.SnackbarResult
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.ahmedkhalifa.motionmix.R
//import com.ahmedkhalifa.motionmix.common.utils.Event
//import com.ahmedkhalifa.motionmix.common.utils.EventObserver
//import com.ahmedkhalifa.motionmix.common.utils.Resource
//import com.ahmedkhalifa.motionmix.data.model.Conversation
//import com.ahmedkhalifa.motionmix.ui.composable.ConversationsErrorContent
//import com.ahmedkhalifa.motionmix.ui.composable.ConversationsList
//import com.ahmedkhalifa.motionmix.ui.composable.ConversationsLoadingContent
//import com.ahmedkhalifa.motionmix.ui.composable.ConversationsTopBar
//import com.ahmedkhalifa.motionmix.ui.composable.EmptyConversationsContent
//import com.ahmedkhalifa.motionmix.ui.composable.UserProfilesSection
//import com.ahmedkhalifa.motionmix.ui.theme.AppMainColor
//
@Composable
fun ConversationsListScreen(
//    onConversationClick: (String) -> Unit,
//    onNewChatClick: () -> Unit,
//    onProfileClick: (String) -> Unit = {},
//    conversationViewModel: ConversationListViewModel = hiltViewModel(),
//    profilesViewModel: UserProfilesViewModel = hiltViewModel()
) {
//    val conversationUiState by conversationViewModel.uiState.collectAsState()
//    val profilesUiState by profilesViewModel.uiState.collectAsState()
//    var isLoading by remember { mutableStateOf(false) }
//    val context = LocalContext.current
//
//    val createConversationEventObserver = EventObserver<Conversation>(
//        onLoading = { isLoading = true },
//        onSuccess = { conversation ->
//            isLoading = false
//            Toast.makeText(
//                context,
//                context.getString(R.string.conversation_created_successfully),
//                Toast.LENGTH_SHORT
//            ).show()
//            onConversationClick(conversation.id)
//        },
//        onError = { errorMessage ->
//            isLoading = false
//            Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
//        }
//    )
//
//    LaunchedEffect(conversationViewModel.createConversationEvent) {
//        conversationViewModel.createConversationEvent.collect { event ->
//            event.getContentIfNotHandled()?.let { resource ->
//                when (resource) {
//                    is Resource.Loading -> createConversationEventObserver.emit(Event(Resource.Loading<Conversation>()))
//                    is Resource.Success -> {
//                        resource.data?.let { conversation ->
//                            createConversationEventObserver.emit(Event(Resource.Success(conversation)))
//                        }
//                    }
//                    is Resource.Error -> {
//                        resource.message?.let { message ->
//                            createConversationEventObserver.emit(Event(Resource.Error(message)))
//                        }
//                    }
//                    is Resource.Init -> {}
//                }
//            }
//        }
//    }
//
//    ConversationsListScreenContent(
//        conversationUiState = conversationUiState,
//        profilesUiState = profilesUiState,
//        isLoading = isLoading,
//        onNewChatClick = onNewChatClick,
//        onConversationClick = onConversationClick,
//        onProfileClick = onProfileClick,
//        onConversationRetry = { conversationViewModel.clearError() },
//        onProfilesRetry = { profilesViewModel.retry() }
//    )
}
//
//@Composable
//fun ConversationsListScreenContent(
//    conversationUiState: ConversationListUiState,
//    profilesUiState: UserProfilesUiState,
//    isLoading: Boolean,
//    onNewChatClick: () -> Unit,
//    onConversationClick: (String) -> Unit,
//    onProfileClick: (String) -> Unit,
//    onConversationRetry: () -> Unit,
//    onProfilesRetry: () -> Unit
//) {
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    // Handle conversation errors
//    LaunchedEffect(conversationUiState.error) {
//        conversationUiState.error?.let { error ->
//            val result = snackbarHostState.showSnackbar(
//                message = error,
//                actionLabel = "Retry",
//                withDismissAction = true
//            )
//            if (result == SnackbarResult.ActionPerformed) {
//                onConversationRetry()
//            }
//        }
//    }
//
//    // Handle profiles errors
//    LaunchedEffect(profilesUiState.error) {
//        profilesUiState.error?.let { error ->
//            val result = snackbarHostState.showSnackbar(
//                message = "Profiles: $error",
//                actionLabel = "Retry",
//                withDismissAction = true
//            )
//            if (result == SnackbarResult.ActionPerformed) {
//                onProfilesRetry()
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            ConversationsTopBar(
//                onNewChatClick = onNewChatClick,
//                onSearchClick = {}
//            )
//        },
//        snackbarHost = { SnackbarHost(snackbarHostState) },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = onNewChatClick,
//                containerColor = AppMainColor
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = stringResource(R.string.new_chat),
//                    tint = Color.White
//                )
//            }
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            // User Profiles Section
//            ProfilesSection(
//                profilesUiState = profilesUiState,
//                onProfileClick = onProfileClick,
//                onRetry = onProfilesRetry
//            )
//
//            // Conversations Section
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f)
//            ) {
//                when {
//                    isLoading || conversationUiState.isLoading -> {
//                        ConversationsLoadingContent()
//                    }
//                    conversationUiState.error != null -> {
//                        ConversationsErrorContent(
//                            error = conversationUiState.error,
//                            onRetry = onConversationRetry
//                        )
//                    }
//                    conversationUiState.conversations.isEmpty() -> {
//                        EmptyConversationsContent(
//                            onNewChatClick = onNewChatClick
//                        )
//                    }
//                    else -> {
//                        ConversationsList(
//                            conversations = conversationUiState.conversations,
//                            onConversationClick = onConversationClick
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun ProfilesSection(
//    profilesUiState: UserProfilesUiState,
//    onProfileClick: (String) -> Unit,
//    onRetry: () -> Unit
//) {
//    when {
//        profilesUiState.isLoading -> {
//            Text(
//                text = "Loading profiles...",
//                modifier = Modifier.padding(16.dp),
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//        profilesUiState.error != null -> {
//            Text(
//                text = "Failed to load profiles",
//                modifier = Modifier.padding(16.dp),
//                color = MaterialTheme.colorScheme.error
//            )
//        }
//        profilesUiState.profiles.isNotEmpty() -> {
//            UserProfilesSection(
//                profiles = profilesUiState.profiles,
//                onProfileClick = onProfileClick
//            )
//            // Divider between profiles and conversations
//            HorizontalDivider(
//                modifier = Modifier.padding(horizontal = 16.dp),
//                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
//            )
//        }
//    }
//}