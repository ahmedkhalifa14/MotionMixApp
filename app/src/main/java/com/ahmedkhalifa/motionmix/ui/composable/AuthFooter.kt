package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedkhalifa.motionmix.ui.theme.AppMainColor
import com.ahmedkhalifa.motionmix.ui.theme.FooterColor
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun AuthFooter(text1: String, text2: String, onClickAuth: () -> Unit) {
    Text(
        buildAnnotatedString {
            append(text1)
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Montserrat,
                    color = AppMainColor,
                    fontSize = 14.sp

                )
            ) {
                append(text2)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(FooterColor)
            .padding(20.dp),
        textAlign = TextAlign.Center,
        color = Color.Black,
        fontWeight = FontWeight.SemiBold,
        fontFamily = Montserrat,
        fontSize = 14.sp,


    )
}
