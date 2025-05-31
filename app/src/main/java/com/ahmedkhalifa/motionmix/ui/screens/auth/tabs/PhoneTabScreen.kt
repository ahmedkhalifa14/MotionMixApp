package com.ahmedkhalifa.motionmix.ui.screens.auth.tabs

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.common.utils.EventObserver
import com.ahmedkhalifa.motionmix.common.utils.isValidPhoneNumber
import com.ahmedkhalifa.motionmix.ui.composable.AuthFooterText
import com.ahmedkhalifa.motionmix.ui.composable.SpacerVertical16
import com.ahmedkhalifa.motionmix.ui.graphs.AuthScreen
import com.ahmedkhalifa.motionmix.ui.screens.auth.signup.RegisterViewModel
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat


@Composable
fun PhoneTabScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = hiltViewModel()

) {
    val selectedCountryCode = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("country_code", null)
        ?.collectAsState()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        registerViewModel.sendVerificationCodeState.collect(
            EventObserver(
                onError = { error ->
                    isLoading = false
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                },
                onLoading = {
                    isLoading = true
                },
                onSuccess = { verificationId ->
                    isLoading = false
                    navController.navigate("OTP/$verificationId")
                }
            )
        )
    }
    PhoneTabScreenContent(
        selectedCountryCode = selectedCountryCode?.value ?: "+20",
        onClickSendCode = { phoneNumber ->
            registerViewModel.sendVerificationCode(phoneNumber, selectedCountryCode?.value ?: "+20")        },
        onClickCountryCode = { countryCode ->
            navController.navigate(AuthScreen.SelectCountryCode.route)
        }
        ,
        isLoading = isLoading
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneTabScreenContent(
    selectedCountryCode: String,
    onClickSendCode: (String) -> Unit = {},
    onClickCountryCode: (String) -> Unit = {},
    isLoading: Boolean
) {
    var phoneNumberState by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        OutlinedTextField(
            value = phoneNumberState,
            onValueChange = { phoneNumberState = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            leadingIcon = {
                Text(
                    text = selectedCountryCode,
                    modifier = Modifier
                        .clickable { onClickCountryCode("") }
                        .padding(horizontal = 8.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            singleLine = true
        )

        SpacerVertical16()

        AuthFooterText(
            onClickTermsOfService = {},
            onClickPrivacyPolicy = {},
            text1 = "By continuing, you agree to our ",
            text2 = "Terms of Service ",
            text3 = "and acknowledge that you have read our ",
            text4 = "Privacy Policy ",
            text5 = "to learn how we collect, use, and share your data."
        )

        SpacerVertical16()

        Button(
            onClick = {
                if (isValidPhoneNumber(selectedCountryCode + phoneNumberState)) {
                    onClickSendCode(phoneNumberState)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = isValidPhoneNumber(selectedCountryCode + phoneNumberState),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            if (isLoading){
                CircularProgressIndicator(color = Color.White)
            } else{
                Text(
                    text = "Send Code",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Montserrat
                )
            }

        }
    }
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PreviewPhoneTabScreen() {
    PhoneTabScreen(rememberNavController())
}