package com.ahmedkhalifa.motionmix.ui.screens.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.Conversation
import com.ahmedkhalifa.motionmix.data.remote_data_source.FirebaseAuthenticationService
import com.ahmedkhalifa.motionmix.domain.repo.chat.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ConversationUiModel(
    val id: String,
    val displayName: String,
    val displayImage: String,
    val lastMessage: String,
    val formattedLastMessageTime: String,
    val unreadCount: Int
)

data class ConversationListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val conversations: List<ConversationUiModel> = emptyList()
)

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authenticationService: FirebaseAuthenticationService
) : ViewModel() {

    private val currentUserId = authenticationService.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(ConversationListUiState())
    val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()

    private val _createConversationEvent = MutableSharedFlow<Event<Resource<Conversation>>>()
    val createConversationEvent: SharedFlow<Event<Resource<Conversation>>> = _createConversationEvent.asSharedFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            chatRepository.getConversations(currentUserId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }
                    is Resource.Success -> {
                        val uiModels = resource.data?.map { it.toUiModel() } ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            conversations = uiModels,
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    fun createConversation(otherUserId: String) {
        viewModelScope.launch {
            _createConversationEvent.emit(Event(Resource.Loading()))
            val result = chatRepository.createConversation(listOf(currentUserId, otherUserId))
            _createConversationEvent.emit(Event(result))
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun Conversation.toUiModel(): ConversationUiModel {
        val otherParticipantId = participants.firstOrNull { it != currentUserId } ?: ""
        val displayName = getUserName(otherParticipantId) // Replace with actual repository call
        val displayImage = getUserAvatar(otherParticipantId) // Replace with actual repository call
        val unreadCount = unreadCount[currentUserId] ?: 0
        val formattedTime = formatLastMessageTime(lastMessageTime)

        return ConversationUiModel(
            id = id,
            displayName = displayName,
            displayImage = displayImage,
            lastMessage = lastMessage,
            formattedLastMessageTime = formattedTime,
            unreadCount = unreadCount
        )
    }

    private fun formatLastMessageTime(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "now"
            diff < 3600_000 -> "${diff / 60_000}m"
            diff < 86400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            diff < 604800_000 -> SimpleDateFormat("E", Locale.getDefault()).format(Date(timestamp))
            else -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))
        }
    }

    private fun getUserName(userId: String): String {
        // Implement repository call to fetch user name
        return "User" // Placeholder
    }

    private fun getUserAvatar(userId: String): String {
        // Implement repository call to fetch user avatar
        return "" // Placeholder
    }
}