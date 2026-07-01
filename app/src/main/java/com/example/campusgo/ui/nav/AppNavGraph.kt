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
import androidx.navigation.compose.rememberNavController
import com.example.campusgo.data.SessionManager
import com.example.campusgo.data.UtilizadorRepository
import com.example.campusgo.ui.auth.AuthViewModelFactory
import com.example.campusgo.ui.auth.LoginScreen
import com.example.campusgo.ui.auth.RegisterScreen

private const val ROTA_LOGIN = "login"
private const val ROTA_REGISTO = "registo"
private const val ROTA_HOME = "home"

@Composable
fun AppNavGraph(
    repository: UtilizadorRepository,
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {
    val destinoInicial = if (sessionManager.isLoggedIn()) ROTA_HOME else ROTA_LOGIN

    NavHost(navController = navController, startDestination = destinoInicial) {
        composable(ROTA_LOGIN) {
            LoginScreen(
                viewModel = viewModel(factory = AuthViewModelFactory(repository, sessionManager)),
                onLoginSuccess = {
                    navController.navigate(ROTA_HOME) {
                        popUpTo(ROTA_LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(ROTA_REGISTO) }
            )
        }
        composable(ROTA_REGISTO) {
            RegisterScreen(
                viewModel = viewModel(factory = AuthViewModelFactory(repository, sessionManager)),
                onRegisterSuccess = {
                    navController.navigate(ROTA_HOME) {
                        popUpTo(ROTA_LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(ROTA_HOME) {
            HomePlaceholderScreen(
                tipoPerfil = sessionManager.getTipoPerfil()?.name.orEmpty(),
                onLogout = {
                    sessionManager.logout()
                    navController.navigate(ROTA_LOGIN) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}

/**
 * Ecrã temporário só para validar o fluxo de login/registo/sessão.
 * Será substituído pelos ecrãs reais de pedidos (utilizador) e gestão (admin).
 */
@Composable
private fun HomePlaceholderScreen(tipoPerfil: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sessão iniciada", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(text = "Perfil: $tipoPerfil")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onLogout) {
            Text("Terminar sessão")
        }
    }
}
