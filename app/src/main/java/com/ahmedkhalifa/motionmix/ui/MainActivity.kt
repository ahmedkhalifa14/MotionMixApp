package com.ahmedkhalifa.motionmix.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.ui.graphs.RootNavigationGraph
import com.ahmedkhalifa.motionmix.ui.theme.MotionMixTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MotionMixTheme {
                RootNavigationGraph(navController = rememberNavController())
            }
        }
    }
}