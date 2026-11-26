package mx.edu.utez.sistemadeinventario.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.edu.utez.sistemadeinventario.data.local.entity.ProductoEntity
import mx.edu.utez.sistemadeinventario.data.repository.AuthRepository
import mx.edu.utez.sistemadeinventario.data.repository.ProductoRepository

@OptIn(ExperimentalCoroutinesApi::class)
class InventarioViewModel(
    private val productoRepository: ProductoRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val productos: StateFlow<List<ProductoEntity>> = _query
        .flatMapLatest { productoRepository.observarProductos(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isAdmin: StateFlow<Boolean> = authRepository.sessionFlow
        .map { it.isAdmin }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refresh()
    }

    fun onQueryChange(texto: String) {
        _query.value = texto
    }

    fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            productoRepository.refresh()
                .onSuccess { _error.value = null }
                .onFailure { _error.value = it.message }
            _isRefreshing.value = false
        }
    }

    fun consumirError() {
        _error.value = null
    }
}
