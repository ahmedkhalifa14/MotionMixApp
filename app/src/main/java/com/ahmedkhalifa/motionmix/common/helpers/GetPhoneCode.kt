package com.ahmedkhalifa.motionmix.common.helpers

import com.google.i18n.phonenumbers.PhoneNumberUtil

fun getPhoneCode(countryCode: String): String {
    val formatter = PhoneNumberUtil.getInstance()
    return try {
        val phoneNumber = formatter.getExampleNumberForType(countryCode, PhoneNumberUtil.PhoneNumberType.MOBILE)
        if (phoneNumber != null) {
            "+${phoneNumber.countryCode}"
        } else {
            ""
        }
    } catch (e: NumberFormatException) {
        ""
    }
}
