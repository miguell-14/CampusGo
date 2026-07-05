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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.campusgo.data.model.EstadoPedido
import com.example.campusgo.data.model.Pedido
import com.example.campusgo.data.model.Utilizador
import com.example.campusgo.ui.pedido.corEstado
import com.example.campusgo.ui.pedido.formatarData
import com.example.campusgo.ui.pedido.labelEstado
import com.example.campusgo.ui.pedido.nomeDaCategoria

// Conteúdo do separador "Pedidos" da home do Admin: lista todos os pedidos de todos os
// utilizadores, com filtro por estado. Tocar num pedido abre o AdminDetalhePedidoScreen — é lá
// que se altera o estado e se elimina, depois de ver o pedido completo (incluindo a fotografia).
@Composable
fun AdminPedidosContent(
    modifier: Modifier = Modifier,
    viewModel: AdminViewModel,
    nomeAdmin: String,
    onAbrirDetalhe: (Long) -> Unit
) {
    val pedidos by viewModel.pedidos.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val utilizadores by viewModel.utilizadores.collectAsState()

    // Filtro por estado (null = mostrar todos). Aplicado em memória sobre a lista já carregada.
    var filtroEstado by remember { mutableStateOf<EstadoPedido?>(null) }
    val pedidosFiltradosBase = if (filtroEstado == null) {
        pedidos
    } else {
        pedidos.filter { it.estado == filtroEstado }
    }

    // Mesma ordenação do Utilizador: pendentes (Submetido/Em análise) primeiro, resolvidos
    // (Concluído/Rejeitado) no fim — estes últimos aparecem esbatidos no PedidoAdminItem.
    val (pendentes, resolvidos) = pedidosFiltradosBase.partition {
        it.estado == EstadoPedido.SUBMETIDO || it.estado == EstadoPedido.EM_ANALISE
    }
    val pedidosFiltrados = pendentes + resolvidos

    // Um único LazyColumn (cabeçalho e filtros incluídos como itens) para a saudação deslizar
    // junto com a lista, em vez de ficar fixa a ocupar espaço quando há muitos pedidos.
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Spacer(Modifier.height(24.dp))
                Text(text = "Olá, $nomeAdmin", style = MaterialTheme.typography.headlineLarge)
                Text(
                    text = "Todos os pedidos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                EstatisticasPedidos(pedidos)
                Spacer(Modifier.height(4.dp))
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = filtroEstado == null,
                        onClick = { filtroEstado = null },
                        label = { Text("Todos") }
                    )
                }
                items(EstadoPedido.entries.toList()) { estado ->
                    FilterChip(
                        selected = filtroEstado == estado,
                        onClick = { filtroEstado = estado },
                        label = { Text(labelEstado(estado)) }
                    )
                }
            }
        }

        if (pedidosFiltrados.isEmpty()) {
            item {
                Text(
                    text = if (pedidos.isEmpty()) {
                        "Ainda não há pedidos submetidos."
                    } else {
                        "Não há pedidos com o estado selecionado."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(pedidosFiltrados, key = { it.id }) { pedido ->
                PedidoAdminItem(
                    pedido = pedido,
                    nomeCategoria = categorias.nomeDaCategoria(pedido.categoriaId),
                    nomeUtilizador = utilizadores.nomeDoUtilizador(pedido.utilizadorId),
                    onAbrirDetalhe = { onAbrirDetalhe(pedido.id) }
                )
            }
        }
    }
}

// Estatísticas simples (valorização do enunciado): contagem de pedidos por estado, sobre a lista
// já carregada — sem query nova. Cor de cada número igual à do estado (ver corEstado).
@Composable
private fun EstatisticasPedidos(pedidos: List<Pedido>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        EstadoPedido.entries.forEach { estado ->
            val quantidade = pedidos.count { it.estado == estado }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = quantidade.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = corEstado(estado)
                )
                Text(
                    text = labelEstado(estado),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Card de um pedido do ponto de vista do Admin: quem o fez, categoria, estado, localização,
// descrição e data. Só visualização — as ações vivem no AdminDetalhePedidoScreen.
@Composable
private fun PedidoAdminItem(
    pedido: Pedido,
    nomeCategoria: String,
    nomeUtilizador: String,
    onAbrirDetalhe: () -> Unit
) {
    // Pedidos já resolvidos (Concluído/Rejeitado) aparecem esbatidos — leem-se como histórico,
    // tal como no PedidoItem do Utilizador.
    val opacidade = if (pedido.estado == EstadoPedido.CONCLUIDO || pedido.estado == EstadoPedido.REJEITADO) {
        0.6f
    } else {
        1f
    }

    Card(
        onClick = onAbrirDetalhe,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(opacidade)
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
            Text(text = "De: $nomeUtilizador", style = MaterialTheme.typography.bodyMedium)
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

// Não é "private": também usado pelo AdminDetalhePedidoScreen, no mesmo package.
fun List<Utilizador>.nomeDoUtilizador(utilizadorId: Long): String =
    firstOrNull { it.id == utilizadorId }?.nome ?: "Utilizador desconhecido"
