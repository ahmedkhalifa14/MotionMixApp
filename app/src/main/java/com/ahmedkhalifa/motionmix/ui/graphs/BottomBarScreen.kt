package com.ahmedkhalifa.motionmix.ui.graphs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarScreen(
        route = Graph.HomeRoutes.REEL,
        title = "Home",
        icon = Icons.Default.Home
    )

    object Profile : BottomBarScreen(
        route = Graph.HomeRoutes.PROFILE,
        title = "Profile",
        icon = Icons.Default.Person
    )

    object Inbox : BottomBarScreen(
        route = Graph.HomeRoutes.INBOX,
        title = "Inbox",
        icon = Icons.Filled.MailOutline
    )

    object Discover : BottomBarScreen(
        route = Graph.HomeRoutes.DISCOVER,
        title = "Discover",
        icon = Icons.Outlined.Search
    )

    object PostReel : BottomBarScreen(
        route = Graph.HomeRoutes.POST_REEL,
        title = "",
        icon = Icons.Default.Add
    )
}