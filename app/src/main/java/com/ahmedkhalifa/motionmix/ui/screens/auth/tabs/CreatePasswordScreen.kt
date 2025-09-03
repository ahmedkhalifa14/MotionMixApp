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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.utils.EventObserver
import com.ahmedkhalifa.motionmix.common.utils.isPasswordValid
import com.ahmedkhalifa.motionmix.ui.composable.CreatePasswordHeader
import com.ahmedkhalifa.motionmix.ui.composable.SpacerVertical16
import com.ahmedkhalifa.motionmix.ui.graphs.AuthScreen
import com.ahmedkhalifa.motionmix.ui.graphs.Graph
import com.ahmedkhalifa.motionmix.ui.screens.auth.signup.RegisterViewModel
import com.ahmedkhalifa.motionmix.ui.theme.AppMainColor
import com.ahmedkhalifa.motionmix.ui.theme.FooterColor
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun CreatePasswordScreen(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    registerViewModel: RegisterViewModel = hiltViewModel(),
) {
    val registerState = registerViewModel.registerState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        registerViewModel.registerState.collect(
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
                        context.getString(R.string.we_will_send_an_email_to_verify_your_email_address),
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate(AuthScreen.UserProfileForm.route) {
                        popUpTo(AuthScreen.Password.route) {
                            inclusive = true
                        }
                    }
                }
            )
        )
    }
    CreatePasswordScreenContent(
        isLoading = isLoading,
        onBackClick = {
            navController.popBackStack()
        },
        onQuestionClick = {
            // navController.navigate()
        },
        onClickContinue = { password ->
            val email = backStackEntry.arguments?.getString("email")
            registerViewModel.registerWithEmailAndPassword(
                email = email ?: "",
                password = password,
            )
            //navController.navigate()
        }
    )
}

@Composable
fun CreatePasswordScreenContent(
    isLoading: Boolean,
    onBackClick: () -> Unit = {},
    onQuestionClick: () -> Unit = {},
    onClickContinue: (String) -> Unit = {},
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
            text = stringResource(R.string.create_password),
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
                focusedIndicatorColor = AppMainColor,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                errorIndicatorColor = MaterialTheme.colorScheme.error,
                errorContainerColor = MaterialTheme.colorScheme.onError,
                disabledIndicatorColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            isError = passwordState.length < 8 || !isPasswordValid(passwordState)
        )

        Text(
            text = stringResource(R.string._8_characters_20_max),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal,
            fontFamily = Montserrat
        )
        Text(
            text = stringResource(R.string._1_letter_1_number_1_special_character),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal,
            fontFamily = Montserrat
        )
        Text(
            text = stringResource(R.string.strong_password),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal,
            fontFamily = Montserrat
        )
        Button(
            onClick = {
                onClickContinue(passwordState)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, FooterColor),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppMainColor,
                contentColor = Color.White
            ),
            enabled = isPasswordValid(passwordState)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(
                    text = stringResource(R.string._continue),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = Montserrat
                )
            }

        }
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true
)
fun CreatePasswordScreenPreview() {
    CreatePasswordScreenContent(
        isLoading = false,
        onBackClick = { },
        onQuestionClick = { },
        onClickContinue = { },
    )
}