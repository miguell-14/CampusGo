package com.example.campusgo.util

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

fun isEmailValido(email: String): Boolean = EMAIL_REGEX.matches(email)
