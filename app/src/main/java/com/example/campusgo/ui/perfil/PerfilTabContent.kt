package com.example.campusgo.ui.perfil

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.campusgo.util.PhotoUtils

// Conteúdo do separador "Perfil", partilhado pela home do Utilizador e a do Admin: foto de perfil
// (círculo, tocável para escolher câmara/galeria/remover), nome e email por baixo, tudo centrado;
// "Editar perfil" e "Terminar sessão" mais abaixo.
@Composable
fun PerfilTabContent(
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel,
    onEditarPerfil: () -> Unit,
    onLogout: () -> Unit
) {
    val utilizador by viewModel.utilizador.collectAsState()
    val context = LocalContext.current

    var mostrarOpcoesFoto by remember { mutableStateOf(false) }
    var uriTemporariaCamera by remember { mutableStateOf<Uri?>(null) }

    // Mesma lógica de câmara/galeria do CriarPedidoContent, aplicada aqui à foto de perfil.
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { sucesso ->
        if (sucesso) {
            uriTemporariaCamera?.let { uri ->
                viewModel.atualizarFotoPerfil(PhotoUtils.copiarParaArmazenamentoInterno(context, uri))
            }
        }
    }
    val permissaoCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
        if (concedida) {
            val uri = PhotoUtils.criarUriTemporaria(context)
            uriTemporariaCamera = uri
            cameraLauncher.launch(uri)
        }
    }
    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.atualizarFotoPerfil(PhotoUtils.copiarParaArmazenamentoInterno(context, uri))
        }
    }

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Foto de perfil: círculo tocável (com o ícone por omissão se ainda não houver foto) e um
        // pequeno distintivo com lápis no canto, só para indicar visualmente que é editável.
        Box(
            modifier = Modifier
                .size(96.dp)
                .clickable { mostrarOpcoesFoto = true }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val path = utilizador?.fotoPerfilPath
                val bitmap = remember(path) { path?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() } }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(text = utilizador?.nome.orEmpty(), style = MaterialTheme.typography.titleLarge)
        Text(
            text = utilizador?.email.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        Button(onClick = onEditarPerfil, modifier = Modifier.fillMaxWidth()) {
            Text("Editar perfil")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Terminar sessão")
        }
    }

    // Diálogo com as opções de foto — "Remover foto" só aparece se já existir uma. AlertDialog só
    // tem 2 botões nativos (confirm/dismiss), por isso as opções vivem todas no "text" e o
    // confirmButton é só o "Cancelar".
    if (mostrarOpcoesFoto) {
        AlertDialog(
            onDismissRequest = { mostrarOpcoesFoto = false },
            title = { Text("Foto de perfil") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            mostrarOpcoesFoto = false
                            tirarFoto()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tirar foto", modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(
                        onClick = {
                            mostrarOpcoesFoto = false
                            galeriaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Escolher da galeria", modifier = Modifier.fillMaxWidth())
                    }
                    if (utilizador?.fotoPerfilPath != null) {
                        TextButton(
                            onClick = {
                                mostrarOpcoesFoto = false
                                viewModel.atualizarFotoPerfil(null)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Remover foto",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarOpcoesFoto = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
