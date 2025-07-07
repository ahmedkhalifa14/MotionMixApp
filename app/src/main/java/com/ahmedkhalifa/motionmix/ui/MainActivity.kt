package com.ahmedkhalifa.motionmix.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.ahmedkhalifa.motionmix.ui.screens.home.ReelsScreen
import com.ahmedkhalifa.motionmix.ui.screens.post_reel.PostReelScreen
import com.ahmedkhalifa.motionmix.ui.theme.MotionMixTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MotionMixTheme {
                PostReelScreen()
               // RootNavigationGraph(navController = rememberNavController())
            }
        }
    }
}