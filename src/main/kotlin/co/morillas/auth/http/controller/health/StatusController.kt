package co.morillas.auth.http.controller.health

import co.morillas.auth.http.HttpController
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class StatusController : HttpController {
    override fun routing(a: Application) {
        a.routing {
            get("/status") {
                call.respondText("{\"status\": \"UP\"}", ContentType.Application.Json)
            }
        }
    }
}
