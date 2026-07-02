package com.example.campusgo.data

import com.example.campusgo.data.dao.PedidoDao
import com.example.campusgo.data.model.EstadoPedido
import com.example.campusgo.data.model.Pedido
import kotlinx.coroutines.flow.Flow

// Regras de negócio dos pedidos do lado do Utilizador (criar, listar, cancelar).
class PedidoRepository(private val pedidoDao: PedidoDao) {

    // Cria um novo pedido, sempre com estado inicial SUBMETIDO.
    suspend fun criar(
        utilizadorId: Long,
        categoriaId: Long,
        localizacao: String,
        descricao: String,
        fotografiaPath: String?
    ): Long {
        val pedido = Pedido(
            utilizadorId = utilizadorId,
            categoriaId = categoriaId,
            localizacao = localizacao,
            descricao = descricao,
            dataCriacao = System.currentTimeMillis(),
            estado = EstadoPedido.SUBMETIDO,
            fotografiaPath = fotografiaPath
        )
        return pedidoDao.inserir(pedido)
    }

    // Lista reativa dos pedidos de um utilizador — usada no ListaPedidosScreen.
    fun getPorUtilizador(utilizadorId: Long): Flow<List<Pedido>> =
        pedidoDao.getPorUtilizador(utilizadorId)

    // Pedido individual por id — para o futuro DetalhePedidoScreen.
    suspend fun getById(id: Long): Pedido? = pedidoDao.getById(id)

    // Cancela (remove) um pedido, exceto se já estiver CONCLUIDO — decisão de negócio explícita.
    suspend fun cancelar(pedido: Pedido): Result<Unit> {
        if (pedido.estado == EstadoPedido.CONCLUIDO) {
            return Result.failure(IllegalStateException("Não é possível cancelar um pedido já concluído"))
        }
        pedidoDao.remover(pedido)
        return Result.success(Unit)
    }
}
