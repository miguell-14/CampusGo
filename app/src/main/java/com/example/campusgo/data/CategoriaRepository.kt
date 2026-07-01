package com.example.campusgo.data

import com.example.campusgo.data.dao.CategoriaDao
import com.example.campusgo.data.model.Categoria
import kotlinx.coroutines.flow.Flow

class CategoriaRepository(private val categoriaDao: CategoriaDao) {

    fun getTodas(): Flow<List<Categoria>> = categoriaDao.getTodas()
}
