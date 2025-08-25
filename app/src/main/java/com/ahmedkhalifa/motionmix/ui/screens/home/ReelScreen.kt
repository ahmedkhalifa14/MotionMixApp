package com.ahmedkhalifa.motionmix.ui.screens.home

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.services.ExoPlayerManager
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.ReelState
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.ui.composable.CommentsBottomSheet
import com.ahmedkhalifa.motionmix.ui.composable.ReelBottomSheet
import com.ahmedkhalifa.motionmix.ui.composable.VideoOverlay
import kotlinx.coroutines.delay

@UnstableApi
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReelsScreen(
    navController: NavController,
    reelViewModel: ReelViewModel = hiltViewModel()
) {
    val reelsState by reelViewModel.reelsState.collectAsStateWithLifecycle(initialValue = emptyList())
    val getReelsState by reelViewModel.getReelsState.collectAsStateWithLifecycle(
        initialValue = Event(Resource.Init())
    )
    val videoStates by reelViewModel.videoStates.collectAsStateWithLifecycle()
    val bottomSheetState by reelViewModel.bottomSheetState.collectAsStateWithLifecycle()
    val isMuted by reelViewModel.isMuted.collectAsStateWithLifecycle()

    val playerManager = ExoPlayerManager.getInstance()

    // States for comments bottom sheet
    var showCommentsSheet by remember { mutableStateOf(false) }
    var selectedReelForComments by remember { mutableStateOf<Reel?>(null) }

    LaunchedEffect(Unit) {
        reelViewModel.getReels()
    }

    // Monitor activity lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                playerManager.pausePlayer()
                playerManager.release()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val resource = getReelsState.peekContent()
    when (resource) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        is Resource.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = resource.message ?: "An unknown error occurred",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { reelViewModel.getReels() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
        }

        else -> {
            if (reelsState.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "No videos available",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { reelViewModel.getReels() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                        ) {
                            Text("Refresh", color = Color.White)
                        }
                    }
                }
            } else {
                val pagerState = rememberPagerState(pageCount = { reelsState.size })
                val currentPlayingIndex by remember { derivedStateOf { pagerState.currentPage } }

                LaunchedEffect(pagerState.currentPage) {
                    reelViewModel.checkAndLoadMore(pagerState.currentPage)
                }

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { index -> reelsState[index].id }
                ) { page ->
                    val reel = reelsState[page]
                    val shouldPlay = page == currentPlayingIndex
                    val videoState = videoStates[reel.id] ?: ReelState()

                    VideoPlayerScreen(
                        reel = reel,
                        shouldPlay = shouldPlay,
                        videoState = videoState,
                        isMuted = isMuted,
                        onUpdateProgress = { progress ->
                            reelViewModel.updateProgress(reel.id, progress)
                        },
                        onSetLoading = { isLoading ->
                            reelViewModel.setLoading(reel.id, isLoading)
                        },
                        onSetError = { errorMessage ->
                            reelViewModel.setError(reel.id, errorMessage)
                        },
                        onSetPlaying = { isPlaying ->
                            reelViewModel.setPlaying(reel.id, isPlaying)
                        },
                        onToggleLike = {
                            reelViewModel.toggleLike(reel.id)
                        },
                        onToggleMute = { player ->
                            reelViewModel.toggleMute(player)
                        },
                        onCommentClick = {
                            selectedReelForComments = reel
                            showCommentsSheet = true
                        },
                        onShareClick = { context ->
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Watch this video: ${reel.mediaUrl}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share video"))
                        },
                        onMoreClick = {
                            reelViewModel.showBottomSheet(reel.id)
                        }
                    )
                }

                // Hoisted Bottom Sheet for options
                if (bottomSheetState.isVisible && bottomSheetState.selectedReelId != null) {
                    val selectedReel = reelsState.find { it.id == bottomSheetState.selectedReelId }
                    selectedReel?.let { reel ->
                        ReelBottomSheet(
                            reel = reel,
                            onDismiss = { reelViewModel.hideBottomSheet() },
                            onReport = {
                                reelViewModel.hideBottomSheet()
                            },
                            onSave = {
                                reelViewModel.hideBottomSheet()
                            },
                            onCopyLink = { context ->
                                reelViewModel.hideBottomSheet()
                                val copyIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, reel.mediaUrl)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(copyIntent, "Copy video link"))
                            }
                        )
                    }
                }

                // Comments Bottom Sheet
                if (showCommentsSheet && selectedReelForComments != null) {
                    CommentsBottomSheet(
                        reel = selectedReelForComments!!,
                        onDismiss = {
                            showCommentsSheet = false
                            selectedReelForComments = null
                        },
                        reelViewModel = reelViewModel
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            playerManager.release()
        }
    }
}

