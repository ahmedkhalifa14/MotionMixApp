package com.ahmedkhalifa.motionmix.ui.composable

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.ui.theme.AppMainColor
import com.ahmedkhalifa.motionmix.ui.theme.AppSecondColor

@Composable
fun ProfilePictureSection(
    profilePictureUri: Uri?,
    onProfilePictureChange: (Uri?) -> Unit
) {
    var showImagePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        if (profilePictureUri == null) AppMainColor
                        else Color.Transparent
                    )
                    .clickable { showImagePicker = true },
                contentAlignment = Alignment.Center
            ) {
                if (profilePictureUri == null) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.profile),
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(profilePictureUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.profile_picture),
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )


                }
            }

            // Camera Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppSecondColor)
                    .border(3.dp, Color.White, CircleShape)
                    .clickable { showImagePicker = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = stringResource(R.string.camera),
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    // Image picker handler
    ImagePickerHandler(
        showImagePicker = showImagePicker,
        onDismiss = { showImagePicker = false },
        onImageSelected = { uri ->
            onProfilePictureChange(uri)
            showImagePicker = false
        }
    )
}
