package co.morillas.auth.http

import io.ktor.server.application.*

interface HttpController {
    fun routing(a: Application)
}