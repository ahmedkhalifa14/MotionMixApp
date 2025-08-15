package com.ahmedkhalifa.motionmix.ui.screens.home


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ahmedkhalifa.motionmix.common.utils.EventObserver
import com.ahmedkhalifa.motionmix.data.model.Comment
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    reelId: String,
    navController: NavController,
    reelViewModel: ReelViewModel = hiltViewModel()
) {
    val reelsState by reelViewModel.reelsState.collectAsState()
    val addCommentState by reelViewModel.addCommentState.collectAsState()
    val reel = reelsState.find { it.id == reelId }
    val commentText = remember { mutableStateOf("") }
    val isCommenting = remember { mutableStateOf(false) }

    // Handle add comment events
//    LaunchedEffect(addCommentState) {
//        addCommentState.collect(
//            EventObserver(
//                onError = { errorMessage ->
//                    isCommenting.value = false
//                    Log.e("CommentsScreen", "Error adding comment: $errorMessage")
//                    // You can show a snackbar here
//                },
//                onLoading = {
//                    isCommenting.value = true
//                },
//                onSuccess = { success ->
//                    isCommenting.value = false
//                    if (success) {
//                        commentText.value = "" // Clear the text field
//                        Log.d("CommentsScreen", "Comment added successfully")
//                    }
//                }
//            )
//        )
//    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comments") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reel?.comments ?: emptyList()) { comment ->
                    CommentItem(comment)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText.value,
                    onValueChange = { commentText.value = it },
                    label = { Text("Add a comment") },
                    modifier = Modifier.weight(1f),
                    enabled = !isCommenting.value
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (commentText.value.isNotBlank() && !isCommenting.value) {
                            reelViewModel.addComment(reelId, commentText.value)
                        }
                    },
                    enabled = commentText.value.isNotBlank() && !isCommenting.value
                ) {
                    if (isCommenting.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Post")
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "@${comment.userId}: ${comment.text}",
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}