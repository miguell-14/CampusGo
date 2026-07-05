package com.example.campusgo.ui.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.campusgo.data.CategoriaRepository
import com.example.campusgo.data.PedidoRepository
import com.example.campusgo.data.SessionManager
import com.example.campusgo.data.UtilizadorRepository
import com.example.campusgo.data.model.TipoPerfil
import com.example.campusgo.ui.admin.AdminCategoriasViewModelFactory
import com.example.campusgo.ui.admin.AdminDetalhePedidoScreen
import com.example.campusgo.ui.admin.AdminHomeScreen
import com.example.campusgo.ui.admin.AdminViewModelFactory
import com.example.campusgo.ui.auth.AuthViewModelFactory
import com.example.campusgo.ui.auth.LoginScreen
import com.example.campusgo.ui.auth.RegisterScreen
import com.example.campusgo.ui.pedido.DetalhePedidoScreen
import com.example.campusgo.ui.pedido.PedidoViewModelFactory
import com.example.campusgo.ui.pedido.UtilizadorHomeScreen
import com.example.campusgo.ui.perfil.EditarPerfilScreen
import com.example.campusgo.ui.perfil.PerfilViewModelFactory

private const val ROTA_LOGIN = "login"
private const val ROTA_REGISTO = "registo"
private const val ROTA_EDITAR_PERFIL = "perfil/editar"
private const val GRAFO_ADMIN = "admin"
private const val GRAFO_UTILIZADOR = "utilizador"
private const val ROTA_ADMIN_HOME = "admin/home"
private const val ROTA_UTILIZADOR_HOME = "utilizador/home"
private const val ROTA_DETALHE_PEDIDO_BASE = "utilizador/pedido/detalhe"
private const val ARG_PEDIDO_ID = "pedidoId"
private const val ROTA_ADMIN_DETALHE_PEDIDO_BASE = "admin/pedido/detalhe"

// Grafo de navegação único da app. O controlo de acesso por perfil é estrutural:
// cada perfil tem o seu próprio grafo aninhado (GRAFO_ADMIN / GRAFO_UTILIZADOR) e as
// rotas de um nunca são alcançáveis a partir do outro.
@Composable
fun AppNavGraph(
    repository: UtilizadorRepository,
    pedidoRepository: PedidoRepository,
    categoriaRepository: CategoriaRepository,
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {
    // Destino inicial: login se não houver sessão, senão o grafo do perfil guardado.
    val destinoInicial = when {
        !sessionManager.isLoggedIn() -> ROTA_LOGIN
        sessionManager.getTipoPerfil() == TipoPerfil.ADMINISTRADOR -> GRAFO_ADMIN
        else -> GRAFO_UTILIZADOR
    }

    // Usado após login/registo — limpa o back stack para não voltar ao ecrã de login.
    fun navegarParaHomeDoPerfil(tipoPerfil: TipoPerfil?) {
        val grafo = if (tipoPerfil == TipoPerfil.ADMINISTRADOR) GRAFO_ADMIN else GRAFO_UTILIZADOR
        navController.navigate(grafo) {
            popUpTo(ROTA_LOGIN) { inclusive = true }
        }
    }

    // Limpa a sessão e o back stack inteiro, voltando ao login.
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

        // Grafo exclusivo do Administrador — home com separadores (Categorias/Pedidos/Perfil) +
        // ecrãs empurrados a partir dela (detalhe do pedido, editar perfil).
        navigation(startDestination = ROTA_ADMIN_HOME, route = GRAFO_ADMIN) {
            composable(ROTA_ADMIN_HOME) {
                val utilizadorId = sessionManager.getUtilizadorId()
                if (utilizadorId != null) {
                    AdminHomeScreen(
                        adminViewModel = viewModel(
                            factory = AdminViewModelFactory(pedidoRepository, categoriaRepository, repository)
                        ),
                        adminCategoriasViewModel = viewModel(
                            factory = AdminCategoriasViewModelFactory(categoriaRepository, pedidoRepository)
                        ),
                        perfilViewModel = viewModel(
                            factory = PerfilViewModelFactory(repository, utilizadorId)
                        ),
                        onAbrirDetalhe = { pedidoId ->
                            navController.navigate("$ROTA_ADMIN_DETALHE_PEDIDO_BASE/$pedidoId")
                        },
                        onEditarPerfil = { navController.navigate(ROTA_EDITAR_PERFIL) },
                        onLogout = ::terminarSessao
                    )
                }
            }
            composable(
                route = "$ROTA_ADMIN_DETALHE_PEDIDO_BASE/{$ARG_PEDIDO_ID}",
                arguments = listOf(navArgument(ARG_PEDIDO_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val pedidoId = backStackEntry.arguments?.getLong(ARG_PEDIDO_ID)
                if (pedidoId != null) {
                    AdminDetalhePedidoScreen(
                        viewModel = viewModel(
                            factory = AdminViewModelFactory(pedidoRepository, categoriaRepository, repository)
                        ),
                        pedidoId = pedidoId,
                        onVoltar = { navController.popBackStack() }
                    )
                }
            }
        }

        // Grafo exclusivo do Utilizador — home com separadores (Pedidos/Criar/Perfil) + ecrãs
        // empurrados a partir dela (detalhe do pedido, editar perfil).
        navigation(startDestination = ROTA_UTILIZADOR_HOME, route = GRAFO_UTILIZADOR) {
            composable(ROTA_UTILIZADOR_HOME) {
                val utilizadorId = sessionManager.getUtilizadorId()
                if (utilizadorId != null) {
                    UtilizadorHomeScreen(
                        pedidoViewModel = viewModel(
                            factory = PedidoViewModelFactory(pedidoRepository, categoriaRepository, utilizadorId)
                        ),
                        perfilViewModel = viewModel(
                            factory = PerfilViewModelFactory(repository, utilizadorId)
                        ),
                        onAbrirDetalhe = { pedidoId ->
                            navController.navigate("$ROTA_DETALHE_PEDIDO_BASE/$pedidoId")
                        },
                        onEditarPerfil = { navController.navigate(ROTA_EDITAR_PERFIL) },
                        onLogout = ::terminarSessao
                    )
                }
            }
            composable(
                route = "$ROTA_DETALHE_PEDIDO_BASE/{$ARG_PEDIDO_ID}",
                arguments = listOf(navArgument(ARG_PEDIDO_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val utilizadorId = sessionManager.getUtilizadorId()
                val pedidoId = backStackEntry.arguments?.getLong(ARG_PEDIDO_ID)
                if (utilizadorId != null && pedidoId != null) {
                    DetalhePedidoScreen(
                        viewModel = viewModel(
                            factory = PedidoViewModelFactory(pedidoRepository, categoriaRepository, utilizadorId)
                        ),
                        pedidoId = pedidoId,
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
