package mx.edu.utez.sistemadeinventario.data.repository

import com.google.gson.JsonParser
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class SesionExpiradaException(
    message: String = "Tu sesion expiró, inicia sesion de nuevo"
) : Exception(message)

class SinPermisoException(
    message: String = "No tienes permiso para realizar esta accion"
) : Exception(message)

class SinConexionException(
    message: String = "Sin conexion con el servidor. Revisa tu red"
) : Exception(message)

class NoEncontradoException(
    message: String = "No se encontro lo solicitado"
) : Exception(message)

class ApiException(val code: Int, message: String) : Exception(message)


internal suspend fun <T> safeApiCall(block: suspend () -> Response<T>): Result<T> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("El servidor devolvio una respuesta vacia"))
            }
        } else {
            Result.failure(response.toException())
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(SinConexionException())
    } catch (e: Exception) {
        Result.failure(Exception("Respuesta inesperada del servidor"))
    }
}

private fun <T> Response<T>.toException(): Exception {
    val serverMessage = serverMessage()
    return when (val code = code()) {
        401 -> SesionExpiradaException()
        403 -> SinPermisoException()
        404 -> NoEncontradoException(serverMessage ?: "No se encontro lo solicitado")
        400, 409, 422 -> ApiException(code, serverMessage ?: "Datos invalidos o stock insuficiente")
        else -> ApiException(code, serverMessage ?: "Error del servidor ($code).")
    }
}

private fun <T> Response<T>.serverMessage(): String? = try {
    val raw = errorBody()?.string()
    if (raw.isNullOrBlank()) {
        null
    } else {
        val json = JsonParser.parseString(raw)
        if (json.isJsonObject) {
            val obj = json.asJsonObject
            listOf("message", "mensaje").firstNotNullOfOrNull { key ->
                obj.get(key)
                    ?.takeIf { it.isJsonPrimitive }
                    ?.asString
                    ?.takeIf { it.isNotBlank() }
            }
        } else {
            null
        }
    }
} catch (e: Exception) {
    null
}
