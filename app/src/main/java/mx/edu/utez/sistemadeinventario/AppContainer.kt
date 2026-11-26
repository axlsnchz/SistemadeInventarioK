package mx.edu.utez.sistemadeinventario

import android.content.Context
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
    val apiService: ApiService = RetrofitClient { sessionRepository.getTokenSync() }.api

    // --- Repositorios (Fase 4) ---
    val authRepository = AuthRepository(apiService, sessionRepository, database.productoDao(), database.carritoDao())
    val productoRepository = ProductoRepository(apiService, database)
    val carritoRepository = CarritoRepository(database.carritoDao())
    val ventaRepository = VentaRepository(apiService, database.carritoDao(), productoRepository)
}
