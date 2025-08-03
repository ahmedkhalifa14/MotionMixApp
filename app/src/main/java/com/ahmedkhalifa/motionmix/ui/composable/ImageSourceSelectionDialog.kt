package com.ahmedkhalifa.motionmix.ui.composable


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ahmedkhalifa.motionmix.R

@Composable
fun ImageSourceSelectionDialog(
    onDismiss: () -> Unit,
    onGallerySelected: () -> Unit,
    onCameraSelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.select_image_source))
        },
        text = {
            Text(text = stringResource(R.string.choose_image_source_message))
        },
        confirmButton = {
            TextButton(onClick = {
                onGallerySelected()
                onDismiss()
            }) {
                Text(stringResource(R.string.gallery))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onCameraSelected()
                onDismiss()
            }) {
                Text(stringResource(R.string.camera))
            }
        }
    )
}