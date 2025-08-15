package com.ahmedkhalifa.motionmix.ui.composable

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.ahmedkhalifa.motionmix.data.model.Reel
import androidx.compose.ui.draw.alpha
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

@OptIn(UnstableApi::class)
@Composable
fun VideoOverlay(
    reel: Reel,
    isMuted: Boolean,
    player: ExoPlayer, // Added to handle mute/unmute
    isLoading: Boolean = false,
    onToggleLike: () -> Unit,
    onToggleMute: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val alpha = remember { Animatable(if (isLoading) 0f else 1f) }

    // Fade-in/out based on loading state
    LaunchedEffect(isLoading) {
        alpha.animateTo(if (isLoading) 0f else 1f, animationSpec = spring(stiffness = Spring.StiffnessLow))
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha.value)
    ) {
        // Gradient background for text readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(150.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        )

        // Left side - User Info and Description
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "@${reel.author}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .shadow(2.dp, shape = RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reel.description,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 2,
                modifier = Modifier
                    .shadow(2.dp, shape = RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Right side - Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(reel.thumbnailUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .border(2.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )
            ActionButton(
                icon = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                text = "",
                onClick = onToggleMute
            )
            ActionButton(
                icon = if (reel.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                text = reel.likesCount.toString(),
                tint = if (reel.isLiked) Color.Red else Color.White,
                onClick = onToggleLike
            )
            ActionButton(
                icon = Icons.AutoMirrored.Filled.Comment,
                text = reel.commentsCount.toString(),
                onClick = onCommentClick
            )
            ActionButton(
                icon = Icons.Filled.Share,
                text = reel.sharesCount.toString(),
                onClick = onShareClick
            )
            ActionButton(
                icon = Icons.Filled.MoreVert,
                text = "",
                onClick = onMoreClick
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val scale = remember { Animatable(1f) }
    val description = when (icon) {
        Icons.Filled.Favorite -> "Like button, $text likes"
        Icons.Filled.FavoriteBorder -> "Like button, $text likes"
        Icons.Filled.Face -> "Comment button, $text comments"
        Icons.Filled.Share -> "Share button, $text shares"
        Icons.Filled.MoreVert -> "More options"
        Icons.AutoMirrored.Filled.VolumeUp -> "Unmute video"
        Icons.AutoMirrored.Filled.VolumeOff -> "Mute video"
        else -> ""
    }

    LaunchedEffect(tint) {
        if (tint == Color.Red) {
            scale.animateTo(1.2f, animationSpec = spring())
            scale.animateTo(1f, animationSpec = spring())
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                // Perform haptic feedback
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator?.vibrate(
                        VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(50)
                    }
                }
                onClick()
            }
            .semantics { contentDescription = description }
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(32.dp)
                .scale(scale.value)
        )
        if (text.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = text, color = Color.White, fontSize = 12.sp)
        }
    }
}