package com.example.circledayplanner.domain.util

import java.security.MessageDigest

object PasswordHasher {
    fun hash(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

