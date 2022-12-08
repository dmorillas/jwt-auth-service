package co.morillas.auth.http.controller.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(val email: String, val password: String)