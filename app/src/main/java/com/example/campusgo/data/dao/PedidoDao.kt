package com.example.campusgo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.campusgo.data.model.EstadoPedido
import com.example.campusgo.data.model.Pedido
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(pedido: Pedido): Long

    @Update
    suspend fun atualizar(pedido: Pedido)

    @Delete
    suspend fun remover(pedido: Pedido)

    @Query("SELECT * FROM pedidos WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Pedido?

    @Query("SELECT * FROM pedidos ORDER BY dataCriacao DESC")
    fun getTodos(): Flow<List<Pedido>>

    @Query("SELECT * FROM pedidos WHERE utilizadorId = :utilizadorId ORDER BY dataCriacao DESC")
    fun getPorUtilizador(utilizadorId: Long): Flow<List<Pedido>>

    @Query("SELECT * FROM pedidos WHERE estado = :estado ORDER BY dataCriacao DESC")
    fun getPorEstado(estado: EstadoPedido): Flow<List<Pedido>>

    @Query("SELECT COUNT(*) FROM pedidos WHERE categoriaId = :categoriaId")
    suspend fun contarPorCategoria(categoriaId: Long): Int
}
