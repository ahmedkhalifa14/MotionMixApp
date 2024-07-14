package com.ahmedkhalifa.motionmix.screens.auth.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedkhalifa.motionmix.composable.AuthFooterText
import com.ahmedkhalifa.motionmix.composable.SpacerVertical16
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat


@Composable
fun  PhoneTabScreen() {
    PhoneTabScreenContent("", onTextChange = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneTabScreenContent(
    state: String,
    onTextChange: (String) -> Unit

) {
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
        TextField(
            value = state,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth(),
            label = { Text(text = "Phone number") },
            placeholder = { Text(text = "Phone number") },
            colors = TextFieldDefaults.textFieldColors(containerColor = Color.White)
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
            onClick = {},
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = false,
            colors = ButtonDefaults.buttonColors(Color.White)
        ) {
            Text(
                text = "Send Code", modifier = Modifier.padding(8.dp),
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontFamily = Montserrat
            )
        }
    }

}

@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PreviewPhoneTabScreen() {
    PhoneTabScreen()
}