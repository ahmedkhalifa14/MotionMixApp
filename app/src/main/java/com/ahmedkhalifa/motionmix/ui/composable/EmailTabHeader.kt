package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun EmailPhoneTabHeader(title: String, onBackClick: () -> Unit, onQuestionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            modifier = Modifier
                .size(36.dp)
                .clickable {
                    onBackClick
                },
            painter = painterResource(id = R.drawable.back_ic),
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onBackground
            )
        Text(
            text = title,
            fontFamily = Montserrat,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            modifier = Modifier
                .size(36.dp)
                .clickable {
                    onQuestionClick()
                },
            painter = painterResource(id = R.drawable.question_icon),
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}