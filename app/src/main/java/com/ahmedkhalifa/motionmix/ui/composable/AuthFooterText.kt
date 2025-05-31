package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun AuthFooterText(
    onClickTermsOfService: () -> Unit,
    onClickPrivacyPolicy: () -> Unit,
    text1: String,
    text2: String,
    text3: String,
    text4: String,
    text5: String,
) {
    Text(
        text = buildAnnotatedString {
            append(text1)
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold,

                    )
            ) {
                append(text2)
            }
            append(text3)
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold,
                )
            ) {
                append(text4)
            }
            append(text5)

        },

        color = Color.Gray,
        fontWeight = FontWeight.Normal,
        fontFamily = Montserrat,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        style = TextStyle(
            lineHeight = 16.sp
        )
    )

}