package com.example.campusgo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utilizadores")
data class Utilizador(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val email: String,
    val passwordHash: String,
    val tipoPerfil: TipoPerfil
)
