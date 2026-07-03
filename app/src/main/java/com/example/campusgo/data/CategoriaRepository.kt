package com.example.campusgo.data

import com.example.campusgo.data.dao.CategoriaDao
import com.example.campusgo.data.model.Categoria
import kotlinx.coroutines.flow.Flow

// Acesso a categorias. Leitura usada por todos (dropdown de "Criar pedido"); escrita (CRUD)
// só é chamada a partir do lado do Admin (AdminCategoriasViewModel).
class CategoriaRepository(private val categoriaDao: CategoriaDao) {

    // Lista reativa de todas as categorias, usada no dropdown de "Criar pedido" e no Admin.
    fun getTodas(): Flow<List<Categoria>> = categoriaDao.getTodas()

    // Cria uma nova categoria (ação do Admin).
    suspend fun criar(nome: String, descricao: String?): Long =
        categoriaDao.inserir(Categoria(nome = nome, descricao = descricao))

    // Atualiza nome/descrição de uma categoria existente (ação do Admin).
    suspend fun atualizar(categoria: Categoria) = categoriaDao.atualizar(categoria)

    // Elimina uma categoria. A regra de bloqueio se estiver em uso (decisão 3 do NOTAS.md) vive no
    // AdminCategoriasViewModel, que tem acesso ao PedidoRepository para contar os pedidos associados.
    suspend fun eliminar(categoria: Categoria) = categoriaDao.remover(categoria)
}
