package co.morillas.auth.context

import co.morillas.auth.core.application.service.BCryptPasswordHasher
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.algorithms.Algorithm
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.concurrent.TimeUnit

object AuthProvider {

	val passwordHasher by lazy {
		BCryptPasswordHasher()
	}

	val issuer: String = "http://localhost:8080/jwk"

	val jwkProvider by lazy {
		JwkProviderBuilder(ClassLoader.getSystemResource("static/.well-known/jwks.json")) //JwkProviderBuilder(issuer)
			.cached(10, 24, TimeUnit.HOURS)
			.rateLimited(10, 1, TimeUnit.MINUTES)
			.build()
	}

	val publicKey by lazy {
		var keyContent = String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("publickey.crt").toURI())))

		keyContent = keyContent.replace(System.lineSeparator(), "")
			.replace("-----BEGIN PUBLIC KEY-----", "")
			.replace("-----END PUBLIC KEY-----", "")

		val kf = KeyFactory.getInstance("RSA")
		val keySpecX509 = X509EncodedKeySpec(Base64.getDecoder().decode(keyContent))

		kf.generatePublic(keySpecX509) as RSAPublicKey
	}

	val privateKey by lazy {
		var keyContent = String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("pkcs8.key").toURI())))

		keyContent = keyContent.replace(System.lineSeparator(), "")
			.replace("-----BEGIN PRIVATE KEY-----", "")
			.replace("-----END PRIVATE KEY-----", "")

		val kf = KeyFactory.getInstance("RSA")
		val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyContent))

		kf.generatePrivate(keySpecPKCS8) as RSAPrivateKey
	}

	val algorithm by lazy {
		Algorithm.RSA256(publicKey, privateKey)
	}
}
