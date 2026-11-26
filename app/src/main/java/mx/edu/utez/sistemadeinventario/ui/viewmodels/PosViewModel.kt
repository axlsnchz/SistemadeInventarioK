package mx.edu.utez.sistemadeinventario.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.edu.utez.sistemadeinventario.data.local.entity.CarritoEntity
import mx.edu.utez.sistemadeinventario.data.local.entity.ProductoEntity
import mx.edu.utez.sistemadeinventario.data.remote.dto.VentaResponse
import mx.edu.utez.sistemadeinventario.data.repository.CarritoRepository
import mx.edu.utez.sistemadeinventario.data.repository.ProductoRepository
import mx.edu.utez.sistemadeinventario.data.repository.VentaRepository

class PosViewModel(
    private val carritoRepository: CarritoRepository,
    private val productoRepository: ProductoRepository,
    private val ventaRepository: VentaRepository
) : ViewModel() {

    val items: StateFlow<List<CarritoEntity>> = carritoRepository.items
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val total: StateFlow<Double> = carritoRepository.total
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _ventaState = MutableStateFlow<UiState<VentaResponse>>(UiState.Idle)
    val ventaState: StateFlow<UiState<VentaResponse>> = _ventaState.asStateFlow()

    fun agregarPorNfc(texto: String) {
        val nfcId = texto.trim()
        if (nfcId.isEmpty()) {
            _mensaje.value = 
            return
        }
        viewModelScope.launch {
            productoRepository.buscarPorNfc(nfcId).fold(
                onSuccess = { agregarProducto(it) },
                onFailure = { _mensaje.value = it.message }
            )
        }
    }

    fun agregarProducto(producto: ProductoEntity) {
        viewModelScope.launch {
            carritoRepository.agregar(producto).onFailure { _mensaje.value = it.message }
        }
    }

    fun cambiarCantidad(productoId: Long, cantidad: Int) {
        viewModelScope.launch {
            carritoRepository.cambiarCantidad(productoId, cantidad).onFailure { _mensaje.value = it.message }
        }
    }

    fun quitar(productoId: Long) {
        viewModelScope.launch { carritoRepository.quitar(productoId) }
    }

    fun vaciar() {
        viewModelScope.launch { carritoRepository.vaciar() }
    }

    fun confirmarVenta() {
        if (_ventaState.value is UiState.Loading) return
        viewModelScope.launch {
            _ventaState.value = UiState.Loading
            ventaRepository.confirmarVenta().fold(
                onSuccess = { _ventaState.value = UiState.Success(it) },
                onFailure = { _ventaState.value = UiState.Error(it.message ?: "No se pudo registrar la venta") }
            )
        }
    }

    fun reiniciarVenta() {
        _ventaState.value = UiState.Idle
    }

    fun consumirMensaje() {
        _mensaje.value = null
    }
}
