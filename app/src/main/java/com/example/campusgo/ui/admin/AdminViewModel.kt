package com.example.campusgo.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusgo.data.CategoriaRepository
import com.example.campusgo.data.PedidoRepository
import com.example.campusgo.data.UtilizadorRepository
import com.example.campusgo.data.model.Categoria
import com.example.campusgo.data.model.Pedido
import com.example.campusgo.data.model.Utilizador
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// ViewModel do Admin para a gestão de pedidos. Por agora só expõe as listas para visualização
// (AdminPedidosScreen) — alterar estado, eliminar e o CRUD de categorias entram nos próximos passos.
class AdminViewModel(
    pedidoRepository: PedidoRepository,
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
