package mx.edu.utez.sistemadeinventario.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.edu.utez.sistemadeinventario.InventarioApplication

class ViewModelFactory(
    private val application: Application,
    private val productoId: Long = -1L
) : ViewModelProvider.Factory {

    private val container = (application as InventarioApplication).appContainer

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val vm: ViewModel = when (modelClass) {
            AuthViewModel::class.java ->
                AuthViewModel(container.authRepository, container.sessionExpired)

            InventarioViewModel::class.java ->
                InventarioViewModel(container.productoRepository, container.authRepository)

            DetalleProductoViewModel::class.java -> {
                require(productoId > 0) { "DetalleProductoViewModel necesita un productoId" }
                DetalleProductoViewModel(productoId, container.productoRepository, container.authRepository)
            }

            ProductoFormViewModel::class.java ->
                ProductoFormViewModel(application, productoId, container.productoRepository)

            ScannerViewModel::class.java ->
                ScannerViewModel(container.productoRepository)

            PosViewModel::class.java ->
                PosViewModel(
                    container.carritoRepository,
                    container.productoRepository,
                    container.ventaRepository
                )

            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
        return vm as T
    }
}
