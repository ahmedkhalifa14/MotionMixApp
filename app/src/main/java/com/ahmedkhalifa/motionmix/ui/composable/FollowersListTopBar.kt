package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersListTopBar(
    username: String,
    onBackClick: () -> Unit,
    onFindFriendsClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = username,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {

            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_arrow),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

        },
        actions = {
            IconButton(onClick = onFindFriendsClick) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "find friends",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}
