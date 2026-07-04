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

    // Lista reativa de todos os pedidos de todos os utilizadores — usada no AdminPedidosScreen.
    fun getTodos(): Flow<List<Pedido>> = pedidoDao.getTodos()

    // Pedido individual por id — para o futuro DetalhePedidoScreen.
    suspend fun getById(id: Long): Pedido? = pedidoDao.getById(id)

    // Cancela (remove) um pedido — só permitido enquanto ainda está SUBMETIDO (por analisar).
    // Depois de entrar em análise (ou ser concluído/rejeitado), já não pode ser cancelado pelo
    // utilizador sozinho.
    suspend fun cancelar(pedido: Pedido): Result<Unit> {
        if (pedido.estado != EstadoPedido.SUBMETIDO) {
            return Result.failure(IllegalStateException("Só é possível cancelar pedidos ainda submetidos (por analisar)"))
        }
        pedidoDao.remover(pedido)
        return Result.success(Unit)
    }

    // Ação do Admin: muda o estado de um pedido. CONCLUIDO e REJEITADO são estados finais — depois
    // de lá chegar, já não é possível voltar atrás nem mudar para outro estado.
    suspend fun atualizarEstado(pedido: Pedido, novoEstado: EstadoPedido): Result<Unit> {
        if (pedido.estado == EstadoPedido.CONCLUIDO || pedido.estado == EstadoPedido.REJEITADO) {
            return Result.failure(IllegalStateException("Não é possível alterar o estado de um pedido já concluído ou rejeitado"))
        }
        pedidoDao.atualizar(pedido.copy(estado = novoEstado))
        return Result.success(Unit)
    }

    // Ação do Admin: elimina definitivamente um pedido, seja qual for o seu estado.
    suspend fun eliminar(pedido: Pedido) {
        pedidoDao.remover(pedido)
    }

    // Quantos pedidos usam esta categoria — usado para bloquear a eliminação de categorias em uso
    // (decisão 3 do NOTAS.md), sem essa regra ter de viver dentro do CategoriaRepository.
    suspend fun contarPorCategoria(categoriaId: Long): Int = pedidoDao.contarPorCategoria(categoriaId)
}
