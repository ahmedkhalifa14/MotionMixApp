package com.ahmedkhalifa.motionmix.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.Comment
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.domain.repo.reel_actions.ReelActionsRepo
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.media3.exoplayer.ExoPlayer
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReelViewModel @Inject constructor(
    private val reelActionsRepo: ReelActionsRepo,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _getReelsState = MutableStateFlow<Event<Resource<List<Reel>>>>(Event(Resource.Init()))
    val getReelsState: StateFlow<Event<Resource<List<Reel>>>> = _getReelsState.asStateFlow()

    private val _toggleLikeState = MutableStateFlow<Event<Resource<Reel?>>>(Event(Resource.Init()))
    val toggleLikeState: StateFlow<Event<Resource<Reel?>>> = _toggleLikeState.asStateFlow()

    private val _addCommentState = MutableStateFlow<Event<Resource<Boolean>>>(Event(Resource.Init()))
    val addCommentState: StateFlow<Event<Resource<Boolean>>> = _addCommentState.asStateFlow()

    private val _incrementSharesState = MutableStateFlow<Event<Resource<Boolean>>>(Event(Resource.Init()))
    val incrementSharesState: StateFlow<Event<Resource<Boolean>>> = _incrementSharesState.asStateFlow()

    private val _reelsState = MutableStateFlow<List<Reel>>(emptyList())
    val reelsState: StateFlow<List<Reel>> = _reelsState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _currentReelIndex = MutableStateFlow(0)
    val currentReelIndex: StateFlow<Int> = _currentReelIndex.asStateFlow()

    init {
        getReels()
    }

    fun getReels() {
        viewModelScope.launch {
            _getReelsState.emit(Event(Resource.Loading()))
            try {
                val getReelsResult = reelActionsRepo.getReels()
                when (getReelsResult) {
                    is Resource.Success -> {
                        val currentUserId = firebaseAuth.currentUser?.uid ?: ""
                        val updatedReels = getReelsResult.data?.map { reel ->
                            reel.copy(isLiked = currentUserId in reel.likedUserIds)
                        } ?: emptyList()
                        _reelsState.value = updatedReels
                    }

                    else -> {}
                }
                _getReelsState.emit(Event(getReelsResult))
            } catch (e: Exception) {
                _getReelsState.emit(Event(Resource.Error(e.message.toString())))
            }
        }
    }

    fun setReels(reels: List<Reel>, player: ExoPlayer, index: Int) {
        _reelsState.value = reels.map { reel ->
            reel.copy(isLiked = firebaseAuth.currentUser?.uid in reel.likedUserIds)
        }
        _currentReelIndex.value = index
        _isMuted.value = player.volume == 0f
    }

    fun updateCurrentIndex(index: Int, player: ExoPlayer) {
        _currentReelIndex.value = index
        _isMuted.value = player.volume == 0f
    }

    fun toggleLike(reelId: String) {
        viewModelScope.launch {
            _toggleLikeState.emit(Event(Resource.Loading()))
            try {
                val currentUserId = firebaseAuth.currentUser?.uid ?: return@launch
                val reel = _reelsState.value.find { it.id == reelId } ?: return@launch
                val newIsLiked = !reel.isLiked

                val toggleLikeResult = reelActionsRepo.toggleLike(reelId, currentUserId, newIsLiked)
                when (toggleLikeResult) {
                    is Resource.Success -> {
                        if (toggleLikeResult.data != null) {
                            _reelsState.value = _reelsState.value.map { r ->
                                if (r.id == reelId) toggleLikeResult.data else r
                            }
                        }
                    }

                    else -> {}
                }
                _toggleLikeState.emit(Event(toggleLikeResult))
            } catch (e: Exception) {
                _toggleLikeState.emit(Event(Resource.Error(e.message.toString())))
            }
        }
    }

    fun toggleMute(player: ExoPlayer) {
        viewModelScope.launch {
            _isMuted.value = !_isMuted.value
            player.volume = if (_isMuted.value) 0f else 1f
        }
    }

    fun addComment(reelId: String, commentText: String) {
        viewModelScope.launch {
            _addCommentState.emit(Event(Resource.Loading()))
            try {
                val currentUserId = firebaseAuth.currentUser?.uid ?: return@launch
                val comment = Comment(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    text = commentText,
                    timestamp = System.currentTimeMillis()
                )

                val addCommentResult = reelActionsRepo.addComment(reelId, comment)
                when (addCommentResult) {
                    is Resource.Success -> {
                        if (addCommentResult.data == true) {
                            _reelsState.value = _reelsState.value.map { reel ->
                                if (reel.id == reelId) {
                                    reel.copy(
                                        comments = reel.comments + comment,
                                        commentsCount = reel.commentsCount + 1
                                    )
                                } else {
                                    reel
                                }
                            }
                        }
                    }

                    else -> {}
                }
                _addCommentState.emit(Event(addCommentResult))
            } catch (e: Exception) {
                _addCommentState.emit(Event(Resource.Error(e.message.toString())))
            }
        }
    }

    fun incrementShares(reelId: String) {
        viewModelScope.launch {
            _incrementSharesState.emit(Event(Resource.Loading()))
            try {
                // Since incrementShares is not in your repo interface yet, 
                // I'll just update the local state for now
                _reelsState.value = _reelsState.value.map { reel ->
                    if (reel.id == reelId) {
                        reel.copy(sharesCount = reel.sharesCount + 1)
                    } else {
                        reel
                    }
                }
                _incrementSharesState.emit(Event(Resource.Success(true)))
            } catch (e: Exception) {
                _incrementSharesState.emit(Event(Resource.Error(e.message.toString())))
            }
        }
    }
}