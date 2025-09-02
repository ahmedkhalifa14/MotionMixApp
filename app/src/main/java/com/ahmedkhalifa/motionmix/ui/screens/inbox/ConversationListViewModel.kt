package com.ahmedkhalifa.motionmix.ui.screens.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedkhalifa.motionmix.common.utils.ConversationListUiState
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
                        _uiState.value = _uiState.value.copy(
                            conversations = resource.data ?: emptyList(),
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
}
