package com.ahmedkhalifa.motionmix.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.source.MediaSource
import java.io.File
import kotlin.math.min

@UnstableApi
object ExoPlayerManager {
    private var exoPlayer: ExoPlayer? = null
    private var currentMediaItem: MediaItem? = null
    private const val MAX_PRELOADED_ITEMS = 1 // Reduced to 1 for all networks
    private const val TAG = "ExoPlayerManager"
    private var simpleCache: SimpleCache? = null
    private var cacheDataSourceFactory: CacheDataSource.Factory? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    @Synchronized
    fun getPlayer(context: Context): ExoPlayer {
        if (exoPlayer == null) {
            // Initialize cache
            if (simpleCache == null) {
                val cacheDir = File(context.cacheDir, "media_cache")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val evictor = LeastRecentlyUsedCacheEvictor(2000 * 1024 * 1024) // 2 GB cache
                simpleCache = SimpleCache(cacheDir, evictor)
            }

            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setConnectTimeoutMs(20000)
                .setReadTimeoutMs(20000)
                .setAllowCrossProtocolRedirects(true)

            val defaultDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

            cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(simpleCache!!)
                .setUpstreamDataSourceFactory(defaultDataSourceFactory)
                .setCacheWriteDataSinkFactory(CacheDataSink.Factory().setCache(simpleCache!!))
                .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                .setCacheKeyFactory { dataSpec -> dataSpec.uri.toString() }

            val mediaSourceFactory = DefaultMediaSourceFactory(cacheDataSourceFactory!!)

            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            val maxBitrate = when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> 6_000_000 // 6 Mbps
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> 300_000 // 300 kbps
                else -> 150_000 // Fallback
            }

            val trackSelector = DefaultTrackSelector(context).apply {
                setParameters(buildUponParameters()
                    .setMaxVideoBitrate(maxBitrate)
                    .setForceLowestBitrate(false)
                    .setAllowVideoMixedMimeTypeAdaptiveness(true))
            }

            exoPlayer = ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .setLoadControl(
                    DefaultLoadControl.Builder()
                        .setBufferDurationsMs(
                            35000,  // minBufferMs - increased
                            180000, // maxBufferMs - increased
                            3000,   // bufferForPlaybackMs
                            3000    // bufferForPlaybackAfterRebufferMs
                        )
                        .setPrioritizeTimeOverSizeThresholds(true)
                        .build()
                )
                .setMediaSourceFactory(mediaSourceFactory)
                .build()

            exoPlayer?.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e(TAG, "Playback error: ${error.message}, cause: ${error.cause}, code: ${error.errorCode}")
                    when (error.errorCode) {
                        PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> Log.e(TAG, "Network permission denied")
                        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> Log.e(TAG, "Media file not found")
                        PlaybackException.ERROR_CODE_DECODING_FAILED -> Log.e(TAG, "Decoding failed, check codec")
                        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> Log.e(TAG, "Bad HTTP status, check URL")
                        else -> Log.e(TAG, "Unknown error: ${error.errorCode}")
                    }
                }

