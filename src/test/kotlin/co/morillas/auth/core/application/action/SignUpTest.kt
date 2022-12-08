package co.morillas.auth.core.application.action

import co.morillas.auth.core.application.service.BCryptPasswordHasher
import co.morillas.auth.core.application.service.PasswordHasher
import co.morillas.auth.core.domain.User
import co.morillas.auth.core.exception.UsernameExistsException
import co.morillas.auth.core.infrastructure.repository.user.MemUserRepository
import co.morillas.auth.core.infrastructure.repository.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SignUpTest {
    @ParameterizedTest
    @ValueSource(strings = ["aaa", "bbb", "ccc"])
    fun `when username already exist throws exception`(username: String): Unit = runBlocking {
        val repository = mockk<UserRepository>()
        val passwordHasher = mockk<PasswordHasher>()

        every { repository.getByUsername(username) } returns User("", username, "", false, 0, 0)

        val action = CustomSignUp(repository, passwordHasher)

        assertThatThrownBy {
            runBlocking {  action.signUp(username, "password") }
        }.isInstanceOf(UsernameExistsException::class.java)
    }

    @ParameterizedTest
    @ValueSource(strings = ["aaa", "bbb", "ccc"])
    fun `user is created and saved into the repository`(username: String): Unit = runBlocking {
        val repository = spyk(MemUserRepository())
        val passwordHasher = spyk(BCryptPasswordHasher())

        every { repository.getByUsername(username) } returns null

        val action = CustomSignUp(repository, passwordHasher)
        action.signUp(username, "password")

        verify(exactly = 1) {
            repository.add(withArg {
                assertThat(it.username).isEqualTo(username)
                assertThat(it.anonymous).isEqualTo(false)
            })
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["aaa", "bbb", "ccc"])
    fun `user is created and returned`(username: String): Unit = runBlocking {
        val repository = spyk(MemUserRepository())
        val passwordHasher = spyk(BCryptPasswordHasher())

        every { repository.getByUsername(username) } returns null

        val action = CustomSignUp(repository, passwordHasher)
        val user = action.signUp(username, "password")

        assertThat(user?.username).isEqualTo(username)
        assertThat(user?.anonymous).isFalse
    }
}
