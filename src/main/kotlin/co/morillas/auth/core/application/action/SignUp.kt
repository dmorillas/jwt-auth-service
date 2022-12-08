package co.morillas.auth.core.application.action

import co.morillas.auth.core.application.service.PasswordHasher
import co.morillas.auth.core.domain.User
import co.morillas.auth.core.exception.UsernameExistsException
import co.morillas.auth.core.infrastructure.repository.user.UserRepository
import com.aventrix.jnanoid.jnanoid.NanoIdUtils

interface SignUp {
    suspend fun signUp(username: String, password: String): User?
}

class CustomSignUp(private val repository: UserRepository, private val hasher: PasswordHasher): SignUp {
    override suspend fun signUp(username: String, password: String): User? {
        val user = repository.getByUsername(username)
        if (user != null) {
            throw UsernameExistsException()
        }

        val hashedPassword = hasher.hash(password)
        val now = System.currentTimeMillis()

        return repository.add(
            User(
                id = NanoIdUtils.randomNanoId(),
                username = username,
                hashedPassword = hashedPassword,
                anonymous = false,
                created_at = now,
                updated_at = now
            )
        )
    }
}
