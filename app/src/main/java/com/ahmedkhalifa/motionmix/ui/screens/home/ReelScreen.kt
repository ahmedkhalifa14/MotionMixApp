package com.ahmedkhalifa.motionmix.ui.screens.home

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.ui.composable.VideoOverlay
import com.ahmedkhalifa.motionmix.ui.composable.VideoPlayerView
import kotlinx.coroutines.delay

@UnstableApi
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReelsScreen(
    navController: NavController,
    reelViewModel: ReelViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val reelsState by reelViewModel.reelsState.collectAsStateWithLifecycle(initialValue = emptyList())
    val getReelsState by reelViewModel.getReelsState.collectAsStateWithLifecycle(initialValue = Event(Resource.Init()))

    val resource = getReelsState.peekContent()
    when (resource) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is Resource.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = resource.message ?: "An error occurred")
            }
        }
        else -> {
            if (reelsState.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No reels available")
                }
            } else {
                val pagerState = rememberPagerState(pageCount = { reelsState.size })
                val currentPlayingIndex by remember { derivedStateOf { pagerState.currentPage } }

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { reelsState[it].id }
                ) { page ->
                    val reel = reelsState[page]
                    val shouldPlay = page == currentPlayingIndex

                    LaunchedEffect(page) {
                        val nextPage = page + 1
                        if (nextPage < reelsState.size) {
                            val nextReel = reelsState[nextPage]
                            val preloader = ExoPlayer.Builder(context).build().apply {
                                setMediaItem(MediaItem.fromUri(nextReel.mediaUrl))
                                prepare()
                                pause()
                            }
                            delay(5000)
                            preloader.release()
                            Log.d("ReelsScreen", "Preloaded and released: ${nextReel.mediaUrl}")
                        }
                    }

                    VideoPlayerScreen(
                        reel = reel,
                        shouldPlay = shouldPlay,
                        reelViewModel = reelViewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    reel: Reel,
    shouldPlay: Boolean,
    reelViewModel: ReelViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val progress = remember { mutableStateOf(0f) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val showBottomSheet = remember { mutableStateOf(false) }
    val isMuted by reelViewModel.isMuted.collectAsStateWithLifecycle()
    val TAG = "VideoPlayerScreen"

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(reel.mediaUrl))
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
            Log.d(TAG, "ExoPlayer released for ${reel.mediaUrl}")
        }
    }

    LaunchedEffect(shouldPlay) {
        if (shouldPlay) {
            player.volume = if (isMuted) 0f else 1f
            player.play()
        } else {
            player.pause()
        }
    }

    LaunchedEffect(isMuted) {
        player.volume = if (isMuted) 0f else 1f
    }

    LaunchedEffect(player, shouldPlay) {
        if (shouldPlay) {
            while (true) {
                if (player.isPlaying) {
                    val duration = player.duration
                    val position = player.currentPosition
                    progress.value = if (duration > 0) position.toFloat() / duration else 0f
                    isLoading.value = false
                }
                delay(1000)
            }
        }
    }

    DisposableEffect(player, shouldPlay) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isLoading.value = state == Player.STATE_BUFFERING || state == Player.STATE_IDLE
                Log.d(
                    TAG, "Playback state for ${reel.mediaUrl}: ${
                        when (state) {
                            Player.STATE_IDLE -> "IDLE"
                            Player.STATE_BUFFERING -> "BUFFERING"
                            Player.STATE_READY -> "READY"
                            Player.STATE_ENDED -> "ENDED"
                            else -> "UNKNOWN"
                        }
                    }"
                )
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(
                    TAG,
                    "Playback error for ${reel.mediaUrl}: ${error.message}, code: ${error.errorCode}"
                )
                errorMessage.value = when {
                    error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                        "The video link is invalid. Click to try again."
                    error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                        "Network error. Check your connection and press to try again."
                    error.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED ->
                        "Video playback failed. The file may be corrupted."
                    else -> "Video loading failed: ${error.message}. Click to try again."
                }
                isLoading.value = false
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    isLoading.value = false
                    Log.d(TAG, "Video playing: ${reel.mediaUrl}")
                }
            }
        }

        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            Log.d(TAG, "Player listener removed for ${reel.mediaUrl}")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VideoPlayerView(
            player = player,
            thumbnailUrl = reel.thumbnailUrl,
            isLoading = isLoading,
            errorMessage = errorMessage
        )
        VideoOverlay(
            reel = reel,
            isMuted = isMuted,
            player = player,
            isLoading = isLoading.value,
            onToggleLike = {
                reelViewModel.toggleLike(reel.id)
            },
            onToggleMute = {
                reelViewModel.toggleMute(player)
            },
            onCommentClick = {
                 //navController.navigate("comments/${reel.id}")
            },
            onShareClick = {
                reelViewModel.incrementShares(reel.id)
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Check out this reel: ${reel.mediaUrl}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Reel"))
            },
            onMoreClick = {
                showBottomSheet.value = true
            }
        )
        LinearProgressIndicator(
            progress = { progress.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.BottomCenter),
            color = Color.White,
            trackColor = Color.Gray.copy(alpha = 0.5f)
        )

        // Bottom Sheet for More Options
        if (showBottomSheet.value) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet.value = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "More Options",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TextButton(onClick = {
                        showBottomSheet.value = false
                        // TODO: Implement report functionality
                        Log.d("VideoPlayerScreen", "Report clicked for reel: ${reel.id}")
                    }) {
                        Text("Report", fontSize = 16.sp, color = Color.Red)
                    }
                    TextButton(onClick = {
                        showBottomSheet.value = false
                        // TODO: Implement save functionality
                        Log.d("VideoPlayerScreen", "Save clicked for reel: ${reel.id}")
                    }) {
                        Text("Save", fontSize = 16.sp)
                    }
                    TextButton(onClick = {
                        showBottomSheet.value = false
                        val copyIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reel.mediaUrl)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(copyIntent, "Copy Reel Link"))
                    }) {
                        Text("Copy Link", fontSize = 16.sp)
                    }
                    TextButton(onClick = {
                        showBottomSheet.value = false
                    }) {
                        Text("Cancel", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Preview(showBackground = true)
@Composable
fun PreviewReelsScreen() {
    ReelsScreen(rememberNavController())
}