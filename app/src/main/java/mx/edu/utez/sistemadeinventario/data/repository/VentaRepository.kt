package mx.edu.utez.sistemadeinventario.data.repository

import kotlinx.coroutines.flow.first
import mx.edu.utez.sistemadeinventario.data.local.dao.CarritoDao
import mx.edu.utez.sistemadeinventario.data.remote.ApiService
import mx.edu.utez.sistemadeinventario.data.remote.dto.ItemVentaRequest
import mx.edu.utez.sistemadeinventario.data.remote.dto.VentaRequest
import mx.edu.utez.sistemadeinventario.data.remote.dto.VentaResponse

class VentaRepository(
    private val api: ApiService,
    private val carritoDao: CarritoDao,
    private val productoRepository: ProductoRepository
) {

    suspend fun confirmarVenta(): Result<VentaResponse> {
        val items = carritoDao.observeAll().first()
        if (items.isEmpty()) {
            return Result.failure(Exception("El carrito esta vacio"))
        }

        val request = VentaRequest(
            items = items.map { ItemVentaRequest(productoId = it.productoId, cantidad = it.cantidad) }
        )

        val resultado = safeApiCall { api.crearVenta(request) }
        if (resultado.isSuccess) {
            carritoDao.clear()
            productoRepository.refresh()
        }
        return resultado
    }
}
