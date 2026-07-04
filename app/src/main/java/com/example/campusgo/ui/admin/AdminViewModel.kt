package com.example.campusgo.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusgo.data.CategoriaRepository
import com.example.campusgo.data.PedidoRepository
import com.example.campusgo.data.UtilizadorRepository
import com.example.campusgo.data.model.Categoria
import com.example.campusgo.data.model.EstadoPedido
import com.example.campusgo.data.model.Pedido
import com.example.campusgo.data.model.Utilizador
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel do Admin para a gestão de pedidos: ver todos, alterar estado e eliminar
// (AdminPedidosScreen). O CRUD de categorias fica para um próximo passo.
class AdminViewModel(
    private val pedidoRepository: PedidoRepository,
    categoriaRepository: CategoriaRepository,
    utilizadorRepository: UtilizadorRepository
) : ViewModel() {

    // Todos os pedidos de todos os utilizadores, para o AdminPedidosScreen.
    val pedidos: StateFlow<List<Pedido>> = pedidoRepository.getTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Categorias, para mostrar o nome em vez do id em cada pedido.
    val categorias: StateFlow<List<Categoria>> = categoriaRepository.getTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Utilizadores, para mostrar quem fez cada pedido.
    val utilizadores: StateFlow<List<Utilizador>> = utilizadorRepository.getTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Muda o estado de um pedido. A UI já impede tentar isto quando o estado atual é final
    // (Concluído/Rejeitado) — o repositório recusa na mesma, por segurança.
    fun alterarEstado(pedido: Pedido, novoEstado: EstadoPedido) {
        viewModelScope.launch {
            pedidoRepository.atualizarEstado(pedido, novoEstado)
        }
    }

    // Elimina definitivamente um pedido (a UI só chama isto depois de confirmação explícita).
    fun eliminarPedido(pedido: Pedido) {
        viewModelScope.launch {
            pedidoRepository.eliminar(pedido)
        }
    }
}

class AdminViewModelFactory(
    private val pedidoRepository: PedidoRepository,
    private val categoriaRepository: CategoriaRepository,
    private val utilizadorRepository: UtilizadorRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AdminViewModel(pedidoRepository, categoriaRepository, utilizadorRepository) as T
    }
}
