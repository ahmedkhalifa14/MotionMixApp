package com.ahmedkhalifa.motionmix.ui.screens.auth.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.common.utils.isValidEmail
import com.ahmedkhalifa.motionmix.ui.composable.AuthFooterText
import com.ahmedkhalifa.motionmix.ui.composable.SpacerVertical16
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun EmailTabScreen(
    navController: NavController
) {
    EmailTabScreenContent(
        onClickNext = { email ->
            navController.navigate("PASSWORD/$email") {
                popUpTo("email") {
                    inclusive = true
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailTabScreenContent(
    onClickNext: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                top = 32.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            )
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            modifier = Modifier
                .fillMaxWidth(),
            maxLines = 1,
            label = { Text(text = "Email address") },
            placeholder = { Text(text = "Email address") },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                errorIndicatorColor = MaterialTheme.colorScheme.error,
            ),
            isError = !isValidEmail(email),
        )
        SpacerVertical16()
        AuthFooterText(
            onClickTermsOfService = { },
            onClickPrivacyPolicy = { },
            text1 = "By continuing, you agree to our ",
            text2 = "Terms of Service ",
            text3 = "and acknowledge that you have read our ",
            text4 = "Privacy Policy ",
            text5 = "to learn how we collect, use, and share your data."
        )
        SpacerVertical16()
        Button(
            onClick = {
                if (isValidEmail(email)) {
                    onClickNext(email)
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = isValidEmail(email),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Next", modifier = Modifier.padding(8.dp),
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontFamily = Montserrat
            )
        }
    }

}

@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PreviewEmailTabScreen() {
    EmailTabScreen(
        navController = rememberNavController()
    )
}