package com.example.campusgo.ui.pedido

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.campusgo.data.model.Categoria
import com.example.campusgo.util.PhotoUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarPedidoScreen(
    viewModel: PedidoViewModel,
    onPedidoCriado: () -> Unit,
    onVoltar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val context = LocalContext.current

    // Estado local do formulário (a UI é stateless em relação aos dados persistidos).
    var categoriaSelecionada by remember { mutableStateOf<Categoria?>(null) }
    var dropdownAberto by remember { mutableStateOf(false) }
    var localizacao by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Caminho final da foto (já copiada para armazenamento interno) e URI temporário da câmara
    // enquanto se espera pelo resultado da captura — ver PhotoUtils.kt e decisão 2 do NOTAS.md.
    var fotografiaPath by remember { mutableStateOf<String?>(null) }
    var uriTemporariaCamera by remember { mutableStateOf<Uri?>(null) }

    // Depois da câmara tirar a foto com sucesso, copia-a do URI temporário para o armazenamento interno.
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { sucesso ->
        if (sucesso) {
            uriTemporariaCamera?.let { uri -> fotografiaPath = PhotoUtils.copiarParaArmazenamentoInterno(context, uri) }
        }
    }

    // Só depois de a permissão CAMERA ser concedida é que se cria o URI temporário e se abre a câmara.
    val permissaoCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
        if (concedida) {
            val uri = PhotoUtils.criarUriTemporaria(context)
            uriTemporariaCamera = uri
            cameraLauncher.launch(uri)
        }
    }

    // Escolha da galeria: o seletor de fotos do sistema não exige permissão de armazenamento.
    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            fotografiaPath = PhotoUtils.copiarParaArmazenamentoInterno(context, uri)
        }
    }

    // Pede a permissão da câmara só se ainda não tiver sido concedida.
    fun tirarFoto() {
        val temPermissao = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (temPermissao) {
            val uri = PhotoUtils.criarUriTemporaria(context)
            uriTemporariaCamera = uri
            cameraLauncher.launch(uri)
        } else {
            permissaoCameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Ao submeter com sucesso, limpa o estado e volta para trás (fecha o ecrã).
    LaunchedEffect(uiState.submitSucesso) {
        if (uiState.submitSucesso) {
            viewModel.limparEstadoSubmissao()
            onPedidoCriado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Criar pedido", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        // Dropdown de seleção de categoria (lista vem do CategoriaRepository via ViewModel).
        ExposedDropdownMenuBox(
            expanded = dropdownAberto,
            onExpandedChange = { dropdownAberto = it }
        ) {
            OutlinedTextField(
                value = categoriaSelecionada?.nome ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoria") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownAberto) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = dropdownAberto,
                onDismissRequest = { dropdownAberto = false }
            ) {
                categorias.forEach { categoria ->
                    DropdownMenuItem(
                        text = { Text(categoria.nome) },
                        onClick = {
                            categoriaSelecionada = categoria
                            dropdownAberto = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        // Campos de texto simples do formulário (localização e descrição do pedido).
        OutlinedTextField(
            value = localizacao,
            onValueChange = { localizacao = it },
            label = { Text("Localização") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // Fotografia opcional: tirar na hora (câmara) ou escolher da galeria — decisão 2 do NOTAS.md.
        Text(text = "Fotografia (opcional)", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))

        // Pré-visualização simples a partir do ficheiro já copiado para armazenamento interno.
        fotografiaPath?.let { path ->
            val bitmap = remember(path) { BitmapFactory.decodeFile(path)?.asImageBitmap() }
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Pré-visualização da fotografia do pedido",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(Modifier.height(4.dp))
            }
            TextButton(onClick = { fotografiaPath = null }) {
                Text("Remover foto")
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(onClick = ::tirarFoto, modifier = Modifier.weight(1f)) {
                Text("Tirar foto")
            }
            OutlinedButton(
                onClick = {
                    galeriaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Da galeria")
            }
        }

        (validationError ?: uiState.submitError)?.let { mensagem ->
            Spacer(Modifier.height(8.dp))
            Text(text = mensagem, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        // Botão de submissão: valida localmente antes de chamar o ViewModel.
        Button(
            onClick = {
                validationError = validarPedido(categoriaSelecionada, localizacao, descricao)
                val categoria = categoriaSelecionada
                if (validationError == null && categoria != null) {
                    viewModel.limparEstadoSubmissao()
                    viewModel.criarPedido(
                        categoriaId = categoria.id,
                        localizacao = localizacao.trim(),
                        descricao = descricao.trim(),
                        fotografiaPath = fotografiaPath
                    )
                }
            },
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Submeter pedido")
            }
        }

        TextButton(
            onClick = onVoltar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

// Validação de formulário: todos os campos são obrigatórios exceto a fotografia.
private fun validarPedido(
    categoria: Categoria?,
    localizacao: String,
    descricao: String
): String? = when {
    categoria == null -> "Seleciona uma categoria"
    localizacao.isBlank() -> "Introduz a localização"
    descricao.isBlank() -> "Introduz a descrição"
    else -> null
}
