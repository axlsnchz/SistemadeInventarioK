package mx.edu.utez.sistemadeinventario.data.remote.dto

data class LoginRequest(
    val username: String,
    val password: String
)


data class LoginResponse(
    val token: String,
    val username: String,
    val rol: String
)
