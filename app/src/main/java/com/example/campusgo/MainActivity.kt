package com.example.campusgo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.campusgo.data.AppDatabase
import com.example.campusgo.data.CategoriaRepository
import com.example.campusgo.data.PedidoRepository
import com.example.campusgo.data.SessionManager
import com.example.campusgo.data.UtilizadorRepository
import com.example.campusgo.ui.nav.AppNavGraph
import com.example.campusgo.ui.theme.CampusGoTheme

// Única Activity da app (Single Activity + Compose). Monta as dependências "à mão"
// (sem DI framework) e passa-as ao AppNavGraph, que trata de toda a navegação.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Instância única da BD e dos repositórios/sessão, criados aqui e injetados manualmente.
        val database = AppDatabase.getInstance(applicationContext)
        val utilizadorRepository = UtilizadorRepository(database.utilizadorDao())
        val pedidoRepository = PedidoRepository(database.pedidoDao())
        val categoriaRepository = CategoriaRepository(database.categoriaDao())
        val sessionManager = SessionManager(applicationContext)

        setContent {
            CampusGoTheme {
                // Surface simples (não Scaffold) só para pintar o fundo do tema atrás de tudo —
                // sem isto, ecrãs sem o seu próprio Scaffold (Login/Registo) ficavam com o branco
                // por omissão da janela, que destoava do fundo branco do logótipo. Um Scaffold
                // aqui voltaria a duplicar os insets do sistema (ver histórico da NavigationBar).
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        repository = utilizadorRepository,
                        pedidoRepository = pedidoRepository,
                        categoriaRepository = categoriaRepository,
                        sessionManager = sessionManager
                    )
                }
            }
        }
    }
}