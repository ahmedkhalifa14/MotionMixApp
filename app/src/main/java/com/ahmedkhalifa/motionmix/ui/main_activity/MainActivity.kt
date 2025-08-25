package com.ahmedkhalifa.motionmix.ui.main_activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.ui.graphs.RootNavigationGraph
import com.ahmedkhalifa.motionmix.ui.theme.MotionMixTheme
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
                //PostReelScreen()
                // ReelsScreen(rememberNavController())
                //UserProfileFormScreen(rememberNavController())
                RootNavigationGraph(navController = rememberNavController())
            }

        }
    }
}