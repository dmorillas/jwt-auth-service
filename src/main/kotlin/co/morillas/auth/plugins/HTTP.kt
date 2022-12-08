package co.morillas.auth.plugins

import co.morillas.auth.context.MetricsProvider.appMicrometerRegistry
import co.morillas.auth.core.exception.PasswordIncorrectException
import co.morillas.auth.core.exception.UserNotFoundException
import co.morillas.auth.core.exception.UsernameExistsException
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import org.slf4j.LoggerFactory
import java.time.Duration

fun Application.configureHTTP() {
    val logger = LoggerFactory.getLogger(this::class.java)

    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024)
        }
    }
    install(ContentNegotiation) {
        json()
    }
    install(DefaultHeaders)
    install(CORS) {
        anyHost()
        allowNonSimpleContentTypes = true
        maxAgeInSeconds = Duration.ofDays(1).seconds
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            JvmThreadMetrics(),
            ProcessorMetrics(),
            UptimeMetrics()
        )
    }
    install(ForwardedHeaders)
    install(XForwardedHeaders)
    install(StatusPages) {
        exception<UsernameExistsException> { call, cause ->
            logger.error("Unhandled exception in ${call.request.path()}: ${cause.localizedMessage}", cause)
            call.respond(HttpStatusCode.Conflict, cause::class.java.simpleName + ": " + cause.message)
        }

        exception<UserNotFoundException> { call, cause ->
            logger.error("Unhandled exception in ${call.request.path()}: ${cause.localizedMessage}", cause)
            call.respond(HttpStatusCode.NotFound, cause::class.java.simpleName + ": " + cause.message)
        }

        exception<PasswordIncorrectException> { call, cause ->
            logger.error("Unhandled exception in ${call.request.path()}: ${cause.localizedMessage}", cause)
            call.respond(HttpStatusCode.Unauthorized, cause::class.java.simpleName + ": " + cause.message)
        }

        exception<Exception> { call, cause ->
            logger.error("Unhandled exception in ${call.request.path()}: ${cause.localizedMessage}", cause)
            call.respond(HttpStatusCode.InternalServerError, cause::class.java.simpleName + ": " + cause.message)
        }
    }
}
