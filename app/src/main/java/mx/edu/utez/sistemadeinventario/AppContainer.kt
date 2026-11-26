package mx.edu.utez.sistemadeinventario

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import mx.edu.utez.sistemadeinventario.data.local.AppDatabase
import mx.edu.utez.sistemadeinventario.data.local.SessionPreferencesRepository
import mx.edu.utez.sistemadeinventario.data.remote.ApiService
import mx.edu.utez.sistemadeinventario.data.remote.RetrofitClient
import mx.edu.utez.sistemadeinventario.data.repository.AuthRepository
import mx.edu.utez.sistemadeinventario.data.repository.CarritoRepository
import mx.edu.utez.sistemadeinventario.data.repository.ProductoRepository
import mx.edu.utez.sistemadeinventario.data.repository.VentaRepository


class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    // --- Local ---
    val database: AppDatabase = AppDatabase.getInstance(appContext)
    val sessionRepository = SessionPreferencesRepository(appContext)

    // --- Remoto (el interceptor lee el token de la sesión) ---
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emite cuando el servidor responde 401 con token: la UI debe llevar al login. */
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    val apiService: ApiService = RetrofitClient(
        tokenProvider = { sessionRepository.getTokenSync() },
        onUnauthorized = {
            appScope.launch {
                authRepository.logout()
                _sessionExpired.emit(Unit)
            }
        }
    ).api

    // --- Repositorios (Fase 4) ---
    val authRepository = AuthRepository(apiService, sessionRepository, database.productoDao(), database.carritoDao())
    val productoRepository = ProductoRepository(apiService, database)
    val carritoRepository = CarritoRepository(database.carritoDao())
    val ventaRepository = VentaRepository(apiService, database.carritoDao(), productoRepository)
}
