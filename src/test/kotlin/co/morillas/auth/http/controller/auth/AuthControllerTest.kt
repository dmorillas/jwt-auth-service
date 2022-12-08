package co.morillas.auth.http.controller.auth

import co.morillas.auth.context.AuthProvider
import co.morillas.auth.context.AuthProvider.issuer
import co.morillas.auth.context.AuthProvider.passwordHasher
import co.morillas.auth.context.RepositoriesProvider
import co.morillas.auth.core.domain.User
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions.assertThat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.*

class AuthControllerTest {
    @Test
    fun testStatus() = testApplication {
        client.get("/status").let {
            assertEquals(HttpStatusCode.OK, it.status)
            assertThat(it.bodyAsText()).contains("UP")
        }
    }

    @Test
    fun `when register user that exists then returns code 490`() : Unit = myTestApplication { client ->
        RepositoriesProvider.userRepository.add(User("", "username", "", false, 0, 0))

        client.post("/signup") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("username", "password"))
        }.let {
            assertThat(it.status).isEqualTo(HttpStatusCode.Conflict)
        }
    }

    @Test
    fun `when register user does not exist then adds it to repository and returns 200`() : Unit = myTestApplication { client ->
        client.post("/signup") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("username", "password"))
        }.let {
            assertThat(it.status).isEqualTo(HttpStatusCode.OK)
        }

        val user = RepositoriesProvider.userRepository.getByUsername("username")
        assertThat(user).isNotNull
        assertThat(user?.anonymous).isFalse
        assertThat(user?.username).isEqualTo("username")
    }

    @Test
    fun `when sign in user does not exist then returns 404`() : Unit = myTestApplication { client ->
        client.post("/signin") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("username", "password"))
        }.let {
            assertThat(it.status).isEqualTo(HttpStatusCode.NotFound)
        }
    }

    @Test
    fun `when sign in user with incorrect password then returns 401`() : Unit = myTestApplication { client ->
        RepositoriesProvider.userRepository.add(User("", "username", "hashedPassword", false, 0, 0))

        client.post("/signin") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("username", "rawPassword"))
        }.let {
            assertThat(it.status).isEqualTo(HttpStatusCode.Unauthorized)
        }
    }

    @Test
    fun `when sign in user correct user then returns 200 and tokens`() : Unit = myTestApplication { client ->
        val hashedPassword = passwordHasher.hash("rawPassword")
        RepositoriesProvider.userRepository.add(User("", "username", hashedPassword, false, 0, 0))

        client.post("/signin") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("username", "rawPassword"))
        }.let {
            assertThat(it.status).isEqualTo(HttpStatusCode.OK)

            val userResponse = it.body<UserResponse>()
            assertThat(userResponse.username).isEqualTo("username")
            assertThat(userResponse.accessToken).isNotBlank
            assertThat(userResponse.refreshToken).isNotBlank
        }
    }

    @Test
    fun `when anonymous sign in then adds it to repository and returns 200`() : Unit = myTestApplication { client ->
        val response = client.post("/signin-anonymous")
        val userResponse = response.body<UserResponse>()

        val user = RepositoriesProvider.userRepository.getByUsername(userResponse.username)
        assertThat(user).isNotNull
        assertThat(user?.anonymous).isTrue
        assertThat(userResponse.accessToken).isNotBlank
        assertThat(userResponse.refreshToken).isNotBlank
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `when invalid refresh token returns error 403`() : Unit = myTestApplication { client ->
        client.post("/refreshtoken") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest("refresh token"))
        }.let {
            assertThat(it.status).isEqualTo(HttpStatusCode.Forbidden)
        }
    }

    @Test
    fun `when token of not type refresh returns error 403`() : Unit = myTestApplication { client ->
        val token = JWT.create()
            .withIssuer("aaa")
            .withClaim("token_use", "a")
            .withExpiresAt(Date.from(Instant.now()))
            .sign(Algorithm.HMAC256("secret"))

        client.post("/refreshtoken") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(token))
        }.let {
            assertThat(it.status).isEqualTo(HttpStatusCode.Forbidden)
        }
    }

    @Test
    fun `when token with wrong issuer returns error 403`() : Unit = myTestApplication { client ->
        val token = JWT.create()
            .withIssuer("aaa")
            .withClaim("token_use", "refresh")
            .withExpiresAt(Date.from(Instant.now()))
            .sign(Algorithm.HMAC256("secret"))

        client.post("/refreshtoken") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(token))
        }.let {
            assertThat(it.status).isEqualTo(HttpStatusCode.Forbidden)
        }
    }

    @Test
    fun `when token is expired returns error 403`() : Unit = myTestApplication { client ->
        val token = JWT.create()
            .withIssuer(issuer)
            .withClaim("token_use", "refresh")
            .withExpiresAt(Date.from(Instant.now()))
            .sign(Algorithm.HMAC256("secret"))

        client.post("/refreshtoken") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(token))
        }.let {
            assertThat(it.status).isEqualTo(HttpStatusCode.Forbidden)
        }
    }

    @Test
    fun `when refresh token is correct returns new token`() : Unit = myTestApplication { client ->
        val token = JWT.create()
            .withIssuer(issuer)
            .withClaim("token_use", "refresh")
            .withClaim("username", "username")
            .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
            .sign(Algorithm.HMAC256("secret"))

        client.post("/refreshtoken") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(token))
        }.let {
            assertThat(it.status).isEqualTo(HttpStatusCode.OK)

            val tokenResponse = it.body<TokenResponse>()
            assertThat(tokenResponse.accessToken).isNotBlank
            assertThat(tokenResponse.refreshToken).isNotBlank

        }
    }
}

fun myTestApplication(block: suspend (HttpClient) -> Unit) = testApplication {
    environment {
        developmentMode = false
    }

    val client = createClient {
        install(ContentNegotiation) {
            json()
        }
    }

    block(client)
}
