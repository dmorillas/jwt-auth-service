package co.morillas.auth.http.controller.health

import co.morillas.auth.context.MetricsProvider.appMicrometerRegistry
import co.morillas.auth.http.HttpController
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class MetricsController: HttpController {
    override fun routing(a: Application) {
        a.routing {
            get("/metrics") {
                call.respond(appMicrometerRegistry.scrape())
            }
        }
    }
}
