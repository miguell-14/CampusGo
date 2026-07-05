package com.example.campusgo.util

// Validação simples de formato de email, partilhada por login, registo e edição de perfil.
private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

fun isEmailValido(email: String): Boolean = EMAIL_REGEX.matches(email)
