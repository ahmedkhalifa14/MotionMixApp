package com.ahmedkhalifa.motionmix.ui.screens.home

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.common.ExoPlayerManager
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.ui.composable.VideoOverlay
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.LocalLifecycleOwner

@UnstableApi
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReelsScreen(
    navController: NavController,
    reelViewModel: ReelViewModel = hiltViewModel()
) {
    val reelsState by reelViewModel.reelsState.collectAsStateWithLifecycle(initialValue = emptyList())
    val getReelsState by reelViewModel.getReelsState.collectAsStateWithLifecycle(
        initialValue = Event(
            Resource.Init()
        )
    )
    val playerManager = ExoPlayerManager.getInstance()

    LaunchedEffect(Unit) {
        reelViewModel.getReels()
    }

    // Monitor activity lifeCycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                Log.d("ReelsScreen", "Lifecycle: $event - Stopping and releasing player")
                playerManager.pausePlayer()
                playerManager.release()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            Log.d("ReelsScreen", "Lifecycle observer removed")
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
                        onClick = {
                            Log.d("ReelsScreen", "🔄 Retry clicked")
                            reelViewModel.getReels()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
        }

        else -> {
            if (reelsState.isEmpty()) {
                Log.d("ReelsScreen", "📭 No reels available")
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
                            onClick = {
                                Log.d("ReelsScreen", "🔄 Refresh clicked")
                                reelViewModel.getReels()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                        ) {
                            Text("Refresh", color = Color.White)
                        }
                    }
                }
            } else {
                Log.d("ReelsScreen", "🎬 Showing ${reelsState.size} reels")
                val pagerState = rememberPagerState(pageCount = { reelsState.size })
                val currentPlayingIndex by remember { derivedStateOf { pagerState.currentPage } }

                LaunchedEffect(pagerState.currentPage) {
                    Log.d("ReelsScreen", "📄 Page changed to: ${pagerState.currentPage}")
                    reelViewModel.checkAndLoadMore(pagerState.currentPage)
                }

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { index -> reelsState[index].id }
                ) { page ->
                    val reel = reelsState[page]
                    val shouldPlay = page == currentPlayingIndex

                    Log.d("ReelsScreen", "🎥 Page $page: ${reel.id}, shouldPlay: $shouldPlay")

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

    DisposableEffect(Unit) {
        onDispose {
            Log.d("ReelsScreen", "🧹 Cleaning up ReelsScreen")
            playerManager.release()
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
    val progress = remember { mutableFloatStateOf(0f) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val showBottomSheet = remember { mutableStateOf(false) }
    val isMuted by reelViewModel.isMuted.collectAsStateWithLifecycle()

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
        isLoading.value = true
        errorMessage.value = null
        progress.floatValue = 0f
    }

    // Play control
    LaunchedEffect(shouldPlay, reel.mediaUrl) {
        if (shouldPlay) {
            isLoading.value = true
            errorMessage.value = null
            delay(100) // small delay to ensure PlayerView initialization
            playerView.player = player // attach ExoPlayer to PlayerView
            playerManager.playMedia(
                mediaUrl = reel.mediaUrl,
                onReady = {
                    isLoading.value = false
                    errorMessage.value = null
                },
                onError = { error ->
                    errorMessage.value = "Failed to load video. Tap to try again."
                    isLoading.value = false
                }
            )
        } else {
            playerManager.pausePlayer()
            playerView.player = null // detach PlayerView when not playing
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
                    progress.floatValue = playerManager.getCurrentProgress()
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
        if (isLoading.value && reel.thumbnailUrl.isNotEmpty()) {
            AsyncImage(
                model = reel.thumbnailUrl,
                contentDescription = "Video thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Loading indicator
        if (isLoading.value) {
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
        errorMessage.value?.let { message ->
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
                        containerColor = Color.White.copy(
                            alpha = 0.9f
                        )
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
                                isLoading.value = true
                                errorMessage.value = null
                                playerManager.playMedia(
                                    mediaUrl = reel.mediaUrl,
                                    onReady = {
                                        isLoading.value = false
                                        errorMessage.value = null
                                    },
                                    onError = { error ->
                                        errorMessage.value =
                                            "Failed to load video. Tap to try again."
                                        isLoading.value = false
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
            isLoading = isLoading.value,
            onToggleLike = {
                reelViewModel.toggleLike(reel.id)
            },
            onToggleMute = {
                reelViewModel.toggleMute(player)
            },
            onCommentClick = {
                navController.navigate("comments/${reel.id}")
            },
            onShareClick = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Watch this video: ${reel.mediaUrl}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share video"))
            },
            onMoreClick = {
                showBottomSheet.value = true
            }
        )

        // Progress bar
        LinearProgressIndicator(
            progress = { progress.floatValue },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.BottomCenter),
            color = Color.White,
            trackColor = Color.Gray.copy(alpha = 0.5f)
        )

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
                        text = "More options",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TextButton(onClick = {
                        showBottomSheet.value = false
                    }) {
                        Text("Report", fontSize = 16.sp, color = Color.Red)
                    }
                    TextButton(onClick = {
                        showBottomSheet.value = false
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
                        context.startActivity(Intent.createChooser(copyIntent, "Copy video link"))
                    }) {
                        Text("Copy link", fontSize = 16.sp)
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

    // Clean PlayerView on exit
    DisposableEffect(reel.id) {
        onDispose {
            playerView.player = null
            playerManager.pausePlayer() // stop playback when leaving page
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Preview(showBackground = true)
@Composable
fun PreviewReelsScreen() {
    ReelsScreen(rememberNavController())
}
