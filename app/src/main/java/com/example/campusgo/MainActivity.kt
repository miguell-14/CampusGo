package com.example.campusgo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.campusgo.data.AppDatabase
import com.example.campusgo.data.CategoriaRepository
import com.example.campusgo.data.PedidoRepository
import com.example.campusgo.data.SessionManager
import com.example.campusgo.data.UtilizadorRepository
import com.example.campusgo.ui.nav.AppNavGraph
import com.example.campusgo.ui.theme.CampusGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val utilizadorRepository = UtilizadorRepository(database.utilizadorDao())
        val pedidoRepository = PedidoRepository(database.pedidoDao())
        val categoriaRepository = CategoriaRepository(database.categoriaDao())
        val sessionManager = SessionManager(applicationContext)

        setContent {
            CampusGoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
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
}