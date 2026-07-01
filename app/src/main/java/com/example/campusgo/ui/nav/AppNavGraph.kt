package com.example.campusgo.ui.nav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.campusgo.data.CategoriaRepository
import com.example.campusgo.data.PedidoRepository
import com.example.campusgo.data.SessionManager
import com.example.campusgo.data.UtilizadorRepository
import com.example.campusgo.data.model.TipoPerfil
import com.example.campusgo.ui.auth.AuthViewModelFactory
import com.example.campusgo.ui.auth.LoginScreen
import com.example.campusgo.ui.auth.RegisterScreen
import com.example.campusgo.ui.pedido.CriarPedidoScreen
import com.example.campusgo.ui.pedido.PedidoViewModelFactory
import com.example.campusgo.ui.perfil.EditarPerfilScreen
import com.example.campusgo.ui.perfil.PerfilViewModelFactory

private const val ROTA_LOGIN = "login"
private const val ROTA_REGISTO = "registo"
private const val ROTA_EDITAR_PERFIL = "perfil/editar"
private const val GRAFO_ADMIN = "admin"
private const val GRAFO_UTILIZADOR = "utilizador"
private const val ROTA_ADMIN_HOME = "admin/home"
private const val ROTA_UTILIZADOR_HOME = "utilizador/home"
private const val ROTA_CRIAR_PEDIDO = "utilizador/pedido/criar"

@Composable
fun AppNavGraph(
    repository: UtilizadorRepository,
    pedidoRepository: PedidoRepository,
    categoriaRepository: CategoriaRepository,
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {
    val destinoInicial = when {
        !sessionManager.isLoggedIn() -> ROTA_LOGIN
        sessionManager.getTipoPerfil() == TipoPerfil.ADMINISTRADOR -> GRAFO_ADMIN
        else -> GRAFO_UTILIZADOR
    }

    fun navegarParaHomeDoPerfil(tipoPerfil: TipoPerfil?) {
        val grafo = if (tipoPerfil == TipoPerfil.ADMINISTRADOR) GRAFO_ADMIN else GRAFO_UTILIZADOR
        navController.navigate(grafo) {
            popUpTo(ROTA_LOGIN) { inclusive = true }
        }
    }

    fun terminarSessao() {
        sessionManager.logout()
        navController.navigate(ROTA_LOGIN) {
            popUpTo(0)
        }
    }

    NavHost(navController = navController, startDestination = destinoInicial) {
        composable(ROTA_LOGIN) {
            LoginScreen(
                viewModel = viewModel(factory = AuthViewModelFactory(repository, sessionManager)),
                onLoginSuccess = { navegarParaHomeDoPerfil(sessionManager.getTipoPerfil()) },
                onNavigateToRegister = { navController.navigate(ROTA_REGISTO) }
            )
        }
        composable(ROTA_REGISTO) {
            RegisterScreen(
                viewModel = viewModel(factory = AuthViewModelFactory(repository, sessionManager)),
                onRegisterSuccess = { navegarParaHomeDoPerfil(sessionManager.getTipoPerfil()) },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Grafo exclusivo do Administrador — rotas de gestão de pedidos/categorias
        // entram aqui nos próximos dias e nunca ficam alcançáveis a partir do grafo do utilizador.
        navigation(startDestination = ROTA_ADMIN_HOME, route = GRAFO_ADMIN) {
            composable(ROTA_ADMIN_HOME) {
                HomePlaceholderScreen(
                    titulo = "Área de Administrador",
                    onEditarPerfil = { navController.navigate(ROTA_EDITAR_PERFIL) },
                    onCriarPedido = null,
                    onLogout = ::terminarSessao
                )
            }
        }

        // Grafo exclusivo do Utilizador — rotas de listar/cancelar pedidos
        // entram aqui nos próximos dias.
        navigation(startDestination = ROTA_UTILIZADOR_HOME, route = GRAFO_UTILIZADOR) {
            composable(ROTA_UTILIZADOR_HOME) {
                HomePlaceholderScreen(
                    titulo = "Área de Utilizador",
                    onEditarPerfil = { navController.navigate(ROTA_EDITAR_PERFIL) },
                    onCriarPedido = { navController.navigate(ROTA_CRIAR_PEDIDO) },
                    onLogout = ::terminarSessao
                )
            }
            composable(ROTA_CRIAR_PEDIDO) {
                val utilizadorId = sessionManager.getUtilizadorId()
                if (utilizadorId != null) {
                    CriarPedidoScreen(
                        viewModel = viewModel(
                            factory = PedidoViewModelFactory(pedidoRepository, categoriaRepository, utilizadorId)
                        ),
                        onPedidoCriado = { navController.popBackStack() },
                        onVoltar = { navController.popBackStack() }
                    )
                }
            }
        }

        // Ecrã comum aos dois perfis — acessível a partir de qualquer um dos grafos acima.
        composable(ROTA_EDITAR_PERFIL) {
            val utilizadorId = sessionManager.getUtilizadorId()
            if (utilizadorId != null) {
                EditarPerfilScreen(
                    viewModel = viewModel(factory = PerfilViewModelFactory(repository, utilizadorId)),
                    onVoltar = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Ecrã temporário só para validar o fluxo de login/registo/sessão/routing por perfil.
 * Será substituído pelos ecrãs reais de pedidos (utilizador) e gestão (admin).
 */
@Composable
private fun HomePlaceholderScreen(
    titulo: String,
    onEditarPerfil: () -> Unit,
    onCriarPedido: (() -> Unit)?,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = titulo, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        if (onCriarPedido != null) {
            Button(onClick = onCriarPedido) {
                Text("Criar pedido")
            }
            Spacer(Modifier.height(8.dp))
        }
        Button(onClick = onEditarPerfil) {
            Text("Editar perfil")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onLogout) {
            Text("Terminar sessão")
        }
    }
}
