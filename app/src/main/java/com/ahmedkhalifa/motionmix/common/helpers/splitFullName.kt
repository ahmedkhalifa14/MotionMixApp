package com.ahmedkhalifa.motionmix.common.helpers

fun splitFullName(fullName: String): Pair<String, String> {
    val parts = fullName.trim().split("\\s+".toRegex())

    val firstName = parts.firstOrNull() ?: ""
    val lastName = if (parts.size > 1) parts.subList(1, parts.size).joinToString(" ") else ""

    return Pair(firstName, lastName)
}
