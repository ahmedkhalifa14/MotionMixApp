package com.ahmedkhalifa.motionmix.ui.composable

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedkhalifa.motionmix.data.model.Reel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelBottomSheet(
    reel: Reel,
    onDismiss: () -> Unit,
    onReport: () -> Unit,
    onSave: () -> Unit,
    onCopyLink: (Context) -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "More options",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextButton(onClick = onReport) {
                Text("Report", fontSize = 16.sp, color = Color.Red)
            }
            TextButton(onClick = onSave) {
                Text("Save", fontSize = 16.sp)
            }
            TextButton(onClick = { onCopyLink(context) }) {
                Text("Copy link", fontSize = 16.sp)
            }
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontSize = 16.sp)
            }
        }
    }
}