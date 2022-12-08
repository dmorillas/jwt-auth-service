package co.morillas.auth.http.controller.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(val id: String, val username: String, val accessToken: String, val refreshToken: String)