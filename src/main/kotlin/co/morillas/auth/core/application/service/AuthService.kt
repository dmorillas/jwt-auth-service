package co.morillas.auth.core.application.service

import co.morillas.auth.context.AuthProvider.algorithm
import co.morillas.auth.context.AuthProvider.issuer
import com.auth0.jwt.JWT
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

fun signAccessToken(username: String): String = JWT.create()
    .withKeyId("a7ee0bd7-611a-4e86-b5b3-0f0e427242e2")
    .withSubject(username)
    .withIssuer(issuer)
    .withClaim("username", username)
    .withClaim("token_use", "access")
    .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
    .withIssuedAt(Date.from(Instant.now()))
    .sign(algorithm)

fun signRefreshToken(username: String): String = JWT.create()
    .withKeyId("a7ee0bd7-611a-4e86-b5b3-0f0e427242e2")
    .withSubject(username)
    .withIssuer(issuer)
    .withClaim("username", username)
    .withClaim("token_use", "refresh")
    .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
    .withIssuedAt(Date.from(Instant.now()))
    .sign(algorithm)