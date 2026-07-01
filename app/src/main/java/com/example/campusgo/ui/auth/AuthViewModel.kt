package com.example.campusgo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusgo.data.SessionManager
import com.example.campusgo.data.UtilizadorRepository
import com.example.campusgo.data.model.TipoPerfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val tipoPerfil: TipoPerfil? = null
)

class AuthViewModel(
    private val repository: UtilizadorRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isLoggedIn = sessionManager.isLoggedIn(),
            tipoPerfil = sessionManager.getTipoPerfil()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.login(email, password)
                .onSuccess { utilizador ->
                    sessionManager.login(utilizador.id, utilizador.tipoPerfil)
                    _uiState.update {
                        it.copy(isLoading = false, isLoggedIn = true, tipoPerfil = utilizador.tipoPerfil)
                    }
                }
                .onFailure { erro ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = erro.message) }
                }
        }
    }

    fun registar(nome: String, email: String, password: String, tipoPerfil: TipoPerfil) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.registar(nome, email, password, tipoPerfil)
                .onSuccess { utilizadorId ->
                    sessionManager.login(utilizadorId, tipoPerfil)
                    _uiState.update {
                        it.copy(isLoading = false, isLoggedIn = true, tipoPerfil = tipoPerfil)
                    }
                }
                .onFailure { erro ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = erro.message) }
                }
        }
    }

    fun logout() {
        sessionManager.logout()
        _uiState.update { AuthUiState() }
    }

    fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

class AuthViewModelFactory(
    private val repository: UtilizadorRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AuthViewModel(repository, sessionManager) as T
    }
}
