package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Data class for user profiles
data class UserProfile(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val isOnline: Boolean = false
)

@Composable
fun UserProfilesSection(
    profiles: List<UserProfile>,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (profiles.isNotEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    count = profiles.size,
                    key = { index -> profiles[index].id }
                ) { index ->
                    val profile = profiles[index]
                    UserProfileItem(
                        profile = profile,
                        onClick = { onProfileClick(profile.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileItem(
    profile: UserProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(70.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile picture container
        Card(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (profile.imageUrl != null) {
                AsyncImage(
                    model = profile.imageUrl,
                    contentDescription = "${profile.name} profile picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder with user initials
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = getInitials(profile.name),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Profile name
        Text(
            text = profile.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 4.dp)
                .width(70.dp)
        )
    }
}

// Helper function to get initials from name
private fun getInitials(name: String): String {
    return name.split(" ")
        .take(2)
        .map { it.firstOrNull()?.uppercase() ?: "" }
        .joinToString("")
        .take(2)
}