package co.morillas.auth.plugins

import co.morillas.auth.context.AuthProvider.issuer
import co.morillas.auth.context.AuthProvider.jwkProvider
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwkProvider, issuer) {
                acceptLeeway(3)
            }

            validate { credential ->
                if (!credential.payload.getClaim("username").asString().isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
