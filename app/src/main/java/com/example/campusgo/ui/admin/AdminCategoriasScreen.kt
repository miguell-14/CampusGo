package com.example.campusgo.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusgo.data.model.Categoria

// Conteúdo do separador "Categorias" da home do Admin: estatística simples (total), lista de
// categorias com "Editar"/"Eliminar" — eliminar é bloqueado pelo ViewModel se a categoria ainda
// tiver pedidos associados (decisão 3 do NOTAS.md). Criar é feito pelo FAB (ver AdminHomeScreen),
// não por um formulário fixo no topo — pouco frequente para justificar ocupar sempre espaço.
@Composable
fun AdminCategoriasContent(
    modifier: Modifier = Modifier,
    viewModel: AdminCategoriasViewModel,
    mostrarDialogoCriar: Boolean,
    onFecharDialogoCriar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categorias by viewModel.categorias.collectAsState()

    // Categoria selecionada para editar/eliminar (null = nenhum diálogo aberto).
    var categoriaParaEditar by remember { mutableStateOf<Categoria?>(null) }
    var categoriaParaEliminar by remember { mutableStateOf<Categoria?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Estatística simples (valorização do enunciado): total de categorias existentes.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Total de categorias: ", style = MaterialTheme.typography.titleLarge)
            Text(
                text = categorias.size.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(24.dp))

        // Lista de categorias existentes, cada uma com "Editar" e "Eliminar".
        if (categorias.isEmpty()) {
            Text(
                text = "Ainda não há categorias criadas.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(categorias, key = { it.id }) { categoria ->
                    CategoriaItem(
                        categoria = categoria,
                        onEditar = { categoriaParaEditar = categoria },
                        onEliminar = { categoriaParaEliminar = categoria }
                    )
                }
            }
        }
    }

    // Diálogo de criação — aberto a partir do FAB na AdminHomeScreen.
    if (mostrarDialogoCriar) {
        CriarCategoriaDialog(
            onConfirmar = { nome, descricao ->
                viewModel.criar(nome, descricao)
                onFecharDialogoCriar()
            },
            onDismiss = onFecharDialogoCriar
        )
    }

    // Diálogo de edição — pré-preenchido com o nome/descrição atuais da categoria escolhida.
    categoriaParaEditar?.let { categoria ->
        EditarCategoriaDialog(
            categoria = categoria,
            onConfirmar = { novoNome, novaDescricao ->
                viewModel.atualizar(categoria, novoNome, novaDescricao)
                categoriaParaEditar = null
            },
            onDismiss = { categoriaParaEditar = null }
        )
    }

    // Confirmação obrigatória antes de eliminar — o ViewModel só efetiva se não estiver em uso.
    categoriaParaEliminar?.let { categoria ->
        AlertDialog(
            onDismissRequest = { categoriaParaEliminar = null },
            title = { Text("Eliminar categoria") },
            text = { Text("Tens a certeza que queres eliminar \"${categoria.nome}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(categoria)
                    categoriaParaEliminar = null
                }) {
                    Text("Sim, eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoriaParaEliminar = null }) {
                    Text("Voltar")
                }
            }
        )
    }

    // Erro de eliminação bloqueada (categoria em uso) — mostrado como diálogo simples e depois limpo.
    uiState.erroEliminar?.let { mensagem ->
        AlertDialog(
            onDismissRequest = { viewModel.limparErroEliminar() },
            title = { Text("Não é possível eliminar") },
            text = { Text(mensagem) },
            confirmButton = {
                TextButton(onClick = { viewModel.limparErroEliminar() }) {
                    Text("Ok")
                }
            }
        )
    }
}

// Card de uma categoria: nome, descrição (se existir) e ações de editar/eliminar.
@Composable
private fun CategoriaItem(
    categoria: Categoria,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = categoria.nome, style = MaterialTheme.typography.titleMedium)
            categoria.descricao?.let { descricao ->
                Text(text = descricao, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(4.dp))
            // Alinhados à direita — mais fácil de alcançar com o polegar do que à esquerda.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Text("Editar")
                }
                TextButton(onClick = onEliminar) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// Diálogo de criação de uma categoria nova — aberto a partir do FAB da AdminHomeScreen.
@Composable
private fun CriarCategoriaDialog(
    onConfirmar: (nome: String, descricao: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Criar categoria") },
        text = {
            Column {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                erro?.let { mensagem ->
                    Spacer(Modifier.height(8.dp))
                    Text(text = mensagem, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nome.isBlank()) {
                    erro = "Introduz o nome da categoria"
                } else {
                    onConfirmar(nome.trim(), descricao.trim().ifBlank { null })
                }
            }) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Diálogo de edição de nome/descrição de uma categoria existente.
@Composable
private fun EditarCategoriaDialog(
    categoria: Categoria,
    onConfirmar: (nome: String, descricao: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var nome by remember { mutableStateOf(categoria.nome) }
    var descricao by remember { mutableStateOf(categoria.descricao.orEmpty()) }
    var erro by remember { mutableStateOf<String?>(null) }

    // Se o diálogo for reaberto para uma categoria diferente, repõe os campos com os dados dela.
    LaunchedEffect(categoria.id) {
        nome = categoria.nome
        descricao = categoria.descricao.orEmpty()
        erro = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar categoria") },
        text = {
            Column {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                erro?.let { mensagem ->
                    Spacer(Modifier.height(8.dp))
                    Text(text = mensagem, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nome.isBlank()) {
                    erro = "Introduz o nome da categoria"
                } else {
                    onConfirmar(nome.trim(), descricao.trim().ifBlank { null })
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
