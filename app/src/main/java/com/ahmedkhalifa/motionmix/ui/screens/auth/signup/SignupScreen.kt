package com.ahmedkhalifa.motionmix.ui.screens.auth.signup

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.utils.EventObserver
import com.ahmedkhalifa.motionmix.ui.composable.AuthButton
import com.ahmedkhalifa.motionmix.ui.composable.AuthFooter
import com.ahmedkhalifa.motionmix.ui.composable.AuthFooterText
import com.ahmedkhalifa.motionmix.ui.composable.AuthHeader
import com.ahmedkhalifa.motionmix.ui.composable.AuthTitle
import com.ahmedkhalifa.motionmix.ui.composable.SpacerVertical16
import com.ahmedkhalifa.motionmix.ui.graphs.AuthScreen


@Composable
fun SignupScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = hiltViewModel()
) {
    val googleSignInState = registerViewModel.googleSignInState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        registerViewModel.googleSignInState.collect(
            EventObserver(
                onLoading = {
                    Toast.makeText(
                        context,
                        "Google Sign In loading",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = { message ->
                    Toast.makeText(
                        context,
                        "Google Sign In Error: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("GoogleSignIn", "Error: $message")
                },
                onSuccess = { googleAccountUserInfo ->
                    Toast.makeText(
                        context,
                        "Google Sign In Success: ${googleAccountUserInfo.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    //navController.navigate("home_screen")
                }
            )
        )
    }

    SignupScreenContent(
        onQuestionClick = { },
        onCloseClick = {},
        onClickAuth = {

        },
        onGoogleAuthClick = {
            registerViewModel.signInWithGoogle()
        },
        onClickEmailPhoneAuth = {
            navController.navigate("EMAIL_PHONE_TAB/Register")
        }
    )

}

@Composable
fun SignupScreenContent(
    onQuestionClick: () -> Unit,
    onCloseClick: () -> Unit,
    onClickAuth: () -> Unit,
    onGoogleAuthClick: () -> Unit,
    onClickEmailPhoneAuth: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    )
    {
        AuthHeader(
            onQuestionClick = onQuestionClick,
            onCloseClick = onCloseClick
        )
        SpacerVertical16()

        AuthTitle(
            title = "Sign up for MotionMix",
            subTitle =
                "Create a profile, follow other accounts, make your own videos, and more."
        )
        SpacerVertical16()

        AuthButton(
            text = "Use phone or email",
            onClick = {
                onClickEmailPhoneAuth()
            },
            icon = R.drawable.profile_account_icon
        )
        SpacerVertical16()
        AuthButton(
            text = "Continue with Facebook",
            onClick = {},
            icon = R.drawable.facebook
        )
        SpacerVertical16()
        AuthButton(
            text = "Continue with Google",
            onClick = {
                onGoogleAuthClick()
            },
            icon = R.drawable.google
        )
        SpacerVertical16()
        AuthButton(
            text = "Continue with Twitter",
            onClick = {},
            icon = R.drawable.twitter_logo_icon
        )
        Spacer(modifier = Modifier.weight(1f))
        AuthFooterText(
            onClickTermsOfService = { /*TODO*/ },
            onClickPrivacyPolicy = { /*TODO*/ },
            text1 = "By continuing, you agree to our ",
            text2 = "Terms of Service ",
            text3 = "and acknowledge that you have read our ",
            text4 = "Privacy Policy ",
            text5 = "to learn how we collect, use, and share your data."
        )
        AuthFooter(
            text1 = "Already have an account? ",
            text2 = "Log in",
            onClickAuth = { onClickAuth() }
        )
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSignupScreen() {
    SignupScreen(rememberNavController())
}

