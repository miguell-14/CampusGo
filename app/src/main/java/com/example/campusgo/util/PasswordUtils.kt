package com.example.campusgo.util

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordUtils {

    private const val SALT_BYTES = 16

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val digest = sha256(salt + password.toByteArray())
        return "${salt.toHex()}:${digest.toHex()}"
    }

    fun verify(password: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false
        val salt = parts[0].fromHex()
        val expectedDigest = parts[1]
        val actualDigest = sha256(salt + password.toByteArray()).toHex()
        return actualDigest == expectedDigest
    }

    private fun sha256(bytes: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(bytes)

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.fromHex(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
