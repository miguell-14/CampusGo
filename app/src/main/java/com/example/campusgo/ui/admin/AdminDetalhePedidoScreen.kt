package com.example.campusgo.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusgo.data.model.EstadoPedido
import com.example.campusgo.ui.components.EcraComTopBar
import com.example.campusgo.ui.pedido.FotografiaDoPedido
import com.example.campusgo.ui.pedido.corEstado
import com.example.campusgo.ui.pedido.formatarData
import com.example.campusgo.ui.pedido.labelEstado
import com.example.campusgo.ui.pedido.nomeDaCategoria

// Ecrã de detalhe de um pedido do ponto de vista do Admin. Reaproveita o AdminViewModel já
// carregado (mesma StateFlow do AdminPedidosScreen, só filtramos aqui pelo id). É aqui que o
// Admin vê o pedido completo — incluindo a fotografia — antes de decidir alterar o estado ou
// eliminar, em vez de o fazer às cegas a partir da lista.
@Composable
fun AdminDetalhePedidoScreen(
    viewModel: AdminViewModel,
    pedidoId: Long,
    onVoltar: () -> Unit
) {
    val pedidos by viewModel.pedidos.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val utilizadores by viewModel.utilizadores.collectAsState()
    val pedido = pedidos.firstOrNull { it.id == pedidoId }

    // Menu de seleção de novo estado (fechado por omissão).
    var menuEstadoAberto by remember { mutableStateOf(false) }

    // Novo estado à espera de confirmação — só quando o destino é Concluído/Rejeitado (final).
    var estadoParaConfirmar by remember { mutableStateOf<EstadoPedido?>(null) }

    // Confirmação obrigatória antes de eliminar — ação destrutiva e irreversível.
    var confirmarEliminar by remember { mutableStateOf(false) }

    EcraComTopBar(titulo = "Detalhe do pedido", onVoltar = onVoltar) { modifierConteudo ->
    Column(
        modifier = modifierConteudo
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (pedido == null) {
            // Pode acontecer se o pedido tiver sido eliminado entretanto noutro ecrã.
            Text(
                text = "Este pedido já não existe.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = categorias.nomeDaCategoria(pedido.categoriaId),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "De: ${utilizadores.nomeDoUtilizador(pedido.utilizadorId)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))

            Text(text = "Localização", style = MaterialTheme.typography.labelMedium)
            Text(text = pedido.localizacao, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))

            Text(text = "Descrição", style = MaterialTheme.typography.labelMedium)
            Text(text = pedido.descricao, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))

            Text(text = "Data de criação", style = MaterialTheme.typography.labelMedium)
            Text(text = formatarData(pedido.dataCriacao), style = MaterialTheme.typography.bodyLarge)

            // Fotografia opcional (câmara/galeria, decisão 2) — só aparece se tiver sido guardada.
            pedido.fotografiaPath?.let { path ->
                Spacer(Modifier.height(16.dp))
                Text(text = "Fotografia", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                FotografiaDoPedido(path)
            }

            Spacer(Modifier.height(24.dp))

            // Estado atual + menu para o alterar — decisão tomada aqui, já com o pedido à vista.
            // "Estado" à esquerda, valor atual encostado à direita (toca para abrir a lista).
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Estado", style = MaterialTheme.typography.labelMedium)
                // Concluído/Rejeitado são finais — deixa de ser possível mudar, mostra-se só texto.
                val estadoFinal = pedido.estado == EstadoPedido.CONCLUIDO ||
                    pedido.estado == EstadoPedido.REJEITADO
                if (estadoFinal) {
                    Text(text = labelEstado(pedido.estado), color = corEstado(pedido.estado))
                } else {
                    Box {
                        // Chip com contorno — a forma visual já sugere que é tocável/editável,
                        // ao contrário de um texto simples.
                        AssistChip(
                            onClick = { menuEstadoAberto = true },
                            label = { Text(labelEstado(pedido.estado)) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = corEstado(pedido.estado)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = corEstado(pedido.estado)
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = corEstado(pedido.estado)
                            )
                        )
                        DropdownMenu(
                            expanded = menuEstadoAberto,
                            onDismissRequest = { menuEstadoAberto = false }
                        ) {
                            EstadoPedido.entries.forEach { estado ->
                                DropdownMenuItem(
                                    text = { Text(labelEstado(estado)) },
                                    onClick = {
                                        menuEstadoAberto = false
                                        // Concluído/Rejeitado são finais — pede confirmação antes
                                        // de aplicar. Submetido/Em análise aplicam-se de imediato.
                                        if (estado == EstadoPedido.CONCLUIDO || estado == EstadoPedido.REJEITADO) {
                                            estadoParaConfirmar = estado
                                        } else {
                                            viewModel.alterarEstado(pedido, estado)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Mesmo estilo do "Cancelar pedido" do Utilizador (botão cheio, largura total),
            // com cor de erro por ser uma ação destrutiva e irreversível.
            Button(
                onClick = { confirmarEliminar = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar pedido")
            }
        }
    }
    }

    // Confirmação ao mudar para um estado final (Concluído/Rejeitado) — depois disto, o estado
    // já não pode voltar a mudar.
    if (estadoParaConfirmar != null && pedido != null) {
        val novoEstado = estadoParaConfirmar!!
        AlertDialog(
            onDismissRequest = { estadoParaConfirmar = null },
            title = { Text("Marcar como ${labelEstado(novoEstado)}") },
            text = {
                Text(
                    "Tens a certeza que queres marcar este pedido como ${labelEstado(novoEstado)}? " +
                        "Depois disto já não é possível alterar o estado."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.alterarEstado(pedido, novoEstado)
                    estadoParaConfirmar = null
                }) {
                    Text("Sim, confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { estadoParaConfirmar = null }) {
                    Text("Voltar")
                }
            }
        )
    }

    if (confirmarEliminar && pedido != null) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Eliminar pedido") },
            text = { Text("Tens a certeza que queres eliminar este pedido? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarPedido(pedido)
                    confirmarEliminar = false
                    onVoltar()
                }) {
                    Text("Sim, eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) {
                    Text("Voltar")
                }
            }
        )
    }
}
