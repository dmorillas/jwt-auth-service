package co.morillas.auth.core.application.action

import co.morillas.auth.core.application.service.BCryptPasswordHasher
import co.morillas.auth.core.application.service.PasswordHasher
import co.morillas.auth.core.domain.User
import co.morillas.auth.core.exception.PasswordIncorrectException
import co.morillas.auth.core.exception.UserNotFoundException
import co.morillas.auth.core.infrastructure.repository.user.MemUserRepository
import co.morillas.auth.core.infrastructure.repository.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SignInTest {
    @ParameterizedTest
    @ValueSource(strings = ["aaa", "bbb", "ccc"])
    fun `when signIn and user does not exist then throws UserNotFoundException`(username: String): Unit = runBlocking {
        val repository = mockk<UserRepository>()
        val passwordHasher = mockk<PasswordHasher>()

        every { repository.getByUsername(username) } returns null

        val action = CustomSignIn(repository, passwordHasher)

        assertThatThrownBy {
            runBlocking {  action.signIn(username, "password") }
        }.isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `when signIn and password is not correct then throws PasswordIncorrectException`(): Unit = runBlocking {
        val repository = mockk<UserRepository>()
        val passwordHasher = mockk<PasswordHasher>()

        every { repository.getByUsername("username") } returns User("", "username", "pwd", false, 0, 0)
        every { passwordHasher.verify("password", any()) } returns false

        val action = CustomSignIn(repository, passwordHasher)

        assertThatThrownBy {
            runBlocking {  action.signIn("username", "password") }
        }.isInstanceOf(PasswordIncorrectException::class.java)
    }

    @Test
    fun `when credentials are valid then the signed user is returned`(): Unit = runBlocking {
        val repository = mockk<UserRepository>()
        val passwordHasher = mockk<PasswordHasher>()

        every { repository.getByUsername("username") } returns User("", "username", "pwd", false, 0, 0)
        every { passwordHasher.verify("password", any()) } returns true

        val action = CustomSignIn(repository, passwordHasher)
        val signInUser = action.signIn("username", "password")

        assertThat(signInUser.username).isEqualTo("username")
        assertThat(signInUser.token).isNotBlank
        assertThat(signInUser.refreshToken).isNotBlank
    }

    @Test
    fun `when anonymous login it creates an account with random username and password`(): Unit = runBlocking {
        val repository = spyk(MemUserRepository())
        val passwordHasher = spyk(BCryptPasswordHasher())

        val action = CustomSignIn(repository, passwordHasher)
        val signInUser = action.anonymous()

        verify(exactly = 1) {
            repository.add(withArg {
                assertThat(it.username).isNotBlank
                assertThat(it.hashedPassword).isNotBlank
                assertThat(it.anonymous).isEqualTo(true)
            })
        }

        assertThat(signInUser.username).isNotBlank
        assertThat(signInUser.token).isNotBlank
        assertThat(signInUser.refreshToken).isNotBlank
    }
}
