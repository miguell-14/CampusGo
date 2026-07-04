package com.example.campusgo.ui.pedido

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.campusgo.ui.perfil.PerfilTabContent
import com.example.campusgo.ui.perfil.PerfilViewModel
import kotlinx.coroutines.launch

private const val TAB_PEDIDOS = 0
private const val TAB_CRIAR = 1
private const val TAB_PERFIL = 2

// Home do Utilizador: Scaffold único com TopAppBar (título muda por separador) + NavigationBar
// fixa em baixo. Trocar de separador só troca o conteúdo — não é navegação com back stack, ao
// contrário do DetalhePedidoScreen/EditarPerfilScreen, que continuam a ser ecrãs empurrados a
// partir daqui. Abre por omissão no separador central, "Criar pedido".
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilizadorHomeScreen(
    pedidoViewModel: PedidoViewModel,
    perfilViewModel: PerfilViewModel,
    onAbrirDetalhe: (Long) -> Unit,
    onEditarPerfil: () -> Unit,
    onLogout: () -> Unit
) {
    var tabSelecionado by rememberSaveable { mutableIntStateOf(TAB_CRIAR) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val titulo = when (tabSelecionado) {
        TAB_PEDIDOS -> "Os meus pedidos"
        TAB_CRIAR -> "Criar pedido"
        else -> "Perfil"
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(titulo) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tabSelecionado == TAB_PEDIDOS,
                    onClick = { tabSelecionado = TAB_PEDIDOS },
                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                    label = { Text("Pedidos") }
                )
                NavigationBarItem(
                    selected = tabSelecionado == TAB_CRIAR,
                    onClick = { tabSelecionado = TAB_CRIAR },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    label = { Text("Criar") }
                )
                NavigationBarItem(
                    selected = tabSelecionado == TAB_PERFIL,
                    onClick = { tabSelecionado = TAB_PERFIL },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { paddingValues ->
        val modifierConteudo = Modifier.padding(paddingValues)
        when (tabSelecionado) {
            TAB_PEDIDOS -> ListaPedidosContent(
                modifier = modifierConteudo,
                viewModel = pedidoViewModel,
                onAbrirDetalhe = onAbrirDetalhe
            )
            TAB_CRIAR -> CriarPedidoContent(
                modifier = modifierConteudo,
                viewModel = pedidoViewModel,
                onPedidoCriado = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Pedido criado com sucesso")
                    }
                }
            )
            else -> PerfilTabContent(
                modifier = modifierConteudo,
                viewModel = perfilViewModel,
                onEditarPerfil = onEditarPerfil,
                onLogout = onLogout
            )
        }
    }
}
