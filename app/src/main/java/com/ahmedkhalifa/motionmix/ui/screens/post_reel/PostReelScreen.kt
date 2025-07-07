package com.ahmedkhalifa.motionmix.ui.screens.post_reel

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
                snackbarHostState.showSnackbar("Notification permission required for upload")
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

    // Permission denial dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text("This app requires notification permission to show upload progress. Please grant it in Settings.") },
            confirmButton = {
                Button(onClick = { showPermissionDialog = false }) {
                    Text("OK")
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = { videoPickerLauncher.launch("video/*") }) {
                    Text("Choose Video to Upload")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uploadStatus.message,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (uploadStatus.isComplete) {
                    Text(
                        text = "Upload completed successfully!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (uploadStatus.isFailed) {
                    Text(
                        text = "Upload failed: ${uploadStatus.message}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
}