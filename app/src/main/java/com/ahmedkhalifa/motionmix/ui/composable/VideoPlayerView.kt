package com.ahmedkhalifa.motionmix.ui.composable

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlin.math.min

@UnstableApi
@Composable
fun VideoPlayerView(
    player: ExoPlayer,
    thumbnailUrl: String,
    isLoading: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    modifier: Modifier = Modifier,
    onDoubleTap: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val retryCount = remember { mutableIntStateOf(0) }
    val loadingProgress = remember { mutableStateOf(0f) }
    val TAG = "VideoPlayerView"
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkSpeed = remember {
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.linkDownstreamBandwidthKbps ?: 0
    }

    // Preload thumbnail
    LaunchedEffect(thumbnailUrl) {
        ImageRequest.Builder(context)
            .data(thumbnailUrl)
            .crossfade(true)
            .build()
            .let { context.imageLoader.enqueue(it) }
        Log.d(TAG, "Preloading thumbnail: $thumbnailUrl")
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> player.playWhenReady = true
                Lifecycle.Event.ON_STOP -> player.playWhenReady = false
                Lifecycle.Event.ON_DESTROY -> player.release()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                mainHandler.post {
                    if (player.isPlaying) {
                        player.pause()
                        Log.d(TAG, "User clicked: Paused")
                    } else {
                        player.play()
                        Log.d(TAG, "User clicked: Resumed")
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    onDoubleTap()
                    Log.d(TAG, "Double tap detected")
                })
            }
    ) {
        if (isLoading.value || errorMessage.value != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = thumbnailUrl,
                    placeholder = rememberAsyncImagePainter("https://via.placeholder.com/720x1280"),
                    error = rememberAsyncImagePainter("https://via.placeholder.com/720x1280")
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    this.player = player
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { it.player = player }
        )

        AnimatedVisibility(
            visible = isLoading.value,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.White,
                        strokeWidth = 4.dp,
                        progress = { loadingProgress.value }
                    )
                    if (networkSpeed < 100) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "شبكة بطيئة ($networkSpeed kbps)",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        errorMessage.value?.let { message ->
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp)
                    .clickable {
                        errorMessage.value = null
                        isLoading.value = true
                        retryCount.intValue = 0
                        player.seekTo(0)
                        player.playWhenReady = true
                        Log.d(TAG, "User tapped to retry")
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "اضغط لإعادة المحاولة",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