                override fun onPlaybackStateChanged(state: Int) {
                    val networkInfo = connectivityManager.activeNetworkInfo
                    when (state) {
                        Player.STATE_BUFFERING -> Log.d(TAG, "Buffering... Network: ${networkInfo?.typeName ?: "None"}, Speed: ${capabilities?.linkDownstreamBandwidthKbps ?: 0} kbps")
                        Player.STATE_READY -> Log.d(TAG, "Ready to play")
                        Player.STATE_ENDED -> Log.d(TAG, "Playback ended")
                    }
                }
            })

            Log.d(TAG, "ExoPlayer initialized with maxBitrate: $maxBitrate")
        }
        return exoPlayer!!
    }

    @Synchronized
    fun playMedia(context: Context, mediaUrl: String, retryCount: Int = 0): Boolean {
        val player = getPlayer(context)
        val newMediaItem = MediaItem.fromUri(mediaUrl)

        if (newMediaItem != currentMediaItem) {
            val mediaSource = buildMediaSource(newMediaItem)
            mainHandler.post {
                player.setMediaSource(mediaSource)
                player.prepare()
            }
            currentMediaItem = newMediaItem
        }

        try {
            mainHandler.post { player.playWhenReady = true }
            Log.d(TAG, "Playing media: $mediaUrl")
            clearExcessMediaItems(context)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Playback failed, attempt ${retryCount + 1}: ${e.message}", e)
            if (retryCount < 2) {
                Thread.sleep(min(1000L * (1 shl retryCount), 4000)) // Exponential backoff
                mainHandler.post {
                    player.trackSelector?.parameters?.buildUpon()
                        ?.setMaxVideoBitrate(150_000) // Force lower bitrate
                        ?.build()?.let {
                            player.trackSelector?.setParameters(
                                it
                            )
                        }
                }
                return playMedia(context, mediaUrl, retryCount + 1)
            }
            return false
        }
    }

    @Synchronized
    fun preloadMedia(context: Context, mediaUrl: String) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities?.linkDownstreamBandwidthKbps ?: 0 < 150) {
            Log.d(TAG, "Skipping preload due to slow network: $mediaUrl")
            return
        }

        val player = getPlayer(context)
        val mediaItem = MediaItem.fromUri(mediaUrl)

        if (!player.getMediaItems().contains(mediaItem) && player.mediaItemCount < MAX_PRELOADED_ITEMS) {
            val mediaSource = buildMediaSource(mediaItem)
            mainHandler.post { player.addMediaSource(mediaSource) }
            Log.d(TAG, "Preloaded media: $mediaUrl")
        } else {
            Log.d(TAG, "Media already in queue or max preloaded items reached: $mediaUrl")
        }
    }

    private fun buildMediaSource(mediaItem: MediaItem): MediaSource {
        val dataSourceFactory = cacheDataSourceFactory ?: throw IllegalStateException("CacheDataSourceFactory not initialized")
        return when {
            mediaItem.localConfiguration?.uri.toString().endsWith(".m3u8") -> {
                HlsMediaSource.Factory(dataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(mediaItem)
                    .also { Log.d(TAG, "HLS source created for: ${mediaItem.localConfiguration?.uri}") }
            }
            mediaItem.localConfiguration?.uri.toString().endsWith(".mpd") -> {
                DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
                    .also { Log.d(TAG, "DASH source created for: ${mediaItem.localConfiguration?.uri}") }
            }
            else -> {
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
                    .also { Log.d(TAG, "Progressive source created for: ${mediaItem.localConfiguration?.uri}") }
            }
        }
    }

    @Synchronized
    fun pausePlayer() {
        mainHandler.post { exoPlayer?.playWhenReady = false }
        Log.d(TAG, "Player paused")
    }

    @Synchronized
    fun resumePlayer() {
        mainHandler.post { exoPlayer?.playWhenReady = true }
        Log.d(TAG, "Player resumed")
    }

    @Synchronized
    fun releasePlayer() {
        mainHandler.post {
            exoPlayer?.release()
            exoPlayer = null
        }
        currentMediaItem = null
        simpleCache?.release()
        simpleCache = null
        cacheDataSourceFactory = null
        Log.d(TAG, "Player released and cache cleared")
    }

    @Synchronized
    fun clearExcessMediaItems(context: Context) {
        exoPlayer?.let { player ->
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            val maxPreload = MAX_PRELOADED_ITEMS

            val currentIndex = player.currentMediaItemIndex
            val mediaItems = player.getMediaItems()
            val itemsToRemove = mutableListOf<Int>()

            for (i in mediaItems.indices.reversed()) {
                if (i < currentIndex || i > currentIndex + maxPreload) {
                    itemsToRemove.add(i)
                }
            }
            mainHandler.post {
                itemsToRemove.forEach { player.removeMediaItem(it) }
            }
            Log.d(TAG, "Removed ${itemsToRemove.size} excess media items")
        }
    }

    private fun Player.getMediaItems(): List<MediaItem> {
        return (0 until mediaItemCount).map { getMediaItemAt(it) }
    }
}