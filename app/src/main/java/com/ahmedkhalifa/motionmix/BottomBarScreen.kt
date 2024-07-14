package com.ahmedkhalifa.motionmix

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarScreen(
        route = "HOME",
        title = "Home",
        icon = Icons.Default.Home
    )

    object Profile : BottomBarScreen(
        route = "PROFILE",
        title = "Me",
        icon = Icons.Default.Person
    )

    object Inbox : BottomBarScreen(
        route = "INBOX",
        title = "Inbox",
        icon = Icons.Filled.MailOutline
    )

    object Discover : BottomBarScreen(
        route = "DISCOVER",
        title = "Discover",
        icon = Icons.Outlined.Search
    )

    object PostReel : BottomBarScreen(
        route = "POST_REAL",
        title = "",
        icon = Icons.Default.Add
    )
}
