package co.morillas.auth

import co.morillas.auth.plugins.configureAuthentication
import co.morillas.auth.plugins.configureHTTP
import co.morillas.auth.plugins.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureRouting()
    configureHTTP()
    configureAuthentication()
}
