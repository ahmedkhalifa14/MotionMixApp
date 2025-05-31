package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun AuthTitle(title:String,subTitle:String){
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title,Modifier.fillMaxWidth(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            color= Color.Black,
            textAlign= TextAlign.Center

            )
        Text(text = subTitle,
            Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = Montserrat,
            color = Color.Gray,
            textAlign= TextAlign.Center,
            style = TextStyle(
                lineHeight = 16.sp
            )
            )
    }

}