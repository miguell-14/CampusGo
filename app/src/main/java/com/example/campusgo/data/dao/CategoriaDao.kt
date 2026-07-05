package com.example.campusgo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.campusgo.data.model.Categoria
import kotlinx.coroutines.flow.Flow

// Acesso Room à tabela "categorias" — CRUD usado pelo Admin, leitura usada por todos.
@Dao
interface CategoriaDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(categoria: Categoria): Long

    @Update
    suspend fun atualizar(categoria: Categoria)

    @Delete
    suspend fun remover(categoria: Categoria)

    @Query("SELECT * FROM categorias ORDER BY nome ASC")
    fun getTodas(): Flow<List<Categoria>>

    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Categoria?
}
