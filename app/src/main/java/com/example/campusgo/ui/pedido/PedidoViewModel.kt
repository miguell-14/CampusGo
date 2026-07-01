package com.example.campusgo.ui.pedido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusgo.data.CategoriaRepository
import com.example.campusgo.data.PedidoRepository
import com.example.campusgo.data.model.Categoria
import com.example.campusgo.data.model.Pedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PedidoUiState(
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSucesso: Boolean = false,
    val cancelarError: String? = null
)

class PedidoViewModel(
    private val pedidoRepository: PedidoRepository,
    categoriaRepository: CategoriaRepository,
    private val utilizadorId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidoUiState())
    val uiState: StateFlow<PedidoUiState> = _uiState

    val categorias: StateFlow<List<Categoria>> = categoriaRepository.getTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pedidos: StateFlow<List<Pedido>> = pedidoRepository.getPorUtilizador(utilizadorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun criarPedido(
        categoriaId: Long,
        localizacao: String,
        descricao: String,
        fotografiaPath: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null, submitSucesso = false) }
            runCatching {
                pedidoRepository.criar(utilizadorId, categoriaId, localizacao, descricao, fotografiaPath)
            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, submitSucesso = true) }
            }.onFailure { erro ->
                _uiState.update { it.copy(isSubmitting = false, submitError = erro.message) }
            }
        }
    }

    fun cancelarPedido(pedido: Pedido) {
        viewModelScope.launch {
            pedidoRepository.cancelar(pedido)
                .onFailure { erro ->
                    _uiState.update { it.copy(cancelarError = erro.message) }
                }
        }
    }

    fun limparEstadoSubmissao() {
        _uiState.update { it.copy(submitError = null, submitSucesso = false) }
    }

    fun limparErroCancelar() {
        _uiState.update { it.copy(cancelarError = null) }
    }
}

class PedidoViewModelFactory(
    private val pedidoRepository: PedidoRepository,
    private val categoriaRepository: CategoriaRepository,
    private val utilizadorId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PedidoViewModel(pedidoRepository, categoriaRepository, utilizadorId) as T
    }
}
