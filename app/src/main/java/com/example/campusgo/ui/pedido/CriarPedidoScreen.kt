package com.example.campusgo.ui.pedido

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import com.example.campusgo.data.model.Categoria

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarPedidoScreen(
    viewModel: PedidoViewModel,
    onPedidoCriado: () -> Unit,
    onVoltar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categorias by viewModel.categorias.collectAsState()

    var categoriaSelecionada by remember { mutableStateOf<Categoria?>(null) }
    var dropdownAberto by remember { mutableStateOf(false) }
    var localizacao by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.submitSucesso) {
        if (uiState.submitSucesso) {
            viewModel.limparEstadoSubmissao()
            onPedidoCriado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Criar pedido", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        ExposedDropdownMenuBox(
            expanded = dropdownAberto,
            onExpandedChange = { dropdownAberto = it }
        ) {
            OutlinedTextField(
                value = categoriaSelecionada?.nome ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoria") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownAberto) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = dropdownAberto,
                onDismissRequest = { dropdownAberto = false }
            ) {
                categorias.forEach { categoria ->
                    DropdownMenuItem(
                        text = { Text(categoria.nome) },
                        onClick = {
                            categoriaSelecionada = categoria
                            dropdownAberto = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = localizacao,
            onValueChange = { localizacao = it },
            label = { Text("Localização") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth()
        )

        (validationError ?: uiState.submitError)?.let { mensagem ->
            Spacer(Modifier.height(8.dp))
            Text(text = mensagem, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                validationError = validarPedido(categoriaSelecionada, localizacao, descricao)
                val categoria = categoriaSelecionada
                if (validationError == null && categoria != null) {
                    viewModel.limparEstadoSubmissao()
                    viewModel.criarPedido(
                        categoriaId = categoria.id,
                        localizacao = localizacao.trim(),
                        descricao = descricao.trim(),
                        fotografiaPath = null
                    )
                }
            },
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Submeter pedido")
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

private fun validarPedido(
    categoria: Categoria?,
    localizacao: String,
    descricao: String
): String? = when {
    categoria == null -> "Seleciona uma categoria"
    localizacao.isBlank() -> "Introduz a localização"
    descricao.isBlank() -> "Introduz a descrição"
    else -> null
}
