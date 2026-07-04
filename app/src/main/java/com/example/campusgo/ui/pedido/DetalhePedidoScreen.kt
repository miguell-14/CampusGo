package com.example.campusgo.ui.pedido

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.campusgo.data.model.EstadoPedido
import com.example.campusgo.data.model.Pedido
import com.example.campusgo.ui.components.EcraComTopBar

// Ecrã de detalhe de um único pedido do Utilizador. Reaproveita o PedidoViewModel já ligado ao
// ListaPedidosScreen — a lista de pedidos é a mesma StateFlow, só filtramos aqui pelo id recebido
// via navegação, em vez de ter uma query/ViewModel próprios só para este ecrã.
@Composable
fun DetalhePedidoScreen(
    viewModel: PedidoViewModel,
    pedidoId: Long,
    onVoltar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pedidos by viewModel.pedidos.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val pedido = pedidos.firstOrNull { it.id == pedidoId }

    // Pedido à espera de confirmação de cancelamento (null = nenhum diálogo aberto).
    var pedidoParaCancelar by remember { mutableStateOf<Pedido?>(null) }

    EcraComTopBar(titulo = "Detalhe do pedido", onVoltar = onVoltar) { modifierConteudo ->
        Column(
            modifier = modifierConteudo
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (pedido == null) {
                // Pode acontecer se o pedido tiver sido cancelado entretanto noutro ecrã.
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
                    text = labelEstado(pedido.estado),
                    style = MaterialTheme.typography.labelLarge,
                    color = corEstado(pedido.estado)
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

                // Erro de cancelamento (ex.: pedido passou a CONCLUIDO entretanto no lado do Admin).
                uiState.cancelarError?.let { mensagem ->
                    Text(text = mensagem, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }

                // Cancelar só é permitido enquanto o pedido ainda está por analisar — depois de
                // entrar em análise já não faz sentido o utilizador poder cancelar sozinho.
                if (pedido.estado == EstadoPedido.SUBMETIDO) {
                    Button(
                        onClick = { pedidoParaCancelar = pedido },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar pedido")
                    }
                }
            }
        }
    }

    // Ao confirmar, cancela e fecha o ecrã — o pedido deixa de existir para se ver aqui.
    ConfirmarCancelamentoDialog(
        pedido = pedidoParaCancelar,
        onConfirmar = { alvo ->
            viewModel.cancelarPedido(alvo)
            pedidoParaCancelar = null
            onVoltar()
        },
        onDismiss = { pedidoParaCancelar = null }
    )
}

// Pré-visualização da fotografia a partir do caminho gravado em armazenamento interno — partilhada
// entre o DetalhePedidoScreen do Utilizador e o do Admin.
@Composable
fun FotografiaDoPedido(path: String) {
    val bitmap = remember(path) { BitmapFactory.decodeFile(path)?.asImageBitmap() }
    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = "Fotografia do pedido",
            modifier = Modifier.size(200.dp)
        )
    }
}
