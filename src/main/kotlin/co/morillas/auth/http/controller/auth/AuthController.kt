package co.morillas.auth.http.controller.auth

import co.morillas.auth.context.AuthProvider.issuer
import co.morillas.auth.core.application.action.SignIn
import co.morillas.auth.core.application.action.SignUp
import co.morillas.auth.core.application.service.signAccessToken
import co.morillas.auth.http.HttpController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.text.ParseException
import java.util.*


class AuthController(private val signUp: SignUp, private val signIn: SignIn): HttpController {
    override fun routing(a: Application) {
        a.routing {
            post("/signup") {
                val userRequest = call.receive<UserRequest>()
                signUp.signUp(userRequest.email, userRequest.password)

                call.respond(HttpStatusCode.OK)
            }

            post("/signin") {
                val userRequest = call.receive<UserRequest>()
                val user = signIn.signIn(userRequest.email, userRequest.password)

                call.respond(UserResponse(user.id, user.username, user.token, user.refreshToken))
            }

            post("/signin-anonymous") {
                val user = signIn.anonymous()
                call.respond(UserResponse(user.id, user.username, user.token, user.refreshToken))
            }

            post("/refreshtoken") {
                val tokenRequest = call.receive<RefreshTokenRequest>()

                try {
                    val token = com.nimbusds.jwt.JWTParser.parse(tokenRequest.refreshToken)
                    val claims = token.jwtClaimsSet
                    val isCorrectTokenUse = claims.getClaim("token_use").toString() == "refresh"
                    val isCorrectIssuer = claims.issuer.equals(issuer)
                    val isNotExpired = Date().before(claims.expirationTime)

                    when {
                        !isCorrectTokenUse -> call.respond(HttpStatusCode.Forbidden, "Not refresh token")
                        !isCorrectIssuer -> call.respond(HttpStatusCode.Forbidden, "Invalid issuer")
                        !isNotExpired -> call.respond(HttpStatusCode.Forbidden, "Refresh token expired")
                        else -> {
                            val username = claims.getClaim("username").toString()
                            call.respond(TokenResponse(signAccessToken(username), tokenRequest.refreshToken))
                        }
                    }
                } catch (exception: ParseException) {
                    call.respond(HttpStatusCode.Forbidden, "Invalid refresh token")
                }
            }

            authenticate("auth-jwt") {
                get("/hello") {
                    val principal = call.principal<JWTPrincipal>()
                    val username = principal!!.payload.getClaim("username").asString()
                    val uid = principal!!.payload.subject
                    val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                    call.respondText("Hello, $username! with id $uid. Token expires in $expiresAt ms.")
                }
            }

            static("/jwk") {
                staticBasePackage = "static"
                resources(".")
            }
        }
    }
}