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

    private val _incrementSharesState = MutableStateFlow<Event<Resource<Boolean>>>(Event(Resource.Init()))
    val incrementSharesState: StateFlow<Event<Resource<Boolean>>> = _incrementSharesState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

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

                Log.d("ReelViewModel", "Calling reelActionsRepo.getReelsPaginated...")
                val getReelsResult = reelActionsRepo.getReelsPaginated(
                    limit = pageSize,
                    lastDocument = null
                )

                Log.d("ReelViewModel", "Repository result type: ${getReelsResult::class.simpleName}")

                when (getReelsResult) {
                    is Resource.Success -> {
                        Log.d("ReelViewModel", "Resource.Success received")
                        val (reels, newLastDoc) = getReelsResult.data ?: Pair(emptyList<Reel>(), null)
                        Log.d("ReelViewModel", "Data extracted: ${reels.size} reels")

                        val currentUserId = firebaseAuth.currentUser?.uid ?: ""
                        Log.d("ReelViewModel", "Current user ID: $currentUserId")

                        val updatedReels = reels.map { reel ->
                            Log.d("ReelViewModel", "Processing reel: ${reel.id}")
                            reel.copy(isLiked = currentUserId in reel.likedUserIds)
                        }

                        Log.d("ReelViewModel", "Setting ${updatedReels.size} reels to state")
                        _reelsState.value = updatedReels
                        lastDocument = newLastDoc
                        hasMoreData = reels.size >= pageSize

                        _getReelsState.emit(Event(Resource.Success(updatedReels)))
                        Log.d("ReelViewModel", "Successfully emitted success state")
                    }
                    is Resource.Error -> {
                        Log.e("ReelViewModel", "Resource.Error: ${getReelsResult.message}")
                        _getReelsState.emit(Event(Resource.Error(getReelsResult.message ?: "Unknown error")))
                    }
                    else -> {
                        Log.e("ReelViewModel", "Unexpected result type: ${getReelsResult::class.simpleName}")
                        _getReelsState.emit(Event(Resource.Error("Unexpected result")))
                    }
                }
            } catch (e: Exception) {
                Log.e("ReelViewModel", "Exception in getReels: ${e.message}", e)
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

    fun refreshReels() {
        getReels()
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

//    fun incrementShares(reelId: String) {
//        viewModelScope.launch {
//            _incrementSharesState.emit(Event(Resource.Loading()))
//            try {
//                val result = reelActionsRepo.incrementShares(reelId)
//                if (result is Resource.Success && result.data == true) {
//                    _reelsState.value = _reelsState.value.map { reel ->
//                        if (reel.id == reelId) {
//                            reel.copy(sharesCount = reel.sharesCount + 1)
//                        } else {
//                            reel
//                        }
//                    }
//                }
//                _incrementSharesState.emit(Event(result))
//            } catch (e: Exception) {
//                _incrementSharesState.emit(Event(Resource.Error(e.message.toString())))
//            }
//        }
//    }

}