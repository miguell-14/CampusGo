package com.example.campusgo.ui.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.campusgo.ui.perfil.PerfilTabContent
import com.example.campusgo.ui.perfil.PerfilViewModel

private const val TAB_CATEGORIAS = 0
private const val TAB_PEDIDOS = 1
private const val TAB_PERFIL = 2

// Home do Admin: mesmo padrão do UtilizadorHomeScreen — Scaffold com TopAppBar (título muda por
// separador) + NavigationBar fixa (Categorias/Pedidos/Perfil). Abre por omissão no separador
// central, "Pedidos". AdminDetalhePedidoScreen continua a ser um ecrã empurrado a partir daqui.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    adminViewModel: AdminViewModel,
    adminCategoriasViewModel: AdminCategoriasViewModel,
    perfilViewModel: PerfilViewModel,
    onAbrirDetalhe: (Long) -> Unit,
    onEditarPerfil: () -> Unit,
    onLogout: () -> Unit
) {
    var tabSelecionado by rememberSaveable { mutableIntStateOf(TAB_PEDIDOS) }
    val utilizador by perfilViewModel.utilizador.collectAsState()

    val titulo = when (tabSelecionado) {
        TAB_CATEGORIAS -> "Categorias"
        else -> "Perfil"
    }

    Scaffold(
        topBar = {
            // No separador "Pedidos" não há TopAppBar — o "Olá" no conteúdo já faz esse papel.
            if (tabSelecionado != TAB_PEDIDOS) {
                CenterAlignedTopAppBar(
                    title = { Text(titulo) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tabSelecionado == TAB_CATEGORIAS,
                    onClick = { tabSelecionado = TAB_CATEGORIAS },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("Categorias") }
                )
                NavigationBarItem(
                    selected = tabSelecionado == TAB_PEDIDOS,
                    onClick = { tabSelecionado = TAB_PEDIDOS },
                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                    label = { Text("Pedidos") }
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
            TAB_CATEGORIAS -> AdminCategoriasContent(
                modifier = modifierConteudo,
                viewModel = adminCategoriasViewModel
            )
            TAB_PEDIDOS -> AdminPedidosContent(
                modifier = modifierConteudo,
                viewModel = adminViewModel,
                nomeAdmin = utilizador?.nome.orEmpty(),
                onAbrirDetalhe = onAbrirDetalhe
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
