package com.ahmedkhalifa.motionmix.ui.screens.auth.select_country

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.helpers.getCountries
import com.ahmedkhalifa.motionmix.common.utils.extractPhoneCode
import com.ahmedkhalifa.motionmix.ui.composable.SpacerHorizontal16
import com.ahmedkhalifa.motionmix.ui.composable.SpacerVertical16
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat
import java.util.Locale
import java.util.*


@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCountryScreenContent(navController: NavController) {
    val searchText = rememberSaveable { mutableStateOf("") }
    val allCountries = remember { mutableStateOf(getCountries()) }

    val filteredCountries = remember(searchText.value) {
        if (searchText.value.isEmpty()) {
            allCountries.value
        } else {
            allCountries.value.filter {
                it.lowercase(Locale.getDefault())
                    .contains(searchText.value.lowercase(Locale.getDefault()))
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(id = R.drawable.cancel_close_delete_icon),
                contentDescription = "Close",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            SpacerHorizontal16()
            Text(
                text = stringResource(R.string.select_country_region),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat,
                fontSize = 16.sp,
            )
        }

        SpacerVertical16()

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = searchText.value,
            onValueChange = { searchText.value = it },
            placeholder = { Text(stringResource(R.string.search_country)) },
            trailingIcon = {
                if (searchText.value.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear text",
                        modifier = Modifier.clickable { searchText.value = "" }
                    )
                }
            }
        )

        SpacerVertical16()

        LazyColumn {
            items(filteredCountries) { item ->
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val phoneCode = extractPhoneCode(item)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("country_code", phoneCode)
                        navController.popBackStack()
                    }
                    .padding(vertical = 8.dp)
                ) {
                    Text(text = item)
                    Divider()
                }
            }
        }
    }
}



@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PreviewSelectCountryScreen() {
    SelectCountryScreenContent(rememberNavController())
}


