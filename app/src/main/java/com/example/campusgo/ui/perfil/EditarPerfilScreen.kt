package com.example.campusgo.ui.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.campusgo.util.isEmailValido

@Composable
fun EditarPerfilScreen(
    viewModel: PerfilViewModel,
    onVoltar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var nome by remember(uiState.nome) { mutableStateOf(uiState.nome) }
    var email by remember(uiState.email) { mutableStateOf(uiState.email) }
    var novaPassword by remember { mutableStateOf("") }
    var confirmarNovaPassword by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Ao guardar com sucesso, volta ao ecrã anterior (home do respetivo perfil).
    LaunchedEffect(uiState.sucesso) {
        if (uiState.sucesso) {
            viewModel.limparSucesso()
            onVoltar()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Editar perfil", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // tipoPerfil não aparece aqui de propósito — decisão 1 do NOTAS.md.
        OutlinedTextField(
            value = novaPassword,
            onValueChange = { novaPassword = it },
            label = { Text("Nova password (opcional)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmarNovaPassword,
            onValueChange = { confirmarNovaPassword = it },
            label = { Text("Confirmar nova password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        (validationError ?: uiState.errorMessage)?.let { mensagem ->
            Spacer(Modifier.height(8.dp))
            Text(text = mensagem, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                validationError = validarEdicaoPerfil(nome, email, novaPassword, confirmarNovaPassword)
                if (validationError == null) {
                    viewModel.limparErro()
                    viewModel.guardar(
                        nome = nome.trim(),
                        email = email.trim(),
                        novaPassword = novaPassword.ifBlank { null }
                    )
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Guardar")
            }
        }

        TextButton(
            onClick = onVoltar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

// Nova password é opcional: só valida tamanho/confirmação se o utilizador a preencher.
private fun validarEdicaoPerfil(
    nome: String,
    email: String,
    novaPassword: String,
    confirmarNovaPassword: String
): String? = when {
    nome.isBlank() -> "Introduz o nome"
    email.isBlank() -> "Introduz o email"
    !isEmailValido(email) -> "Email inválido"
    novaPassword.isNotBlank() && novaPassword.length < 6 -> "A nova password deve ter pelo menos 6 caracteres"
    novaPassword != confirmarNovaPassword -> "As passwords não coincidem"
    else -> null
}
