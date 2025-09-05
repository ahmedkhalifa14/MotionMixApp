package com.ahmedkhalifa.motionmix.ui.screens.follow

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.utils.EventObserver
import com.ahmedkhalifa.motionmix.common.utils.FollowUiState
import com.ahmedkhalifa.motionmix.data.model.User
import com.ahmedkhalifa.motionmix.ui.composable.CircularImage
import com.ahmedkhalifa.motionmix.ui.composable.FollowersListTopBar
import com.ahmedkhalifa.motionmix.ui.screens.dicover.FollowViewModel
import com.ahmedkhalifa.motionmix.ui.theme.AppMainColor
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@Composable
fun FollowersAndFollowingScreen(
    followViewModel: FollowViewModel = hiltViewModel()
) {
    val uiState by followViewModel.uiState.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    // Observe follow/unfollow events
    LaunchedEffect(Unit) {
        followViewModel.followEvent.collect(
            EventObserver(
                onError = { error -> },
                onLoading = { },
                onSuccess = { }
            ))
    }

    // Load counts and initial tab
    LaunchedEffect(userId) {
        followViewModel.loadFollowCounts(userId)
        followViewModel.loadFollowing(userId, reset = true)
    }

    FollowersAndFollowingScreenContent(
        followViewModel = followViewModel,
        userId = userId,
        uiState = uiState,
        onBackClick = { },
        onFindFriendsClick = {},
        onFollowClick = { targetUserId -> followViewModel.followUser(targetUserId) },
        onUnfollowClick = { targetUserId -> followViewModel.unfollowUser(targetUserId) },
        onLoadMoreFollowers = { followViewModel.loadFollowers(userId) },
        onLoadMoreFollowing = { followViewModel.loadFollowing(userId) },
        onLoadMoreFriends = { followViewModel.loadFriends(userId) },
        onClearError = { followViewModel.clearError() },
        onTabSelected = { index ->
            when (index) {
                0 -> followViewModel.loadFollowing(userId, reset = true)
                1 -> followViewModel.loadFollowers(userId, reset = true)
                2 -> followViewModel.loadFriends(userId, reset = true)
                3 -> followViewModel.loadSuggestions()
            }
        }
    )
}

@Composable
fun FollowersAndFollowingScreenContent(
    followViewModel: FollowViewModel,
    userId: String,
    uiState: FollowUiState,
    onBackClick: () -> Unit,
    onFindFriendsClick: () -> Unit,
    onFollowClick: (String) -> Unit,
    onUnfollowClick: (String) -> Unit,
    onLoadMoreFollowers: () -> Unit,
    onLoadMoreFollowing: () -> Unit,
    onLoadMoreFriends: () -> Unit,
    onClearError: () -> Unit,
    onTabSelected: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Sync selectedTabIndex with pager state and scroll tab into view
    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
        onTabSelected(pagerState.currentPage)
        val tabWidth = 120.dp // Approximate width of each tab, adjust based on your design
        scope.launch {
            scrollState.animateScrollTo(
                (selectedTabIndex * tabWidth.value * 1.2).toInt(), // Adjust multiplier for spacing
                animationSpec = tween(durationMillis = 300)
            )
        }
    }

    Scaffold(
        topBar = {
            FollowersListTopBar(
                username = uiState.followers.firstOrNull()?.username ?: userId,
                onBackClick = onBackClick,
                onFindFriendsClick = onFindFriendsClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Custom tab row with scrollable behavior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 4.dp)
            ) {
                listOf(
                    "Following (${uiState.followingCount})",
                    "Followers (${uiState.followersCount})",
                    "Friends (${uiState.friendsCount})",
                    "Suggested"
                ).forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable {
                                selectedTabIndex = index
                                onTabSelected(index)
                                scope.launch { pagerState.animateScrollToPage(index) }
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            maxLines = 2,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            fontFamily = Montserrat
                        )
                    }
                }
            }
            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // HorizontalPager for content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> UsersList(
                        users = uiState.following,
                        isFollowingMap = uiState.isFollowingMap,
                        onFollowClick = onFollowClick,
                        onUnfollowClick = onUnfollowClick,
                        onLoadMore = onLoadMoreFollowing,
                        isLoading = uiState.isLoading
                    )

                    1 -> UsersList(
                        users = uiState.followers,
                        isFollowingMap = uiState.isFollowingMap,
                        onFollowClick = onFollowClick,
                        onUnfollowClick = onUnfollowClick,
                        onLoadMore = onLoadMoreFollowers,
                        isLoading = uiState.isLoading
                    )

                    2 -> UsersList(
                        users = uiState.friends,
                        isFollowingMap = uiState.isFollowingMap,
                        onFollowClick = onFollowClick,
                        onUnfollowClick = onUnfollowClick,
                        onLoadMore = onLoadMoreFriends,
                        isLoading = uiState.isLoading
                    )

                    3 -> UsersList(
                        users = uiState.suggestions,
                        isFollowingMap = uiState.isFollowingMap,
                        onFollowClick = onFollowClick,
                        onUnfollowClick = onUnfollowClick,
                        onLoadMore = null,
                        isLoading = uiState.isLoading
                    )
                }
            }

            // Error handling
            uiState.error?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                        Button(onClick = onClearError) {
                            Text("Clear Error")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsersList(
    users: List<User>,
    isFollowingMap: Map<String, Boolean>,
    onFollowClick: (String) -> Unit,
    onUnfollowClick: (String) -> Unit,
    onLoadMore: (() -> Unit)?,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        items(
            items = users,
            key = { user ->
                user.userId.ifEmpty { "user_${users.indexOf(user)}" }
            }
        ) { user ->
            UserRow(
                user = user,
                isFollowing = isFollowingMap[user.userId] == true,
                onFollowClick = { onFollowClick(user.userId) },
                onUnfollowClick = { onUnfollowClick(user.userId) }
            )
        }
        onLoadMore?.let {
            item {
                if (!isLoading) {
                    Button(
                        onClick = onLoadMore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Load More")
                    }
                } else {
                    Text(
                        text = "Loading...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun UserRow(
    user: User,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            CircularImage(
                user.profilePictureLink,
                size = 100.dp
            )
        }
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = "${user.firstName}${user.lastName}",
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "You may Know ${user.firstName + user.lastName}",
                fontWeight = FontWeight.Medium,
                fontFamily = Montserrat,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier.padding(6.dp),
                    onClick = {
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceVariant)
                )
                {
                    Text(
                        stringResource(R.string.remove),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Montserrat
                    )
                }
                Button(
                    modifier = Modifier.padding(6.dp),
                    onClick = if (isFollowing) onUnfollowClick else onFollowClick,
                    colors = ButtonDefaults.buttonColors(AppMainColor),

                    ) {
                    Text(
                        text = if (isFollowing) "Unfollow" else "Follow",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Montserrat
                    )
                }

            }
        }
    }
}