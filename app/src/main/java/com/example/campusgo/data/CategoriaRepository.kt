package com.example.campusgo.data

import com.example.campusgo.data.dao.CategoriaDao
import com.example.campusgo.data.model.Categoria
import kotlinx.coroutines.flow.Flow

// Acesso a categorias — só leitura, o CRUD (criar/editar/eliminar) fica do lado do Admin.
class CategoriaRepository(private val categoriaDao: CategoriaDao) {

    // Lista reativa de todas as categorias, usada no dropdown de "Criar pedido".
    fun getTodas(): Flow<List<Categoria>> = categoriaDao.getTodas()
}
