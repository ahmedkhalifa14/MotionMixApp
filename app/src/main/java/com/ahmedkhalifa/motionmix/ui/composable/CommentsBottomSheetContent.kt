package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.Comment
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.ui.screens.home.ReelViewModel
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    reel: Reel,
    onDismiss: () -> Unit,
    reelViewModel: ReelViewModel = hiltViewModel()
) {
    val addCommentState by reelViewModel.addCommentState.collectAsState()
    var commentText by remember { mutableStateOf("") }
    var isCommenting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isLoadingComments by remember { mutableStateOf(true) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(reel.id) {
        isLoadingComments = true
        try {
            comments = reelViewModel.getCommentsForReel(reel.id)
        } catch (e: Exception) {
            errorMessage = "Failed to load comments"
        }
        isLoadingComments = false
        focusRequester.requestFocus()
    }

    LaunchedEffect(addCommentState) {
        val currentResource = addCommentState.peekContent()
        when (currentResource) {
            is Resource.Loading -> {
                isCommenting = true
                errorMessage = null
            }

            is Resource.Error -> {
                isCommenting = false
                errorMessage = currentResource.message
            }

            is Resource.Success -> {
                isCommenting = false
                commentText = ""
                errorMessage = null
                comments = reelViewModel.getCommentsForReel(reel.id)
            }

            else -> {
                isCommenting = false
            }
        }
    }

    LaunchedEffect(comments.size) {
        if (comments.isNotEmpty()) {
            listState.animateScrollToItem(comments.size - 1)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            keyboardController?.hide()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.
                background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.comments, reel.commentsCount),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        onDismiss()
                    },
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoadingComments) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontSize = 12.sp,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Normal
                )
            }

            // Comments List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(comments.size) { index ->
                    CommentItem(comments[index])
                }

                if (comments.isEmpty() && !isLoadingComments) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight(0.8f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.there_are_no_comments_yet_be_the_first_to_comment),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AddCommentInput(
                commentText = commentText,
                onCommentTextChange = { commentText = it },
                isCommenting = isCommenting,
                onAddComment = {
                    if (commentText.isNotBlank()) {
                        reelViewModel.addComment(reel.id, commentText)
                    }
                },
                focusRequester = focusRequester
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "@${comment.userId}",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.text,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimestamp(comment.timestamp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = Montserrat
            )
        }
    }
}

@Composable
fun AddCommentInput(
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    isCommenting: Boolean,
    onAddComment: () -> Unit,
    focusRequester: FocusRequester
) {
    OutlinedTextField(
        value = commentText,
        onValueChange = onCommentTextChange,
        placeholder = { Text(stringResource(R.string.write_your_comment)) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        enabled = !isCommenting,
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
            onSend = {
                if (commentText.isNotBlank()) {
                    onAddComment()
                }
            }
        ),
        trailingIcon = {
            if (isCommenting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onAddComment()
                        }
                    },
                    enabled = commentText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.post),
                        tint = if (commentText.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}


private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}