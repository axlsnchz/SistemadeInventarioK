package mx.edu.utez.sistemadeinventario.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utez.sistemadeinventario.data.local.entity.ProductoEntity
import mx.edu.utez.sistemadeinventario.data.repository.ProductoRepository

sealed interface ScanState {
    data object Esperando : ScanState
    data object Buscando : ScanState
    data class Encontrado(val producto: ProductoEntity) : ScanState
    data class Error(val mensaje: String) : ScanState
}

class ScannerViewModel(
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ScanState>(ScanState.Esperando)
    val state: StateFlow<ScanState> = _state.asStateFlow()

    fun onNfcLeido(texto: String) {
        val nfcId = texto.trim()
        if (nfcId.isEmpty()) {
            _state.value = ScanState.Error("La etiqueta no tiene un ID de producto")
            return
        }
        if (_state.value is ScanState.Buscando) return
        viewModelScope.launch {
            _state.value = ScanState.Buscando
            productoRepository.buscarPorNfc(nfcId).fold(
                onSuccess = { _state.value = ScanState.Encontrado(it) },
                onFailure = { _state.value = ScanState.Error(it.message ?: "No se pudo buscar el producto") }
            )
        }
    }

    fun reiniciar() {
        _state.value = ScanState.Esperando
    }
}
