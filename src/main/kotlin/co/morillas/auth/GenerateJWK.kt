package co.morillas.auth

import co.morillas.auth.context.AuthProvider.privateKey
import co.morillas.auth.context.AuthProvider.publicKey
import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import java.util.*

fun main() {
    val jwk: JWK = RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .keyUse(KeyUse.SIGNATURE)
        .keyID(UUID.randomUUID().toString())
        .algorithm(Algorithm.parse("RS256"))
        .build()

    System.out.println(jwk.toPublicJWK().toJSONString())
}
