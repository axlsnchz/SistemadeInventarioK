package mx.edu.utez.sistemadeinventario.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.edu.utez.sistemadeinventario.data.local.UserSession
import mx.edu.utez.sistemadeinventario.data.repository.AuthRepository

class AuthViewModel(
    private val authRepository: AuthRepository,
    val sessionExpired: SharedFlow<Unit>
) : ViewModel() {

    val session: StateFlow<UserSession?> = authRepository.sessionFlow
        .map<UserSession, UserSession?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _loginState = MutableStateFlow<UiState<UserSession>>(UiState.Idle)
    val loginState: StateFlow<UiState<UserSession>> = _loginState.asStateFlow()

    fun login(username: String, password: String) {
        if (_loginState.value is UiState.Loading) return
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            authRepository.login(username, password).fold(
                onSuccess = { _loginState.value = UiState.Success(it) },
                onFailure = { _loginState.value = UiState.Error(it.message ?: "No se pudo iniciar sesion") }
            )
        }
    }

    fun resetLoginState() {
        _loginState.value = UiState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = UiState.Idle
        }
    }
}
