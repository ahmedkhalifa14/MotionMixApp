package com.ahmedkhalifa.motionmix.ui.screens.auth.signup

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.utils.AuthMethod
import com.ahmedkhalifa.motionmix.common.utils.EventObserver
import com.ahmedkhalifa.motionmix.ui.composable.AuthButton
import com.ahmedkhalifa.motionmix.ui.composable.AuthFooter
import com.ahmedkhalifa.motionmix.ui.composable.AuthFooterText
import com.ahmedkhalifa.motionmix.ui.composable.AuthHeader
import com.ahmedkhalifa.motionmix.ui.composable.AuthTitle
import com.ahmedkhalifa.motionmix.ui.composable.SpacerVertical16
import com.ahmedkhalifa.motionmix.ui.graphs.AuthScreen
import com.ahmedkhalifa.motionmix.ui.graphs.BottomBarScreen
import com.ahmedkhalifa.motionmix.ui.graphs.Graph

@Composable
fun SignupScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = hiltViewModel()
) {
    registerViewModel.googleSignInState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        registerViewModel.googleSignInState.collect(
            EventObserver(
                onLoading = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.google_sign_in_loading),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = { message ->
                    Toast.makeText(
                        context,
                        context.getString(R.string.google_sign_in_error, message),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("GoogleSignIn", "Error: $message")
                },
                onSuccess = { googleAccountUserInfo ->
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.google_sign_in_success,
                            googleAccountUserInfo.toString()
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.navigate(BottomBarScreen.Home.route)
                }
            )
        )
    }
    val authMethods = listOf(
        AuthMethod(
            text = stringResource(R.string.use_phone_email_username),
            iconResId = R.drawable.profile_account_icon,
            onClick = {
                Toast.makeText(context, "clicked", Toast.LENGTH_LONG).show()
                navController.navigate("EMAIL_PHONE_TAB/Register")
            },
            tint = MaterialTheme.colorScheme.onBackground
        ),
        AuthMethod(
            text = stringResource(R.string.continue_with_facebook),
            iconResId = R.drawable.facebook,
            onClick = {
                // handle facebook login
            }
        ),
        AuthMethod(
            text = stringResource(R.string.continue_with_google),
            iconResId = R.drawable.google,
            onClick = {
                registerViewModel.signInWithGoogle()
            }
        ),
        AuthMethod(
            text = stringResource(R.string.continue_with_x),
            iconResId = R.drawable.x,
            onClick = {
                // handle X login
            },
            tint = MaterialTheme.colorScheme.onBackground
        ),
        AuthMethod(
            text = stringResource(R.string.continue_with_instagram),
            iconResId = R.drawable.instagram_icon,
            onClick = {
                // handle instagram login
            }
        )
    )
    SignupScreenContent(
        authMethods = authMethods,
        onQuestionClick = { },
        onCloseClick = {},
        onClickAuth = {
            Toast.makeText(context, "clicked", Toast.LENGTH_LONG).show()
            navController.navigate(AuthScreen.Login.route)
        },
    )

}

@Composable
fun SignupScreenContent(
    authMethods: List<AuthMethod>,
    onQuestionClick: () -> Unit,
    onCloseClick: () -> Unit,
    onClickAuth: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
            title = stringResource(R.string.sign_up_for_motionmix),
            subTitle =
                stringResource(R.string.create_a_profile_follow_other_accounts_make_your_own_videos_and_more)
        )
        SpacerVertical16()
        authMethods.forEach { authMethod ->
            AuthButton(
                text = authMethod.text,
                onClick = {
                    authMethod.onClick()
                },
                icon = authMethod.iconResId,
                tint = authMethod.tint
            )
            SpacerVertical16()
        }

        Spacer(modifier = Modifier.weight(1f))
        AuthFooterText(
            onClickTermsOfService = { },
            onClickPrivacyPolicy = { },
            text1 = stringResource(R.string.by_continuing_you_agree_to_our),
            text2 = stringResource(R.string.terms_of_service),
            text3 = stringResource(R.string.and_acknowledge_that_you_have_read_our),
            text4 = stringResource(R.string.privacy_policy),
            text5 = stringResource(R.string.to_learn_how_we_collect_use_and_share_your_data)
        )
        AuthFooter(
            text1 = stringResource(R.string.already_have_an_account),
            text2 = stringResource(R.string.log_in),
            onClickAuth = { onClickAuth() }
        )
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSignupScreen() {
    SignupScreen(rememberNavController())
}

