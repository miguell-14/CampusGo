package com.example.campusgo.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

// Helper de foto do pedido (câmara + galeria — decisão 2 do NOTAS.md).
object PhotoUtils {

    // Cria um ficheiro temporário no cache e devolve o URI (via FileProvider) onde a câmara grava a foto.
    fun criarUriTemporaria(context: Context): Uri {
        val ficheiro = File.createTempFile("pedido_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", ficheiro)
    }

    // Copia o conteúdo do URI (câmara ou galeria) para o armazenamento interno da app, que sobrevive
    // depois do ficheiro de cache/temporário ser removido, e devolve o caminho absoluto final.
    fun copiarParaArmazenamentoInterno(context: Context, uriOrigem: Uri): String? =
        runCatching {
            val ficheiroDestino = File(context.filesDir, "pedido_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uriOrigem)?.use { input ->
                ficheiroDestino.outputStream().use { output -> input.copyTo(output) }
            }
            ficheiroDestino.absolutePath
        }.getOrNull()
}
