package com.ahmedkhalifa.motionmix.ui.screens.splashscreen

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.ui.graphs.Graph
import com.ahmedkhalifa.motionmix.ui.screens.AppPreferencesViewModel
import com.ahmedkhalifa.motionmix.ui.theme.AppMainColor
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue


@Composable
fun SplashScreen(
    navController: NavController
) {
    SplashScreenContent(navController)
}

@Composable
private fun SplashScreenContent(
    navController: NavController,
    appPreferencesViewModel: AppPreferencesViewModel = hiltViewModel()
) {
    val isUserLoggedIn by appPreferencesViewModel.isUserLogin.collectAsState(initial = null)
    appPreferencesViewModel.checkUserLogin()
    val scale = remember {
        Animatable(0f)
    }
    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn != null) {
            scale.animateTo(
                targetValue = 0.7f,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = {
                        OvershootInterpolator(2f).getInterpolation(it)
                    }
                )
            )
            delay(3000L)
            when {
                !isUserLoggedIn!! -> {
                    navController.navigate(Graph.AUTHENTICATION) {
                        popUpTo(Graph.SPLASH) {
                            inclusive = true
                        }
                    }
                }

                else -> {
                    navController.navigate(Graph.HOME) {
                        popUpTo(Graph.SPLASH) {
                            inclusive = true
                        }
                    }
                }
            }

        }

    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(AppMainColor)

    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = stringResource(R.string.app_logo),
            modifier = Modifier.scale(scale.value)
        )
    }
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PreviewSplashScreen() {
    SplashScreen(rememberNavController())
}

