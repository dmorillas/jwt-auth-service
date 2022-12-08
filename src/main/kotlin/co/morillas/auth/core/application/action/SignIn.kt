package co.morillas.auth.core.application.action

import co.morillas.auth.core.application.service.PasswordHasher
import co.morillas.auth.core.application.service.signAccessToken
import co.morillas.auth.core.application.service.signRefreshToken
import co.morillas.auth.core.domain.SignInUser
import co.morillas.auth.core.domain.User
import co.morillas.auth.core.exception.PasswordIncorrectException
import co.morillas.auth.core.exception.UserNotFoundException
import co.morillas.auth.core.infrastructure.repository.user.UserRepository
import com.aventrix.jnanoid.jnanoid.NanoIdUtils

interface SignIn {
    suspend fun signIn(username: String, password: String): SignInUser
    suspend fun anonymous(): SignInUser
}

class CustomSignIn(private val repository: UserRepository, private val hasher: PasswordHasher): SignIn {
    override suspend fun signIn(username: String, password: String): SignInUser {
        val user = repository.getByUsername(username) ?: throw UserNotFoundException()

        if (hasher.verify(password, user.hashedPassword)) {
            return SignInUser(
                id = user.id,
                username = user.username,
                token = signAccessToken(user.username),
                refreshToken = signRefreshToken(user.username)
            )
        }

        throw PasswordIncorrectException()
    }

    override suspend fun anonymous(): SignInUser {
        var username = NanoIdUtils.randomNanoId()
        while (repository.getByUsername(username) != null) {
            username = NanoIdUtils.randomNanoId()
        }
        val password = NanoIdUtils.randomNanoId()

        val hashedPassword = hasher.hash(password)
        val now = System.currentTimeMillis()

        repository.add(
            User(
                id = NanoIdUtils.randomNanoId(),
                username = username,
                hashedPassword = hashedPassword,
                anonymous = true,
                created_at = now,
                updated_at = now
            )
        )

        return signIn(username, password)
    }
}
