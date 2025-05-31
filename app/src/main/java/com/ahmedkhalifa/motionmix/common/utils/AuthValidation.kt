package com.ahmedkhalifa.motionmix.common.utils

fun isPasswordValid(password: String): Boolean {
    if (password.length < 8 || password.length > 20) return false
    val hasLetter = password.any { it.isLetter() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }
    return hasLetter && hasDigit && hasSpecial
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    )
    return email.matches(emailRegex)
}
fun isValidPhoneNumber(phone: String): Boolean {
    val cleanedPhone = phone.trim().replace("[\\s\\-()]+".toRegex(), "")
    val phoneRegex = Regex("^\\+?[0-9]{10,15}\$") // Accepts + and 10-15 digits
    return cleanedPhone.matches(phoneRegex)
}

fun extractPhoneCode(fullText: String): String {
    val regex = "\\((\\+\\d+)\\)".toRegex()
    return regex.find(fullText)?.groupValues?.get(1) ?: "+00"
}

