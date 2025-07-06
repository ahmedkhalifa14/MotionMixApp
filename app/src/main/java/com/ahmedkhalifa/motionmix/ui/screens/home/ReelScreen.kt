package com.ahmedkhalifa.motionmix.ui.screens.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.ahmedkhalifa.motionmix.common.ExoPlayerManager
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.data.model.getSampleVideos
import com.ahmedkhalifa.motionmix.ui.composable.VideoOverlay
import com.ahmedkhalifa.motionmix.ui.composable.VideoPlayerView
import kotlinx.coroutines.delay

@UnstableApi
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen() {
    val videos = remember { getSampleVideos() }
    val pagerState = rememberPagerState(pageCount = { videos.size })
    val currentPlayingIndex by remember { derivedStateOf { pagerState.currentPage } }
    val context = LocalContext.current
    val TAG = "ReelsScreen"

    LaunchedEffect(currentPlayingIndex) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val preloadCount = 1 // Fixed to 1 for all networks

        // Preload next video
        val nextIndex = currentPlayingIndex + 1
        if (nextIndex < videos.size) {
            ExoPlayerManager.preloadMedia(context, videos[nextIndex].mediaUrl)
            Log.d(TAG, "Preloading video at index $nextIndex: ${videos[nextIndex].mediaUrl}")
        }

        // Clean up preloaded items
        ExoPlayerManager.clearExcessMediaItems(context)
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        key = { videos[it].id }
    ) { page ->
        val video = videos[page]
        val shouldPlay = page == currentPlayingIndex
        VideoPlayerScreen(reel = video, shouldPlay = shouldPlay)
    }
}

@UnstableApi
@Composable
fun VideoPlayerScreen(reel: Reel, shouldPlay: Boolean) {
    val context = LocalContext.current
    val exoPlayer = ExoPlayerManager.getPlayer(context)
    val progress = remember { mutableStateOf(0f) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val TAG = "VideoPlayerScreen"

    // Update progress less frequently
    LaunchedEffect(exoPlayer, shouldPlay) {
        if (shouldPlay) {
            while (true) {
                if (exoPlayer.isPlaying) {
                    val duration = exoPlayer.duration
                    val position = exoPlayer.currentPosition
                    progress.value = if (duration > 0) position.toFloat() / duration else 0f
                    isLoading.value = false
                }
                delay(1000) // Update every 1s
            }
        }
    }

    DisposableEffect(exoPlayer, shouldPlay) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isLoading.value = state == Player.STATE_BUFFERING || state == Player.STATE_IDLE
                Log.d(TAG, "Playback state for ${reel.mediaUrl}: ${when (state) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }}")
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Playback error for ${reel.mediaUrl}: ${error.message}, code: ${error.errorCode}")
                errorMessage.value = when {
                    error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                        "رابط الفيديو غير صالح. اضغط لإعادة المحاولة."
                    error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                        "خطأ في الشبكة. تحقق من الاتصال واضغط لإعادة المحاولة."
                    error.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED ->
                        "فشل تشغيل الفيديو. قد يكون الملف تالف."
                    else -> "فشل تحميل الفيديو: ${error.message}. اضغط لإعادة المحاولة."
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

        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            Log.d(TAG, "Player listener removed")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VideoPlayerView(
            mediaUrl = reel.mediaUrl,
            thumbnailUrl = reel.thumbnailUrl,
            shouldPlay = shouldPlay,
            isLoading = isLoading,
            errorMessage = errorMessage
        )
        VideoOverlay(reel = reel, isLoading = isLoading.value)
        LinearProgressIndicator(
            progress = { progress.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.BottomCenter),
            color = Color.White,
            trackColor = Color.Gray.copy(alpha = 0.5f)
        )
    }
}