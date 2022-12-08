package co.morillas.auth.core.domain

data class SignInUser(
    val id: String,
    val username: String,
    val token: String,
    val refreshToken: String
)