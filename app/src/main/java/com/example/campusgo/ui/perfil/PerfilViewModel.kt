package com.example.campusgo.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusgo.data.UtilizadorRepository
import com.example.campusgo.data.model.Utilizador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado do fluxo de edição (EditarPerfilScreen) — os dados em si (nome/email/foto) vêm de
// `utilizador`, reativo, para o separador "Perfil" refletir sempre o valor mais recente.
data class PerfilUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sucesso: Boolean = false
)

class PerfilViewModel(
    private val repository: UtilizadorRepository,
    private val utilizadorId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState

    // Dados atuais do utilizador da sessão — reativo, para o separador "Perfil" e o
    // EditarPerfilScreen refletirem sempre o valor mais recente na BD.
    val utilizador: StateFlow<Utilizador?> = repository.observarUtilizador(utilizadorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun guardar(nome: String, email: String, novaPassword: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, sucesso = false) }
            repository.atualizarPerfil(utilizadorId, nome, email, novaPassword)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, sucesso = true) }
                }
                .onFailure { erro ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = erro.message) }
                }
        }
    }

    // Foto de perfil escolhida no separador "Perfil" (câmara/galeria) — grava logo, sem passar
    // pelo formulário de editar perfil.
    fun atualizarFotoPerfil(fotoPerfilPath: String?) {
        viewModelScope.launch {
            repository.atualizarFotoPerfil(utilizadorId, fotoPerfilPath)
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
