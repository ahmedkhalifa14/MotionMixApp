package com.ahmedkhalifa.motionmix.ui.screens.home

import android.util.Log
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
import com.ahmedkhalifa.motionmix.common.utils.BottomSheetState
import com.ahmedkhalifa.motionmix.common.utils.ReelState
import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReelViewModel @Inject constructor(
    private val reelActionsRepo: ReelActionsRepo,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _reelsState = MutableStateFlow<List<Reel>>(emptyList())
    val reelsState: StateFlow<List<Reel>> = _reelsState.asStateFlow()

    private val _getReelsState = MutableStateFlow<Event<Resource<List<Reel>>>>(Event(Resource.Init()))
    val getReelsState: StateFlow<Event<Resource<List<Reel>>>> = _getReelsState.asStateFlow()

    private val _toggleLikeState = MutableStateFlow<Event<Resource<Reel?>>>(Event(Resource.Init()))
    val toggleLikeState: StateFlow<Event<Resource<Reel?>>> = _toggleLikeState.asStateFlow()

    private val _addCommentState = MutableStateFlow<Event<Resource<Boolean>>>(Event(Resource.Init()))
    val addCommentState: StateFlow<Event<Resource<Boolean>>> = _addCommentState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // Hoisted state for individual video players
    private val _videoStates = MutableStateFlow<Map<String, ReelState>>(emptyMap())
    val videoStates: StateFlow<Map<String, ReelState>> = _videoStates.asStateFlow()

    // Hoisted state for bottom sheet
    private val _bottomSheetState = MutableStateFlow<BottomSheetState>(BottomSheetState())
    val bottomSheetState: StateFlow<BottomSheetState> = _bottomSheetState.asStateFlow()

    private var lastDocument: DocumentSnapshot? = null
    private var hasMoreData = true
    private val pageSize = 10L

    fun getReels() {
        viewModelScope.launch {
            Log.d("ReelViewModel", "Starting getReels()")
            _getReelsState.emit(Event(Resource.Loading()))

            try {
                lastDocument = null
                hasMoreData = true

                val getReelsResult = reelActionsRepo.getReelsPaginated(
                    limit = pageSize,
                    lastDocument = null
                )

                when (getReelsResult) {
                    is Resource.Success -> {
                        val (reels, newLastDoc) = getReelsResult.data ?: Pair(emptyList<Reel>(), null)
                        val currentUserId = firebaseAuth.currentUser?.uid ?: ""

                        val updatedReels = reels.map { reel ->
                            reel.copy(isLiked = currentUserId in reel.likedUserIds)
                        }

                        _reelsState.value = updatedReels
                        lastDocument = newLastDoc
                        hasMoreData = reels.size >= pageSize

                        // Initialize video states for new reels
                        _videoStates.value = updatedReels.associate { reel ->
                            reel.id to ReelState()
                        }

                        _getReelsState.emit(Event(Resource.Success(updatedReels)))
                    }
                    is Resource.Error -> {
                        _getReelsState.emit(Event(Resource.Error(getReelsResult.message ?: "Unknown error")))
                    }
                    else -> {
                        _getReelsState.emit(Event(Resource.Error("Unexpected result")))
                    }
                }
            } catch (e: Exception) {
                _getReelsState.emit(Event(Resource.Error(e.message ?: "Unknown error")))
            }
        }
    }

    fun loadMoreReels() {
        if (!hasMoreData || _isLoadingMore.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val getReelsResult = reelActionsRepo.getReelsPaginated(
                    limit = pageSize,
                    lastDocument = lastDocument
                )

                when (getReelsResult) {
                    is Resource.Success -> {
                        val (newReels, newLastDoc) = getReelsResult.data ?: Pair(emptyList<Reel>(), null)
                        val currentUserId = firebaseAuth.currentUser?.uid ?: ""

                        val updatedNewReels = newReels.map { reel ->
                            reel.copy(isLiked = currentUserId in reel.likedUserIds)
                        }

                        _reelsState.value = _reelsState.value + updatedNewReels
                        lastDocument = newLastDoc
                        hasMoreData = newReels.size >= pageSize

                        // Add video states for new reels
                        val newVideoStates = updatedNewReels.associate { reel ->
                            reel.id to ReelState()
                        }
                        _videoStates.value = _videoStates.value + newVideoStates
                    }
                    is Resource.Error -> {
                        Log.e("ReelViewModel", "Failed to load more reels: ${getReelsResult.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("ReelViewModel", "Exception loading more reels: ${e.message}")
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun checkAndLoadMore(currentIndex: Int) {
        val totalItems = _reelsState.value.size
        if (currentIndex >= totalItems - 3 && hasMoreData && !_isLoadingMore.value) {
            loadMoreReels()
        }
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
            Log.d("ReelViewModel", "Starting addComment for reel: $reelId")

            _addCommentState.emit(Event(Resource.Loading()))

            var comment: Comment? = null

            try {
                val currentUserId = firebaseAuth.currentUser?.uid
                if (currentUserId == null) {
                    _addCommentState.emit(Event(Resource.Error("User not authenticated")))
                    return@launch
                }

                comment = Comment(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    text = commentText,
                    timestamp = System.currentTimeMillis()
                )

                Log.d("ReelViewModel", "Created comment: $comment")

                // Add locally first
                _reelsState.value = _reelsState.value.map { reel ->
                    if (reel.id == reelId) {
                        val newComments = reel.comments + comment
                        val newCount = reel.commentsCount + 1
                        Log.d("ReelViewModel", "Updated locally - comments: ${newComments.size}, count: $newCount")
                        reel.copy(comments = newComments, commentsCount = newCount)
                    } else {
                        reel
                    }
                }

                // Try to add to Firebase
                Log.d("ReelViewModel", "Calling repo to add comment to Firebase")
                val addCommentResult = reelActionsRepo.addComment(reelId, comment)
                Log.d("ReelViewModel", "Repo result: $addCommentResult")

                when (addCommentResult) {
                    is Resource.Success -> {
                        val success = addCommentResult.data ?: false
                        if (success) {
                            Log.d("ReelViewModel", "Successfully added to Firebase")
                            _addCommentState.emit(Event(Resource.Success(true)))
                        } else {
                            Log.e("ReelViewModel", "Firebase returned false")
                            rollbackLocalComment(reelId, comment.id)
                            _addCommentState.emit(Event(Resource.Error("Failed to add comment")))
                        }
                    }
                    is Resource.Error -> {
                        Log.e("ReelViewModel", "Error from repo: ${addCommentResult.message}")
                        rollbackLocalComment(reelId, comment.id)
                        _addCommentState.emit(Event(Resource.Error(addCommentResult.message.toString())))
                    }
                    else -> {
                        Log.e("ReelViewModel", "Unexpected result")
                        rollbackLocalComment(reelId, comment.id)
                        _addCommentState.emit(Event(Resource.Error("Unexpected result")))
                    }
                }
            } catch (e: Exception) {
                Log.e("ReelViewModel", "Exception: ${e.message}", e)
                if (comment != null) {
                    rollbackLocalComment(reelId, comment.id)
                }
                _addCommentState.emit(Event(Resource.Error(e.message.toString())))
            }
        }
    }

    private fun rollbackLocalComment(reelId: String, commentId: String) {
        _reelsState.value = _reelsState.value.map { reel ->
            if (reel.id == reelId) {
                val filteredComments = reel.comments.filter { it.id != commentId }
                val newCount = (reel.commentsCount - 1).coerceAtLeast(0)
                Log.d("ReelViewModel", "Rollback - comments: ${filteredComments.size}, count: $newCount")
                reel.copy(comments = filteredComments, commentsCount = newCount)
            } else {
                reel
            }
        }
    }    // State hoisting methods for video states
    fun updateVideoState(reelId: String, update: (ReelState) -> ReelState) {
        viewModelScope.launch {
            val currentStates = _videoStates.value.toMutableMap()
            val currentState = currentStates[reelId] ?: ReelState()
            currentStates[reelId] = update(currentState)
            _videoStates.value = currentStates
        }
    }

    fun updateProgress(reelId: String, progress: Float) {
        updateVideoState(reelId) { it.copy(progress = progress) }
    }

    fun setLoading(reelId: String, isLoading: Boolean) {
        updateVideoState(reelId) { it.copy(isLoading = isLoading) }
    }

    fun setError(reelId: String, errorMessage: String?) {
        updateVideoState(reelId) { it.copy(errorMessage = errorMessage) }
    }

    fun setPlaying(reelId: String, isPlaying: Boolean) {
        updateVideoState(reelId) { it.copy(isPlaying = isPlaying) }
    }

    // State hoisting methods for bottom sheet
    fun showBottomSheet(reelId: String) {
        viewModelScope.launch {
            _bottomSheetState.value = BottomSheetState(
                isVisible = true,
                selectedReelId = reelId
            )
        }
    }

    fun hideBottomSheet() {
        viewModelScope.launch {
            _bottomSheetState.value = BottomSheetState(
                isVisible = false,
                selectedReelId = null
            )
        }
    }

    suspend fun getCommentsForReel(reelId: String): List<Comment> {
        return try {
            reelActionsRepo.getCommentsForReel(reelId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}