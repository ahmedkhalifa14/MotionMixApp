package com.ahmedkhalifa.motionmix.screens.auth.select_country

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.helpers.getCountries
import com.ahmedkhalifa.motionmix.composable.SpacerHorizontal16
import com.ahmedkhalifa.motionmix.composable.SpacerVertical16
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCountryScreenContent() {
    val searchText = rememberSaveable { mutableStateOf("") }
    val countries = remember { mutableStateOf(getCountries()) }
    var filteredCountries: ArrayList<String>

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                    },
                painter = painterResource(id = R.drawable.cancel_close_delete_icon),
                contentDescription = "",

                )
            SpacerHorizontal16()
            Text(text = "Select country/region",
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
            onValueChange = {
                searchText.value = it
            },
            trailingIcon = {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "clear text",
                    modifier = Modifier
                        .clickable {
                            searchText.value = ""
                        }
                )
            }
        )
        LazyColumn(Modifier.padding(top = 8.dp)) {
            filteredCountries = if (searchText.value.isEmpty()) {
                countries.value
            } else {
                val resultList = ArrayList<String>()
                for (country in countries.value) {
                    if (country.lowercase(Locale.getDefault())
                            .contains(searchText.value.lowercase(Locale.getDefault()))
                    ) {
                        resultList.add(country)
                    }
                }
                resultList
            }
            items(filteredCountries, itemContent = { item ->
                Text(modifier = Modifier.padding(top = 6.dp), text = item)
                Divider(modifier = Modifier.padding(top = 6.dp))
            })
        }
    }
}


@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PreviewSelectCountryScreen() {
    SelectCountryScreenContent()
}


