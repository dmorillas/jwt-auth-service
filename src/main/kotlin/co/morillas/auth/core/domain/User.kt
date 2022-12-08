package co.morillas.auth.core.domain

data class User(
    val id: String,
    val username: String,
    val hashedPassword: String,
    val anonymous: Boolean,
    val created_at: Long,
    val updated_at: Long
)
