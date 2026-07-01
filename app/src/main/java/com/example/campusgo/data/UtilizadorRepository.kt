package com.example.campusgo.data

import com.example.campusgo.data.dao.UtilizadorDao
import com.example.campusgo.data.model.TipoPerfil
import com.example.campusgo.data.model.Utilizador
import com.example.campusgo.util.PasswordUtils

class UtilizadorRepository(private val utilizadorDao: UtilizadorDao) {

    suspend fun registar(
        nome: String,
        email: String,
        password: String,
        tipoPerfil: TipoPerfil
    ): Result<Long> {
        if (utilizadorDao.getByEmail(email) != null) {
            return Result.failure(IllegalStateException("Já existe uma conta com este email"))
        }
        val utilizador = Utilizador(
            nome = nome,
            email = email,
            passwordHash = PasswordUtils.hash(password),
            tipoPerfil = tipoPerfil
        )
        return Result.success(utilizadorDao.inserir(utilizador))
    }

    suspend fun login(email: String, password: String): Result<Utilizador> {
        val utilizador = utilizadorDao.getByEmail(email)
            ?: return Result.failure(IllegalStateException("Credenciais inválidas"))
        if (!PasswordUtils.verify(password, utilizador.passwordHash)) {
            return Result.failure(IllegalStateException("Credenciais inválidas"))
        }
        return Result.success(utilizador)
    }

    suspend fun getById(id: Long): Utilizador? = utilizadorDao.getById(id)

    suspend fun atualizarPerfil(utilizador: Utilizador) {
        utilizadorDao.atualizar(utilizador)
    }
}
