package mx.edu.utez.sistemadeinventario.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Agrega "Authorization: Bearer <token>" a todas las rutas menos /api/auth/login.
 * Si el servidor responde 401 a una peticion que llevaba token (token vencido),
 * avisa con [onUnauthorized] para cerrar sesion y llevar al login.
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?,
    private val onUnauthorized: () -> Unit = {}
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.url.encodedPath.endsWith("/auth/login")) {
            return chain.proceed(request)
        }

        val token = tokenProvider()
        if (token.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authenticated = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        val response = chain.proceed(authenticated)
        if (response.code == 401) {
            onUnauthorized()
        }
        return response
    }
}
