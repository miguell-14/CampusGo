package com.example.campusgo.data

import android.content.Context
import com.example.campusgo.data.model.TipoPerfil

class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun login(utilizadorId: Long, tipoPerfil: TipoPerfil) {
        prefs.edit()
            .putLong(KEY_UTILIZADOR_ID, utilizadorId)
            .putString(KEY_TIPO_PERFIL, tipoPerfil.name)
            .apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun getUtilizadorId(): Long? {
        val id = prefs.getLong(KEY_UTILIZADOR_ID, -1L)
        return if (id == -1L) null else id
    }

    fun getTipoPerfil(): TipoPerfil? =
        prefs.getString(KEY_TIPO_PERFIL, null)?.let { TipoPerfil.valueOf(it) }

    fun isLoggedIn(): Boolean = getUtilizadorId() != null

    private companion object {
        const val PREFS_NAME = "campusgo_session"
        const val KEY_UTILIZADOR_ID = "utilizador_id"
        const val KEY_TIPO_PERFIL = "tipo_perfil"
    }
}
