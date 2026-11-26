package mx.edu.utez.sistemadeinventario.data.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.utez.sistemadeinventario.data.local.SessionPreferencesRepository
import mx.edu.utez.sistemadeinventario.data.local.UserSession
import mx.edu.utez.sistemadeinventario.data.local.dao.CarritoDao
import mx.edu.utez.sistemadeinventario.data.local.dao.ProductoDao
import mx.edu.utez.sistemadeinventario.data.remote.ApiService
import mx.edu.utez.sistemadeinventario.data.remote.dto.LoginRequest

class AuthRepository(
    private val api: ApiService,
    private val session: SessionPreferencesRepository,
    private val productoDao: ProductoDao,
    private val carritoDao: CarritoDao
) {

    val sessionFlow: Flow<UserSession> = session.sessionFlow

    suspend fun login(username: String, password: String): Result<UserSession> {
        val user = username.trim()
        if (user.isEmpty() || password.isEmpty()) {
            return Result.failure(Exception("Escribe tu usuario y contraseña"))
        }

        return safeApiCall { api.login(LoginRequest(user, password)) }.fold(
            onSuccess = { resp ->

                val rol = resp.rol.removePrefix("ROLE_").uppercase()
                session.saveSession(token = resp.token, username = resp.username, rol = rol)
                Result.success(
                    UserSession(
                        token = resp.token,
                        username = resp.username,
                        rol = rol,
                        isLoggedIn = true
                    )
                )
            },
            onFailure = { error ->
                when (error) {
                    is SesionExpiradaException ->
                        if (error.fromServer) Result.failure(error)
                        else Result.failure(Exception("Usuario o contrasena incorrectos"))
                    is SinPermisoException ->
                        Result.failure(Exception("Usuario o contraseña incorrectos"))
                    else -> Result.failure(error)
                }
            }
        )
    }

    suspend fun logout() {
        session.clearSession()
        productoDao.clear()
        carritoDao.clear()
    }
}