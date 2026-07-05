package com.example.campusgo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.campusgo.data.model.Utilizador
import kotlinx.coroutines.flow.Flow

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

    // Versão reativa de getById — usada no separador "Perfil" para refletir de imediato qualquer
    // alteração (nome/email/foto), venha de onde vier (EditarPerfilScreen ou o próprio separador).
    @Query("SELECT * FROM utilizadores WHERE id = :id LIMIT 1")
    fun observarPorId(id: Long): Flow<Utilizador?>

    // Lista reativa de todos os utilizadores — usada pelo Admin para mostrar quem fez cada pedido.
    @Query("SELECT * FROM utilizadores")
    fun getTodos(): Flow<List<Utilizador>>
}
