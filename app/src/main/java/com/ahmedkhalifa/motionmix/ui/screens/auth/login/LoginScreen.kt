package com.ahmedkhalifa.motionmix.ui.screens.auth.login

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.helpers.splitFullName
import com.ahmedkhalifa.motionmix.common.utils.AuthMethod
import com.ahmedkhalifa.motionmix.common.utils.EventObserver
import com.ahmedkhalifa.motionmix.common.utils.GoogleSignInState
import com.ahmedkhalifa.motionmix.data.model.User
import com.ahmedkhalifa.motionmix.ui.composable.AuthButton
import com.ahmedkhalifa.motionmix.ui.composable.AuthFooter
import com.ahmedkhalifa.motionmix.ui.composable.AuthFooterText
import com.ahmedkhalifa.motionmix.ui.composable.AuthHeader
import com.ahmedkhalifa.motionmix.ui.composable.AuthTitle
import com.ahmedkhalifa.motionmix.ui.composable.SpacerVertical16
import com.ahmedkhalifa.motionmix.ui.graphs.AuthScreen
import com.ahmedkhalifa.motionmix.ui.graphs.BottomBarScreen
import com.ahmedkhalifa.motionmix.ui.graphs.Graph
import com.ahmedkhalifa.motionmix.ui.main_activity.AppPreferencesViewModel
import com.ahmedkhalifa.motionmix.ui.screens.auth.signup.RegisterViewModel
import com.ahmedkhalifa.motionmix.ui.screens.profile.UserProfileViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = hiltViewModel(),
    appPreferencesViewModel: AppPreferencesViewModel = hiltViewModel(),
    userProfileViewModel: UserProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        registerViewModel.googleSignInState.collect(
            EventObserver(
                onLoading = {

                },
                onError = { message ->
                    Toast.makeText(
                        context,
                        context.getString(R.string.google_sign_in_error, message),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("GoogleSignIn", "Error: $message")
                },
                onSuccess = { state ->
                    when (state) {
                        is GoogleSignInState.Success -> {
                            val googleAccountUserInfo = state.user
                            Toast.makeText(
                                context,
                                context.getString(
                                    R.string.google_sign_in_success,
                                    googleAccountUserInfo.toString()
                                ),
                                Toast.LENGTH_SHORT
                            ).show()

                            appPreferencesViewModel.setUserLogin(true)

                            val (firstName, lastName) = splitFullName(
                                googleAccountUserInfo.displayName ?: ""
                            )
                            userProfileViewModel.saveUserProfileData(
                                User(
                                    firstName = firstName,
                                    lastName = lastName,
                                    profilePictureLink = googleAccountUserInfo.photoUrl ?: "",
                                    email = googleAccountUserInfo.email ?: ""
                                ),googleAccountUserInfo.photoUrl?.toUri()  ,context
                            )

                            navController.navigate(BottomBarScreen.Home.route)
                        }

                        else -> {
                            // Handle unexpected state if needed
                        }
                    }
                }
            )
        )
    }

    val authMethods = listOf(
        AuthMethod(
            text = stringResource(R.string.use_phone_email_username),
            iconResId = R.drawable.profile_account_icon,
            onClick = {
                navController.navigate(context.getString(R.string.email_phone_tab_login))
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
            },

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

    LoginScreenContent(
        authMethods = authMethods,
        onQuestionClick = {

        },
        onCloseClick = {

        },
        onClickAuth = {
            navController.navigate(AuthScreen.SignUp.route)
        }
    )

}

@Composable
fun LoginScreenContent(
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
            title = stringResource(R.string.login_in_to_motionmix),
            subTitle =
                stringResource(R.string.manage_your_account_check_notification_comment_on_videos_and_more)
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
            onClickTermsOfService = {},
            onClickPrivacyPolicy = {},
            text1 = stringResource(R.string.by_continuing_you_agree_to_our),
            text2 = stringResource(R.string.terms_of_service),
            text3 = stringResource(R.string.and_acknowledge_that_you_have_read_our),
            text4 = stringResource(R.string.privacy_policy),
            text5 = stringResource(R.string.to_learn_how_we_collect_use_and_share_your_data)
        )
        SpacerVertical16()
        AuthFooter(
            text1 = stringResource(R.string.don_t_have_an_account),
            text2 = stringResource(R.string.sign_up),
            onClickAuth = { onClickAuth() }
        )
    }

}


@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PreviewLoginScreen() {
    LoginScreen(rememberNavController())
}