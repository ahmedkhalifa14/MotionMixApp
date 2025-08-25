package com.ahmedkhalifa.motionmix.services

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("DEPRECATION")
@UnstableApi
class ExoPlayerManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: ExoPlayerManager? = null
        private const val TAG = "SinglePlayerManager"

        fun getInstance(): ExoPlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ExoPlayerManager().also { INSTANCE = it }
            }
        }
    }

    private var exoPlayer: ExoPlayer? = null
    private var currentMediaUrl: String? = null
    private var simpleCache: SimpleCache? = null
    private var cacheDataSourceFactory: CacheDataSource.Factory? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val isInitialized = AtomicBoolean(false)

    fun initializePlayer(context: Context): ExoPlayer {
        if (exoPlayer == null && !isInitialized.get()) {
            isInitialized.set(true)
            setupCache(context)
            createPlayer(context)
        }
        return exoPlayer!!
    }

    private fun setupCache(context: Context) {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "video_cache")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val cacheSize = 500L * 1024 * 1024 // 500MB
            val evictor = LeastRecentlyUsedCacheEvictor(cacheSize)
            simpleCache = SimpleCache(cacheDir, evictor)
            Log.d(TAG, "Cache initialized with size: $cacheSize bytes")
        }

        if (cacheDataSourceFactory == null) {
            val httpFactory = DefaultHttpDataSource.Factory()
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(15000)

            val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)

            cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(simpleCache!!)
                .setUpstreamDataSourceFactory(dataSourceFactory)
                .setCacheWriteDataSinkFactory(CacheDataSink.Factory().setCache(simpleCache!!))
        }
    }

    private fun createPlayer(context: Context) {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1500,  // min buffer
                5000,  // max buffer
                1000,  // buffer for playback
                1500   // buffer for playback after rebuffer
            )
            .build()

        val trackSelector = DefaultTrackSelector(context)

        exoPlayer = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory!!))
            .build()

        exoPlayer?.apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = false

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    val stateStr = when (state) {
                        Player.STATE_IDLE -> "IDLE"
                        Player.STATE_BUFFERING -> "BUFFERING"
                        Player.STATE_READY -> "READY"
                        Player.STATE_ENDED -> "ENDED"
                        else -> "UNKNOWN"
                    }
                    Log.d(TAG, "State: $stateStr for $currentMediaUrl")

                    if (state == Player.STATE_ENDED) {
                        seekTo(0)
                        play()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e(TAG, "Player error for $currentMediaUrl: ${error.message}, cause: ${error.cause}")
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    Log.d(TAG, "Playing: $isPlaying for $currentMediaUrl")
                }
            })
        }
    }

    fun playMedia(mediaUrl: String, onReady: () -> Unit = {}, onError: (String) -> Unit = {}) {
        if (exoPlayer == null) {
            Log.e(TAG, "Player not initialized")
            onError("Player not initialized")
            return
        }

        Log.d(TAG, "playMedia: $mediaUrl (current: $currentMediaUrl)")

        mainHandler.post {
            try {
                if (mediaUrl == currentMediaUrl && exoPlayer?.playbackState == Player.STATE_READY) {
                    Log.d(TAG, "Same media, resuming")
                    exoPlayer?.play()
                    onReady()
                    return@post
                }

                exoPlayer?.apply {
                    stop()
                    clearMediaItems()
                }

                mainHandler.postDelayed({
                    try {
                        val mediaItem = MediaItem.fromUri(mediaUrl)
                        exoPlayer?.apply {
                            setMediaItem(mediaItem)
                            prepare()

                            val readyListener = object : Player.Listener {
                                override fun onPlaybackStateChanged(state: Int) {
                                    if (state == Player.STATE_READY) {
                                        removeListener(this)
                                        play()
                                        onReady()
                                        Log.d(TAG, "Media ready and playing: $mediaUrl")
                                    }
                                }

                                override fun onPlayerError(error: PlaybackException) {
                                    removeListener(this)
                                    onError("Playback error: ${error.message}")
                                }
                            }

                            addListener(readyListener)
                        }

                        currentMediaUrl = mediaUrl

                    } catch (e: Exception) {
                        Log.e(TAG, "Error in delayed setup: ${e.message}")
                        onError("Setup error: ${e.message}")
                    }
                }, 300)

            } catch (e: Exception) {
                Log.e(TAG, "Error in playMedia: ${e.message}")
                onError("Play error: ${e.message}")
            }
        }
    }

    fun pausePlayer() {
        mainHandler.post {
            exoPlayer?.pause()
            Log.d(TAG, "Player paused")
        }
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    fun getCurrentProgress(): Float {
        return exoPlayer?.let { player ->
            val duration = player.duration
            val position = player.currentPosition
            if (duration > 0) position.toFloat() / duration else 0f
        } ?: 0f
    }

    fun release() {
        mainHandler.post {
            // إيقاف التشغيل أولاً لضمان توقف الصوت
            exoPlayer?.stop()
            exoPlayer?.clearMediaItems()
            Log.d(TAG, "Player stopped and media items cleared")

            // تحرير ExoPlayer
            exoPlayer?.release()
            exoPlayer = null
            currentMediaUrl = null

            // تحرير ذاكرة التخزين المؤقت
            simpleCache?.release()
            simpleCache = null
            cacheDataSourceFactory = null

            isInitialized.set(false)
            Log.d(TAG, "Player and cache fully released")
        }
    }
}