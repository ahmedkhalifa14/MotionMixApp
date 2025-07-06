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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import com.ahmedkhalifa.motionmix.common.ExoPlayerManager
import kotlinx.coroutines.delay
import kotlin.math.min

@UnstableApi
@Composable
fun VideoPlayerView(
    mediaUrl: String,
    thumbnailUrl: String,
    shouldPlay: Boolean,
    isLoading: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    modifier: Modifier = Modifier,
    onDoubleTap: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val shouldPlayState by rememberUpdatedState(shouldPlay)
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

    // Network monitoring
    DisposableEffect(Unit) {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                mainHandler.post {
                    if (shouldPlayState) {
                        ExoPlayerManager.resumePlayer()
                        Log.d(TAG, "Network available, resuming playback")
                    }
                }
            }

            override fun onLost(network: Network) {
                mainHandler.post {
                    ExoPlayerManager.pausePlayer()
                    errorMessage.value = "فقدان الاتصال بالإنترنت. اضغط لإعادة المحاولة."
                    isLoading.value = false
                    Log.w(TAG, "Network lost, pausing playback")
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    // Network retry and loading timeout
    LaunchedEffect(shouldPlayState, mediaUrl, retryCount.intValue) {
        var attempts = retryCount.intValue
        val maxAttempts = 3
        val maxLoadingTime = 8000L // Reduced to 8s
        var loadingStartTime = 0L

        while (attempts < maxAttempts && shouldPlayState) {
            val isOnline = connectivityManager.activeNetwork != null
            if (!isOnline) {
                errorMessage.value = "لا يوجد اتصال بالإنترنت. جارٍ إعادة المحاولة..."
                isLoading.value = true
                Log.w(TAG, "No internet, attempt ${attempts + 1}/$maxAttempts for $mediaUrl")
                delay(min(1000L * (1 shl attempts), 4000)) // Exponential backoff
                attempts++
                retryCount.intValue = attempts
            } else {
                if (ExoPlayerManager.playMedia(context, mediaUrl)) {
                    loadingStartTime = System.currentTimeMillis()
                    while (isLoading.value && System.currentTimeMillis() - loadingStartTime < maxLoadingTime) {
                        loadingProgress.value = ((System.currentTimeMillis() - loadingStartTime) / maxLoadingTime.toFloat()).coerceIn(0f, 1f)
                        delay(100)
                    }
                    if (isLoading.value) {
                        errorMessage.value = if (networkSpeed < 150) {
                            "الشبكة بطيئة جدًا ($networkSpeed kbps). اضغط لإعادة المحاولة."
                        } else {
                            "انتهت مهلة التحميل. اضغط لإعادة المحاولة."
                        }
                        isLoading.value = false
                        Log.e(TAG, "Loading timeout for $mediaUrl after $maxLoadingTime ms")
                        // Downgrade quality on timeout
                        if (attempts < maxAttempts - 1) {
                            val player = ExoPlayerManager.getPlayer(context)
                            mainHandler.post {
                                player.trackSelector?.parameters?.buildUpon()
                                    ?.setMaxVideoBitrate(150_000) // Force lower bitrate
                                    ?.build()?.let {
                                        player.trackSelector?.setParameters(
                                            it
                                        )
                                    }
                            }
                            Log.d(TAG, "Downgraded quality to 150 kbps for $mediaUrl")
                        }
                    }
                } else {
                    errorMessage.value = "فشل تحميل الفيديو. اضغط لإعادة المحاولة."
                    isLoading.value = false
                    Log.e(TAG, "Failed to start playback for $mediaUrl")
                }
                break
            }
        }
        if (attempts >= maxAttempts) {
            errorMessage.value = "لا يوجد اتصال بالإنترنت. اضغط لإعادة المحاولة."
            isLoading.value = false
            Log.e(TAG, "Max retry attempts reached for $mediaUrl")
        }
    }

    DisposableEffect(lifecycleOwner, shouldPlayState, mediaUrl) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (shouldPlayState) {
                        ExoPlayerManager.playMedia(context, mediaUrl)
                        Log.d(TAG, "Lifecycle ON_START: Playing $mediaUrl")
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    ExoPlayerManager.pausePlayer()
                    Log.d(TAG, "Lifecycle ON_STOP: Paused")
                }
                Lifecycle.Event.ON_DESTROY -> {
                    ExoPlayerManager.releasePlayer()
                    Log.d(TAG, "Lifecycle ON_DESTROY: Player released")
                }
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
                val exoPlayer = ExoPlayerManager.getPlayer(context)
                mainHandler.post {
                    if (exoPlayer.isPlaying) {
                        ExoPlayerManager.pausePlayer()
                        Log.d(TAG, "User clicked: Paused")
                    } else {
                        ExoPlayerManager.resumePlayer()
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
                    player = ExoPlayerManager.getPlayer(ctx)
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { it.player = ExoPlayerManager.getPlayer(context) }
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
                    if (networkSpeed < 150) {
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
                        retryCount.intValue = 0 // Reset retry count
                        ExoPlayerManager.playMedia(context, mediaUrl)
                        Log.d(TAG, "User tapped to retry: $mediaUrl")
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