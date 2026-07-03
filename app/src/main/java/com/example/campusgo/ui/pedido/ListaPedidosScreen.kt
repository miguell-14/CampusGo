package com.example.campusgo.ui.pedido

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusgo.data.model.Categoria
import com.example.campusgo.data.model.EstadoPedido
import com.example.campusgo.data.model.Pedido
import java.text.SimpleDateFormat
import java.util.Locale

// Ecrã do Utilizador: lista os seus próprios pedidos com estado visível.
// Cancelar só é feito a partir do DetalhePedidoScreen (aberto ao tocar num pedido).
@Composable
fun ListaPedidosScreen(
    viewModel: PedidoViewModel,
    onVoltar: () -> Unit,
    onAbrirDetalhe: (Long) -> Unit
) {
    val pedidos by viewModel.pedidos.collectAsState()
    val categorias by viewModel.categorias.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Os meus pedidos", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // Estado vazio vs. lista de pedidos (cada item é um Card, ver PedidoItem abaixo).
        if (pedidos.isEmpty()) {
            Text(
                text = "Ainda não tens pedidos submetidos.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pedidos, key = { it.id }) { pedido ->
                    PedidoItem(
                        pedido = pedido,
                        nomeCategoria = categorias.nomeDaCategoria(pedido.categoriaId),
                        onAbrirDetalhe = { onAbrirDetalhe(pedido.id) }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = onVoltar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar")
        }
    }
}

// Diálogo de confirmação partilhado (ListaPedidosScreen e DetalhePedidoScreen) antes de cancelar
// um pedido — evita cancelamentos acidentais ao tocar sem querer no botão.
@Composable
fun ConfirmarCancelamentoDialog(
    pedido: Pedido?,
    onConfirmar: (Pedido) -> Unit,
    onDismiss: () -> Unit
) {
    if (pedido == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancelar pedido") },
        text = { Text("Tens a certeza que queres cancelar este pedido?") },
        confirmButton = {
            TextButton(onClick = { onConfirmar(pedido) }) {
                Text("Sim, cancelar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Voltar")
            }
        }
    )
}

// Card de um único pedido: categoria + estado, localização, descrição e data.
// Tocar no card abre o DetalhePedidoScreen — é lá que se pode cancelar.
@Composable
private fun PedidoItem(
    pedido: Pedido,
    nomeCategoria: String,
    onAbrirDetalhe: () -> Unit
) {
    Card(
        onClick = onAbrirDetalhe,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = nomeCategoria, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = labelEstado(pedido.estado),
                    style = MaterialTheme.typography.labelLarge,
                    color = corEstado(pedido.estado)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(text = pedido.localizacao, style = MaterialTheme.typography.bodyMedium)
            Text(text = pedido.descricao, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatarData(pedido.dataCriacao),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- Helpers de apresentação (categoria por id, labels e cores de estado, formatação de data) ---
// Não são "private": também usados pelo DetalhePedidoScreen, no mesmo package.

fun List<Categoria>.nomeDaCategoria(categoriaId: Long): String =
    firstOrNull { it.id == categoriaId }?.nome ?: "Categoria desconhecida"

fun labelEstado(estado: EstadoPedido): String = when (estado) {
    EstadoPedido.SUBMETIDO -> "Submetido"
    EstadoPedido.EM_ANALISE -> "Em análise"
    EstadoPedido.CONCLUIDO -> "Concluído"
    EstadoPedido.REJEITADO -> "Rejeitado"
}

@Composable
fun corEstado(estado: EstadoPedido) = when (estado) {
    EstadoPedido.SUBMETIDO -> MaterialTheme.colorScheme.primary
    EstadoPedido.EM_ANALISE -> MaterialTheme.colorScheme.tertiary
    EstadoPedido.CONCLUIDO -> MaterialTheme.colorScheme.secondary
    EstadoPedido.REJEITADO -> MaterialTheme.colorScheme.error
}

fun formatarData(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(timestamp)
