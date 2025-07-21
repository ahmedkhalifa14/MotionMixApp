package com.ahmedkhalifa.motionmix.ui.screens.post_reel

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.utils.UploadEvent
import com.ahmedkhalifa.motionmix.ui.theme.AppMainColor
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat
import kotlinx.coroutines.launch

@Composable
fun PostReelScreen(viewModel: UploadVideoViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uploadEvent by viewModel.uploadEvent.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showPermissionDialog by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var thumbnailBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var hasLaunchedPicker by remember { mutableStateOf(false) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedVideoUri = it
            thumbnailBitmap = generateVideoThumbnail(context, it)
        } ?: coroutineScope.launch {
            snackbarHostState.showSnackbar(context.getString(R.string.failed_to_select_video))
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showPermissionDialog = true
            coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.notification_permission_required_for_upload_progress))
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLaunchedPicker) {
            hasLaunchedPicker = true
            videoPickerLauncher.launch("video/*")
        }
    }

    LaunchedEffect(uploadEvent) {
        if (uploadEvent.isComplete && selectedVideoUri != null) {
            val mediaUrl = uploadEvent.message
            val thumbnailUrl = mediaUrl.replace(".mp4", "_thumb.jpg")
            viewModel.saveReel(mediaUrl, thumbnailUrl, description) { result ->
                result.onSuccess {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.reel_saved_successfully))
                    }
                    selectedVideoUri = null
                    description = ""
                    thumbnailBitmap = null
                    viewModel.resetStatus()
                }.onFailure {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Failed to save reel: ${it.message}")
                    }
                }
            }
        }
    }

    PostReelScreenContent(
        selectedVideoUri = selectedVideoUri,
        thumbnailBitmap = thumbnailBitmap,
        uploadEvent = uploadEvent,
        showPermissionDialog = showPermissionDialog,
        snackbarHostState = snackbarHostState,
        description = description,
        onDescriptionChange = { description = it },
        onSelectVideo = { videoPickerLauncher.launch("video/*") },
        onPost = {
            selectedVideoUri?.let { viewModel.uploadVideo(it) }
        },
        onOpenSettings = {
            showPermissionDialog = false
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            )
        },
        onDismissPermissionDialog = { showPermissionDialog = false }
    )
}

@Composable
fun PostReelScreenContent(
    selectedVideoUri: Uri?,
    thumbnailBitmap: Bitmap?,
    uploadEvent: UploadEvent,
    showPermissionDialog: Boolean,
    snackbarHostState: SnackbarHostState,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onSelectVideo: () -> Unit,
    onPost: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissPermissionDialog: () -> Unit
) {
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = onDismissPermissionDialog,
            title = { Text("Permission required") },
            text = { Text("This app requires notification permission. Please grant it in settings.") },
            confirmButton = {
                Button(onClick = onOpenSettings) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissPermissionDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (selectedVideoUri == null) {
            Button(
                onClick = onSelectVideo,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppMainColor
                )
            ) {
                Text(
                    text = "Select Video",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.post_reel),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    val isUploading =
                        uploadEvent.progress > 0 && !uploadEvent.isComplete && !uploadEvent.isFailed

                    Button(
                        onClick = onPost,
                        shape = RoundedCornerShape(10),
                        enabled = !isUploading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppMainColor,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                            disabledContainerColor = AppMainColor.copy(alpha = 0.5f), // لون مختلف عند التعطيل
                            disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.post),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Description TextField
                TextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    placeholder = {
                        Text(stringResource(R.string.say_something_about_this_video))
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedPlaceholderColor = Color.Gray,
                        focusedPlaceholderColor = Color.LightGray
                    )
                )

                // Thumbnail
                thumbnailBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                // Upload progress
                if (uploadEvent.progress > 0 && !uploadEvent.isComplete && !uploadEvent.isFailed) {
                    LinearProgressIndicator(
                        progress = { uploadEvent.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = AppMainColor,
                    )
                }

                // Upload status
                when {
                    uploadEvent.isComplete -> {
                        Text(
                            "Upload completed successfully.",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    uploadEvent.isFailed -> {
                        Text(
                            "Upload failed: ${uploadEvent.message}",
                            color = Color.Red,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}


fun generateVideoThumbnail(context: Context, uri: Uri): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        retriever.release()
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
