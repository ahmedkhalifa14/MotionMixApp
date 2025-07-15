package com.ahmedkhalifa.motionmix.ui.screens.auth.tabs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.utils.EventObserver
import com.ahmedkhalifa.motionmix.ui.composable.AuthFooterText
import com.ahmedkhalifa.motionmix.ui.composable.SpacerVertical16
import com.ahmedkhalifa.motionmix.ui.screens.auth.signup.RegisterViewModel
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun OtpScreen(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    registerViewModel: RegisterViewModel = hiltViewModel()
) {
    val verificationId = backStackEntry.arguments?.getString("verificationId")
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        registerViewModel.verifyCodeState.collect(
            EventObserver(
                onError = { message ->
                    isLoading = false
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
                onLoading = {
                    isLoading = true
                },
                onSuccess = {
                    isLoading = false
                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                }
            )
        )
    }
    OtpScreenContent(
        onClickVerify = { verificationCode ->
            registerViewModel.verifyCode(verificationId ?: "", verificationCode)
        }
    )
}

@Composable
fun OtpScreenContent(
    onClickVerify: (String) -> Unit
) {
    var otpState by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 32.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            )
    ) {

        BasicTextField(
            value = otpState,
            onValueChange = { newValue ->
                if (newValue.length <= 6) {
                    otpState = newValue
                }
            },
            decorationBox = {
                Box() {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(6) { index ->
                            val number = when {
                                index >= otpState.length -> ""
                                else -> otpState[index]
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(80.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
                                    .padding(vertical = 4.dp, horizontal = 8.dp)

                            ) {
                                Text(
                                    text = number.toString(),
                                )
                            }
                        }
                    }
                }
            }

        )

        SpacerVertical16()
        AuthFooterText(
            onClickTermsOfService = { },
            onClickPrivacyPolicy = { },
            text1 = stringResource(R.string.by_continuing_you_agree_to_our),
            text2 = stringResource(R.string.terms_of_service),
            text3 = stringResource(R.string.and_acknowledge_that_you_have_read_our),
            text4 = stringResource(R.string.privacy_policy),
            text5 = stringResource(R.string.to_learn_how_we_collect_use_and_share_your_data)
        )
        SpacerVertical16()
        Button(
            onClick = {
                onClickVerify(otpState)
            },
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = otpState.length == 6,
            colors = ButtonDefaults.buttonColors(
                MaterialTheme.colorScheme.primary,
            )
        ) {
            Text(
                text = stringResource(R.string.verify),
                modifier = Modifier.padding(8.dp),
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontFamily = Montserrat
            )
        }
    }


}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OtpScreenPreview() {
    OtpScreenContent(
        onClickVerify = {}
    )
}
