package com.example.campusgo.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusgo.data.UtilizadorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de UI do EditarPerfilScreen — sem tipoPerfil, que nunca é editável aqui.
data class PerfilUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sucesso: Boolean = false,
    val nome: String = "",
    val email: String = ""
)

class PerfilViewModel(
    private val repository: UtilizadorRepository,
    private val utilizadorId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState

    // Carrega os dados atuais do utilizador da sessão para pré-preencher o formulário.
    init {
        viewModelScope.launch {
            repository.getById(utilizadorId)?.let { utilizador ->
                _uiState.update { it.copy(nome = utilizador.nome, email = utilizador.email) }
            }
        }
    }

    fun guardar(nome: String, email: String, novaPassword: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, sucesso = false) }
            repository.atualizarPerfil(utilizadorId, nome, email, novaPassword)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, sucesso = true, nome = nome, email = email)
                    }
                }
                .onFailure { erro ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = erro.message) }
                }
        }
    }

    fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun limparSucesso() {
        _uiState.update { it.copy(sucesso = false) }
    }
}

class PerfilViewModelFactory(
    private val repository: UtilizadorRepository,
    private val utilizadorId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PerfilViewModel(repository, utilizadorId) as T
    }
}
