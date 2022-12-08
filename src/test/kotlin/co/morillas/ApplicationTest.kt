package co.morillas

import io.ktor.http.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.test.*
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions

class ApplicationTest {
    @Test
    fun testStatus() = testApplication {
        val response = client.get("/status")
        assertEquals(HttpStatusCode.OK, response.status)
        Assertions.assertThat(response.bodyAsText()).contains("UP")
    }
}