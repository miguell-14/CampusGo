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

    suspend fun atualizarPerfil(
        utilizadorId: Long,
        nome: String,
        email: String,
        novaPassword: String?
    ): Result<Unit> {
        val atual = utilizadorDao.getById(utilizadorId)
            ?: return Result.failure(IllegalStateException("Utilizador não encontrado"))

        val outroComMesmoEmail = utilizadorDao.getByEmail(email)
        if (outroComMesmoEmail != null && outroComMesmoEmail.id != utilizadorId) {
            return Result.failure(IllegalStateException("Já existe uma conta com este email"))
        }

        val atualizado = atual.copy(
            nome = nome,
            email = email,
            passwordHash = if (novaPassword.isNullOrBlank()) {
                atual.passwordHash
            } else {
                PasswordUtils.hash(novaPassword)
            }
        )
        utilizadorDao.atualizar(atualizado)
        return Result.success(Unit)
    }
}
