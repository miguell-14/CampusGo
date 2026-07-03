package com.example.campusgo.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Scaffold partilhado por todos os ecrãs "para dentro" (tudo exceto as duas homes): TopAppBar
// com título e seta de voltar, em vez de cada ecrã ter o seu próprio botão "Voltar"/"Cancelar"
// solto no fundo da coluna — segue o padrão Material 3 em vez de um botão ad-hoc.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcraComTopBar(
    titulo: String,
    onVoltar: () -> Unit,
    content: @Composable (modifierConteudo: Modifier) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        content(Modifier.padding(paddingValues))
    }
}
