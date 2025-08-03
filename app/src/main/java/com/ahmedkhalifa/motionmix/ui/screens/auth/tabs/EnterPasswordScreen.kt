package com.ahmedkhalifa.motionmix.ui.screens.auth.tabs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.utils.EventObserver
import com.ahmedkhalifa.motionmix.common.utils.isPasswordValid
import com.ahmedkhalifa.motionmix.ui.composable.AuthFooterText
import com.ahmedkhalifa.motionmix.ui.composable.CreatePasswordHeader
import com.ahmedkhalifa.motionmix.ui.composable.SpacerVertical16
import com.ahmedkhalifa.motionmix.ui.graphs.BottomBarScreen
import com.ahmedkhalifa.motionmix.ui.graphs.Graph
import com.ahmedkhalifa.motionmix.ui.screens.AppPreferencesViewModel
import com.ahmedkhalifa.motionmix.ui.screens.auth.login.LoginViewModel
import com.ahmedkhalifa.motionmix.ui.theme.FooterColor
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun EnterPasswordScreen(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    loginViewModel: LoginViewModel = hiltViewModel(),
    appPreferencesViewModel: AppPreferencesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }



    LaunchedEffect(Unit) {
        loginViewModel.loginState.collect(
            EventObserver(
                onLoading = {
                    isLoading = true
                },
                onError = { message ->
                    isLoading = false
                    Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
                },
                onSuccess = {
                    isLoading = false
                    Toast.makeText(
                        context,
                        context.getString(R.string.you_have_successfully_logged_in),
                        Toast.LENGTH_SHORT
                    ).show()
                    appPreferencesViewModel.setUserLogin(true)
                    navController.navigate(Graph.HOME)                }
            )
        )
    }
    EnterPasswordScreenContent(
        isLoading = isLoading,
        onBackClick = {

        },
        onQuestionClick = {

        },
        onClickLogin = { password ->
            val email = backStackEntry.arguments?.getString("email")
            loginViewModel.loginWithEmailAndPassword(
                userEmail = email ?: "",
                userPassword = password
            )
        }
    )
}


@Composable
fun EnterPasswordScreenContent(
    isLoading: Boolean,
    onBackClick: () -> Unit = {},
    onQuestionClick: () -> Unit = {},
    onClickLogin: (String) -> Unit = {},
) {
    var passwordState by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    )
    {
        CreatePasswordHeader(
            onBackClick = {
                onBackClick()
            },
            onQuestionClick = {
                onQuestionClick()
            }
        )
        Text(
            text = stringResource(R.string.enter_your_password),
            fontFamily = Montserrat,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        SpacerVertical16()
        OutlinedTextField(
            value = passwordState,
            onValueChange = { passwordState = it },
            label = { Text(stringResource(R.string.enter_password)) },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorIndicatorColor = MaterialTheme.colorScheme.error,
                errorContainerColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            isError = passwordState.length < 8 || !isPasswordValid(passwordState)
        )
        SpacerVertical16()
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
        Button(
            onClick = {
                onClickLogin(passwordState)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, FooterColor),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            enabled = isPasswordValid(passwordState)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(
                    text = stringResource(R.string.log_in),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = Montserrat
                )
            }

        }
    }


}
