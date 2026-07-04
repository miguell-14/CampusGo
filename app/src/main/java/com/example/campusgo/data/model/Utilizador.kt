package com.example.campusgo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Conta de utilizador. passwordHash nunca guarda a password em texto simples (ver PasswordUtils).
@Entity(tableName = "utilizadores")
data class Utilizador(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val email: String,
    val passwordHash: String,
    val tipoPerfil: TipoPerfil,
    val fotoPerfilPath: String? = null
)
