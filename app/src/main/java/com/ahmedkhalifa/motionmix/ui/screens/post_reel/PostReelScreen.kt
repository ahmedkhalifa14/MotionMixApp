package com.ahmedkhalifa.motionmix.ui.screens.post_reel

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahmedkhalifa.motionmix.data.model.UploadStatus
import kotlinx.coroutines.launch

@Composable
fun PostReelScreen(viewModel: UploadViewModel = viewModel()) {
    val context = LocalContext.current
    val uploadStatus by viewModel.uploadStatus.collectAsState(initial = UploadStatus())
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showPermissionDialog by remember { mutableStateOf(false) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.startUpload(it)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Upload started")
            }
        } ?: coroutineScope.launch {
            snackbarHostState.showSnackbar("Failed to select video")
        }
    }

    // Request notification permission for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showPermissionDialog = true
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notification permission required for upload progress")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    PostReelScreenContent(
        uploadStatus = uploadStatus,
        showPermissionDialog = showPermissionDialog,
        snackbarHostState = snackbarHostState,
        onSelectVideo = { videoPickerLauncher.launch("video/*") },
        onRetryUpload = { videoPickerLauncher.launch("video/*") },
        onResetUpload = { viewModel.resetUploadStatus() },
        onOpenSettings = {
            showPermissionDialog = false
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        },
        onDismissPermissionDialog = { showPermissionDialog = false }
    )
}

@Composable
fun PostReelScreenContent(
    uploadStatus: UploadStatus,
    showPermissionDialog: Boolean,
    snackbarHostState: SnackbarHostState,
    onSelectVideo: () -> Unit,
    onRetryUpload: () -> Unit,
    onResetUpload: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissPermissionDialog: () -> Unit
) {
    // Permission denial dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = onDismissPermissionDialog,
            title = { Text("Permission Required") },
            text = { Text("This app requires notification permission to show upload progress. Please grant it in Settings.") },
            confirmButton = {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.semantics { contentDescription = "Open Settings" }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissPermissionDialog,
                    modifier = Modifier.semantics { contentDescription = "Cancel" }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Upload button
                Button(
                    onClick = onSelectVideo,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .semantics { contentDescription = "Choose video to upload" }
                ) {
                    Text("Choose Video to Upload")
                }

                // Progress or status display
                when {
                    uploadStatus.isComplete -> {
                        Text(
                            text = "Upload completed successfully!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(
                            onClick = onResetUpload,
                            modifier = Modifier.semantics { contentDescription = "Start new upload" }
                        ) {
                            Text("Start New Upload")
                        }
                    }
                    uploadStatus.isFailed -> {
                        Text(
                            text = "Upload failed: ${uploadStatus.message}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = onRetryUpload,
                            modifier = Modifier.semantics { contentDescription = "Retry upload" }
                        ) {
                            Text("Retry Upload")
                        }
                    }
                    uploadStatus.progress > 0 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uploadStatus.message,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            LinearProgressIndicator(
                                progress = { uploadStatus.progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .padding(top = 8.dp)
                                    .semantics { contentDescription = "Upload progress" }
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "Select a video to start uploading",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PreviewPostReelScreenContent() {
    PostReelScreenContent(
        uploadStatus = UploadStatus(),
        showPermissionDialog = false,
        snackbarHostState = SnackbarHostState(),
        onSelectVideo = {},
        onRetryUpload = {},
        onResetUpload = {},
        onOpenSettings = {},
        onDismissPermissionDialog = {}
    )
}