package com.ahmedkhalifa.motionmix.ui.screens.auth.tabs

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.ui.composable.EmailPhoneTabHeader
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat
import com.ahmedkhalifa.motionmix.ui.theme.MotionMixTheme

@Composable
fun EmailPhoneTab(
    navController: NavController,
    initialMode: String
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.phone), stringResource(R.string.email_username))
    val tabIcons = listOf(Icons.Default.Phone, Icons.Default.Email)
    val authTypes = listOf("phone", "email")

    Column(modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.background)
        .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        EmailPhoneTabHeader(
            title = initialMode,
            onBackClick = {},
            onQuestionClick = {}
        )
        AuthTopBar(
            tabs = tabs,
            icons = tabIcons,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        Crossfade(
            targetState = selectedTab,
            animationSpec = tween(durationMillis = 250),
            label = "TabTransition"
        ) { tabIndex ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                when (authTypes[tabIndex]) {
                    "email" -> EmailTabScreen(navController)
                    "phone" -> PhoneTabScreen(navController)
                }
            }
        }
    }
}


@Composable
private fun AuthTopBar(
    tabs: List<String>,
    icons: List<ImageVector>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier.fillMaxWidth(),
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                height = 4.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                text = { Text(title,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,) },
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = "$title Tab Icon"
                    )
                },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier.height(64.dp)
            )
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewEmailPhoneTab() {
    MotionMixTheme {
        EmailPhoneTab(
            navController = rememberNavController(),
            initialMode = "Login"
        )
    }
}
