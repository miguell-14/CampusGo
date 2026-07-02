package com.example.campusgo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.campusgo.data.model.Utilizador

// Acesso Room à tabela "utilizadores". getByEmail serve tanto o login como a validação
// de email duplicado no registo/edição de perfil.
@Dao
interface UtilizadorDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(utilizador: Utilizador): Long

    @Update
    suspend fun atualizar(utilizador: Utilizador)

    @Query("SELECT * FROM utilizadores WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): Utilizador?

    @Query("SELECT * FROM utilizadores WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Utilizador?
}
