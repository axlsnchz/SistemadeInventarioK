package mx.edu.utez.sistemadeinventario.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.edu.utez.sistemadeinventario.data.nfc.NfcManager
import mx.edu.utez.sistemadeinventario.data.nfc.NfcWriteResult
import mx.edu.utez.sistemadeinventario.data.remote.dto.ProductoRequest
import mx.edu.utez.sistemadeinventario.data.repository.ProductoRepository
import mx.edu.utez.sistemadeinventario.utils.ImageUtils
import java.util.UUID

data class ProductoFormState(
    val nombre: String = "",
    val sku: String = "",
    val precio: String = "",
    val stock: String = "",
    val stockMinimo: String = "0",
    val categoria: String = "",
    val nfcId: String? = null,
    val fotoUri: Uri? = null,
    val fotoUrlActual: String? = null,
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val grabandoNfc: Boolean = false,
    val error: String? = null,
    val aviso: String? = null,
    val guardado: Boolean = false
)

class ProductoFormViewModel(
    application: Application,
    productoId: Long,
    private val productoRepository: ProductoRepository
) : AndroidViewModel(application) {

    private var idActual: Long = productoId
    private var fotoPendienteUri: Uri? = null
    private var grabarJob: Job? = null

    private val _state = MutableStateFlow(ProductoFormState(esEdicion = productoId > 0))
    val state: StateFlow<ProductoFormState> = _state.asStateFlow()

    init {
        if (productoId > 0) cargarProducto(productoId)
    }

    private fun cargarProducto(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(cargando = true) }
            productoRepository.obtenerProducto(id).fold(
                onSuccess = { p ->
                    _state.update {
                        it.copy(
                            cargando = false,
                            nombre = p.nombre,
                            sku = p.sku.orEmpty(),
                            precio = p.precio.toString(),
                            stock = p.stock.toString(),
                            stockMinimo = p.stockMinimo.toString(),
                            categoria = p.categoria.orEmpty(),
                            nfcId = p.nfcId,
                            fotoUrlActual = p.fotoUrl
                        )
                    }
                },
                onFailure = { e -> _state.update { it.copy(cargando = false, error = e.message) } }
            )
        }
    }

    fun onNombre(v: String) = _state.update { it.copy(nombre = v, error = null) }
    fun onSku(v: String) = _state.update { it.copy(sku = v, error = null) }
    fun onPrecio(v: String) = _state.update { it.copy(precio = v, error = null) }
    fun onStock(v: String) = _state.update { it.copy(stock = v, error = null) }
    fun onStockMinimo(v: String) = _state.update { it.copy(stockMinimo = v, error = null) }
    fun onCategoria(v: String) = _state.update { it.copy(categoria = v, error = null) }
    fun consumirAviso() = _state.update { it.copy(aviso = null) }

    fun prepararFoto(): Uri {
        val ctx = getApplication<Application>()
        val uri = ImageUtils.getUriForFile(ctx, ImageUtils.createTempImageFile(ctx))
        fotoPendienteUri = uri
        return uri
    }

    fun onFotoResultado(exito: Boolean) {
        val uri = fotoPendienteUri
        fotoPendienteUri = null
        if (exito && uri != null) _state.update { it.copy(fotoUri = uri, error = null) }
    }

    fun grabarNfc(nfc: NfcManager) {
        if (_state.value.grabandoNfc) return
        val id = _state.value.nfcId ?: "INV-" + UUID.randomUUID().toString().take(8).uppercase()
        grabarJob = viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            _state.update { it.copy(grabandoNfc = true, error = null) }
            when (val r = nfc.writeEvents.first()) {
                is NfcWriteResult.Exito ->
                    _state.update { it.copy(grabandoNfc = false, nfcId = id, aviso = "Etiqueta grabada: $id") }
                is NfcWriteResult.Error ->
                    _state.update { it.copy(grabandoNfc = false, error = r.mensaje) }
            }
        }
        nfc.startWriting(id)
    }

    fun cancelarGrabado(nfc: NfcManager) {
        nfc.cancelWriting()
        grabarJob?.cancel()
        _state.update { it.copy(grabandoNfc = false) }
    }

    fun guardar() {
        val s = _state.value
        if (s.guardando || s.grabandoNfc) return

        val precio = s.precio.replace(',', '.').toDoubleOrNull()
        val stock = s.stock.toIntOrNull()
        val stockMinimo = s.stockMinimo.toIntOrNull()
        val error = when {
            s.nombre.isBlank() -> "El nombre es obligatorio"
            precio == null || precio < 0 -> "Escribe un precio valido"
            stock == null || stock < 0 -> "Escribe un stock valido"
            stockMinimo == null || stockMinimo < 0 -> "Escribe un stock minimo valido"
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(guardando = true, error = null, aviso = null) }

            val foto = try {
                s.fotoUri?.let { uri ->
                    withContext(Dispatchers.IO) { ImageUtils.uriToMultipart(getApplication(), uri) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(guardando = false, error = "No se pudo leer la foto") }
                return@launch
            }

            val request = ProductoRequest(
                nombre = s.nombre.trim(),
                sku = s.sku.trim().ifEmpty { null },
                precio = precio!!,
                stock = stock!!,
                stockMinimo = stockMinimo!!,
                categoria = s.categoria.trim().ifEmpty { null },
                nfcId = s.nfcId,
                fotoUrl = s.fotoUrlActual
            )

            val resultado = if (idActual > 0) {
                productoRepository.editarProducto(idActual, request, foto)
            } else {
                productoRepository.crearProducto(request, foto)
            }

            resultado.fold(
                onSuccess = { r ->
                    idActual = r.producto.id
                    if (r.completo) {
                        _state.update {
                            it.copy(
                                guardando = false,
                                guardado = true,
                                fotoUri = null,
                                fotoUrlActual = r.producto.fotoUrl
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                guardando = false,
                                esEdicion = true,
                                aviso = "Producto guardado, pero la foto fallo: ${r.errorFoto}. " +
                                    "Toca Guardar para reintentar."
                            )
                        }
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(guardando = false, error = e.message ?: "No se pudo guardar") }
                }
            )
        }
    }

    override fun onCleared() {
        grabarJob?.cancel()
        super.onCleared()
    }
}
