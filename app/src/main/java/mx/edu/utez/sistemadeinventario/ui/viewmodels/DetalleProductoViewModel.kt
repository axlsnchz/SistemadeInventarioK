package mx.edu.utez.sistemadeinventario.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.edu.utez.sistemadeinventario.data.local.entity.ProductoEntity
import mx.edu.utez.sistemadeinventario.data.repository.AuthRepository
import mx.edu.utez.sistemadeinventario.data.repository.ProductoRepository

class DetalleProductoViewModel(
    private val productoId: Long,
    private val productoRepository: ProductoRepository,
    authRepository: AuthRepository
) : ViewModel() {

    val producto: StateFlow<ProductoEntity?> = productoRepository.observarProducto(productoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isAdmin: StateFlow<Boolean> = authRepository.sessionFlow
        .map { it.isAdmin }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        recargar()
    }

    fun recargar() {
        viewModelScope.launch {
            _cargando.value = true
            productoRepository.obtenerProducto(productoId)
                .onSuccess { _error.value = null }
                .onFailure { _error.value = it.message }
            _cargando.value = false
        }
    }
}
