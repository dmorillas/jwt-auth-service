package co.morillas.auth.http.controller.auth

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(val accessToken: String, val refreshToken: String)