package co.morillas.auth.plugins

import co.morillas.auth.context.DeliveryProvider.controllers
import io.ktor.server.routing.*
import io.ktor.server.application.*
import org.slf4j.LoggerFactory

fun Application.configureRouting() {
    val logger = LoggerFactory.getLogger(this::class.java)

    routing {
        if (logger.isTraceEnabled)
            trace { logger.trace(it.buildText()) }
    }

    controllers.forEach { it.routing(this) }
}