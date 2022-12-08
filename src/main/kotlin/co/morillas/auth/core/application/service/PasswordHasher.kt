package co.morillas.auth.core.application.service

import at.favre.lib.crypto.bcrypt.BCrypt

interface PasswordHasher {
    fun hash(password: String): String
    fun verify(rawPassword: String, hashedPassword: String): Boolean
}

class BCryptPasswordHasher(): PasswordHasher {
    override fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(11, password.toCharArray())
    }

    override fun verify(rawPassword: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), hashedPassword).verified
    }
}