package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ahmedkhalifa.motionmix.R

@Composable
fun CircularImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val defaultImage = painterResource(R.drawable.app_logo)
    val painter = rememberAsyncImagePainter(
        model = if (!imageUrl.isNullOrEmpty()) imageUrl else null,
        placeholder = defaultImage,
        error = defaultImage,
        fallback = defaultImage
    )

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            ),
        contentScale = ContentScale.Crop
    )
}
