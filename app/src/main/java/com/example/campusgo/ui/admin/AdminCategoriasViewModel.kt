package com.example.campusgo.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusgo.data.CategoriaRepository
import com.example.campusgo.data.PedidoRepository
import com.example.campusgo.data.model.Categoria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de UI do AdminCategoriasScreen — só o erro de eliminação bloqueada precisa de ser exposto.
data class AdminCategoriasUiState(
    val erroEliminar: String? = null
)

// ViewModel do Admin para o CRUD de categorias.
class AdminCategoriasViewModel(
    private val categoriaRepository: CategoriaRepository,
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCategoriasUiState())
    val uiState: StateFlow<AdminCategoriasUiState> = _uiState

    // Lista reativa de todas as categorias, para o AdminCategoriasScreen.
    val categorias: StateFlow<List<Categoria>> = categoriaRepository.getTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun criar(nome: String, descricao: String?) {
        viewModelScope.launch {
            categoriaRepository.criar(nome, descricao)
        }
    }

    fun atualizar(categoria: Categoria, novoNome: String, novaDescricao: String?) {
        viewModelScope.launch {
            categoriaRepository.atualizar(categoria.copy(nome = novoNome, descricao = novaDescricao))
        }
    }

    // Bloqueado se a categoria ainda tiver pedidos associados — decisão 3 do NOTAS.md.
    fun eliminar(categoria: Categoria) {
        viewModelScope.launch {
            val emUso = pedidoRepository.contarPorCategoria(categoria.id) > 0
            if (emUso) {
                _uiState.update {
                    it.copy(erroEliminar = "Não é possível eliminar \"${categoria.nome}\": ainda tem pedidos associados")
                }
            } else {
                categoriaRepository.eliminar(categoria)
            }
        }
    }

    fun limparErroEliminar() {
        _uiState.update { it.copy(erroEliminar = null) }
    }
}

class AdminCategoriasViewModelFactory(
    private val categoriaRepository: CategoriaRepository,
    private val pedidoRepository: PedidoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AdminCategoriasViewModel(categoriaRepository, pedidoRepository) as T
    }
}
