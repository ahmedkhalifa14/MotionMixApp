package com.ahmedkhalifa.motionmix.ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.ui.theme.FooterColor
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@Composable
fun AuthButton(text: String, icon: Int,tint: Color=Color.Unspecified ,onClick: () -> Unit) {
    OutlinedButton(
        onClick = { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, FooterColor)
//        elevation = ButtonDefaults.buttonElevation(
//            defaultElevation = 1.dp
//        ),
,
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.background)
    ) {
        Icon(
            painter = painterResource(id = icon), contentDescription = "Login Icon",
            tint = tint
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            fontFamily = Montserrat
        )


    }


}

@Preview(showSystemUi = true)
@Composable
fun PreviewAuthButton() {
    Box(modifier = Modifier.fillMaxWidth()) {
        AuthButton("Google", R.drawable.instagram_icon,Color.Unspecified){}
    }
}