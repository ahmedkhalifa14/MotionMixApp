package com.ahmedkhalifa.motionmix.ui.screens.home

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.data.model.getSampleVideos
import com.ahmedkhalifa.motionmix.ui.composable.VideoOverlay
import com.ahmedkhalifa.motionmix.ui.composable.VideoPlayerView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay

@UnstableApi
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen() {
    val context = LocalContext.current

    val videos = remember { getSampleVideos() }
    val pagerState = rememberPagerState(pageCount = { videos.size })
    val currentPlayingIndex by remember { derivedStateOf { pagerState.currentPage } }



    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize(),
        key = { videos[it].id }
    ) { page ->
        val video = videos[page]
        val shouldPlay = page == currentPlayingIndex

        LaunchedEffect(page) {
            val nextPage = page + 1
            if (nextPage < videos.size) {
                val nextVideo = videos[nextPage]
                val preloader = ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(nextVideo.mediaUrl))
                    prepare()
                    pause()
                }
                delay(5000)
                preloader.release()
                Log.d("ReelsScreen", "Preloaded and released: ${nextVideo.mediaUrl}")
            }
        }

        VideoPlayerScreen(reel = video, shouldPlay = shouldPlay)
    }
}

@UnstableApi
@Composable
fun VideoPlayerScreen(reel: Reel, shouldPlay: Boolean) {
    val context = LocalContext.current
    val progress = remember { mutableStateOf(0f) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
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
        if (shouldPlay) player.play() else player.pause()
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