@UnstableApi
@Composable
fun VideoPlayerScreen(
    reel: Reel,
    shouldPlay: Boolean,
    videoState: ReelState,
    isMuted: Boolean,
    onUpdateProgress: (Float) -> Unit,
    onSetLoading: (Boolean) -> Unit,
    onSetError: (String?) -> Unit,
    onSetPlaying: (Boolean) -> Unit,
    onToggleLike: () -> Unit,
    onToggleMute: (player: ExoPlayer) -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: (context: Context) -> Unit,
    onMoreClick: () -> Unit,
) {
    val context = LocalContext.current
    val playerManager = ExoPlayerManager.getInstance()
    val player = playerManager.initializePlayer(context)

    // Create PlayerView for each page
    val playerView = remember(reel.id) {
        PlayerView(context).apply {
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    // Reset states when reel changes
    LaunchedEffect(reel.id) {
        onSetLoading(true)
        onSetError(null)
        onUpdateProgress(0f)
    }

    // Play control
    LaunchedEffect(shouldPlay, reel.mediaUrl) {
        if (shouldPlay) {
            onSetLoading(true)
            onSetError(null)
            delay(100)
            playerView.player = player
            playerManager.playMedia(
                mediaUrl = reel.mediaUrl,
                onReady = {
                    onSetLoading(false)
                    onSetError(null)
                    onSetPlaying(true)
                },
                onError = { error ->
                    onSetError(context.getString(R.string.failed_to_load_video_tap_to_try_again))
                    onSetLoading(false)
                    onSetPlaying(false)
                }
            )
        } else {
            playerManager.pausePlayer()
            playerView.player = null
            onSetPlaying(false)
        }
    }

    // Volume update
    LaunchedEffect(isMuted) {
        player.volume = if (isMuted) 0f else 1f
    }

    // Progress update
    LaunchedEffect(shouldPlay) {
        if (shouldPlay) {
            while (true) {
                try {
                    onUpdateProgress(playerManager.getCurrentProgress())
                } catch (e: Exception) {
                    e.message
                }
                delay(250)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Show PlayerView
        AndroidView(
            factory = { playerView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                if (shouldPlay) {
                    view.player = player
                } else {
                    view.player = null
                }
            }
        )

        // Show thumbnail while loading
        if (videoState.isLoading && reel.thumbnailUrl.isNotEmpty()) {
            AsyncImage(
                model = reel.thumbnailUrl,
                contentDescription = "Video thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Loading indicator
        if (videoState.isLoading && !videoState.isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        // Show error message
        videoState.errorMessage?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = message,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onSetLoading(true)
                                onSetError(null)
                                playerManager.playMedia(
                                    mediaUrl = reel.mediaUrl,
                                    onReady = {
                                        onSetLoading(false)
                                        onSetError(null)
                                        onSetPlaying(true)
                                    },
                                    onError = { error ->
                                        onSetError(context.getString(R.string.failed_to_load_video_tap_to_try_again))
                                        onSetLoading(false)
                                        onSetPlaying(false)
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            }
        }

        VideoOverlay(
            reel = reel,
            isMuted = isMuted,
            player = player,
            isLoading = videoState.isLoading,
            onToggleLike = onToggleLike,
            onToggleMute = { onToggleMute(player) },
            onCommentClick = onCommentClick,
            onShareClick = { onShareClick(context) },
            onMoreClick = onMoreClick
        )

        // Progress bar
        LinearProgressIndicator(
            progress = { videoState.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.BottomCenter),
            color = Color.White,
            trackColor = Color.Gray.copy(alpha = 0.5f)
        )
    }

    // Clean PlayerView on exit
    DisposableEffect(reel.id) {
        onDispose {
            playerView.player = null
            playerManager.pausePlayer()
        }
    }
}