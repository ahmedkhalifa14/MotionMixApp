package com.ahmedkhalifa.motionmix.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.ui.graphs.Graph
import com.ahmedkhalifa.motionmix.ui.graphs.RootNavigationGraph
import com.ahmedkhalifa.motionmix.ui.screens.auth.userProfile.UserProfileFormScreen
import com.ahmedkhalifa.motionmix.ui.screens.home.ReelsScreen
import com.ahmedkhalifa.motionmix.ui.screens.post_reel.PostReelScreen
import com.ahmedkhalifa.motionmix.ui.screens.splashscreen.SplashScreen
import com.ahmedkhalifa.motionmix.ui.theme.MotionMixTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            val navController = rememberNavController()
//            val currentRoute by navController.currentBackStackEntryAsState()
//            val isSplashScreen = currentRoute?.destination?.route == Graph.SPLASH
//            if (isSplashScreen) {
//                enableEdgeToEdge()
//            }
//            MotionMixTheme {
//                RootNavigationGraph(navController = navController)
//            }
            MotionMixTheme {
              //  PostReelScreen()
                ReelsScreen(rememberNavController())
                //UserProfileFormScreen(rememberNavController())

            }

        }
    }
}

