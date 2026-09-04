package cl.ipfsoft.backend.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipfsoft.backend.app.data.ApiService
import cl.ipfsoft.backend.app.data.AuthResponse
import cl.ipfsoft.backend.app.data.LoginRequest
import cl.ipfsoft.backend.app.data.MarkRequest
import cl.ipfsoft.backend.app.data.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    /** Intenta restaurar la sesión guardada localmente al iniciar la app. */
    fun restoreSession() {
        viewModelScope.launch {
            val token = tokenStore.token.first()
            if (token != null) {
                _state.value = _state.value.copy(
                    token = token,
                    isAuthenticated = true,
                )
                val auth = AuthResponse(token, null, null, null, null)
                loadEmployeeData(auth)
            }
        }
    }

    /** Inicia sesión con usuario y contraseña. */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val auth = api.login(LoginRequest(username, password))
                tokenStore.saveSession(auth)
                _state.value = _state.value.copy(
                    token = auth.token,
                    username = auth.username,
                    role = auth.role,
                    employeeId = auth.employeeId,
                    fullName = auth.fullName,
                    isAuthenticated = true,
                    isLoading = false,
                    error = null,
                )
                loadEmployeeData(auth)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = mensajeError(e))
            }
        }
    }

    /** Cierra la sesión y borra el token local. */
    fun logout() {
        viewModelScope.launch {
            tokenStore.clear()
            _state.value = UiState()
        }
    }

    /** Realiza el marcaje del trabajador por su código (RUT). */
    fun mark(code: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, markResult = null)
            try {
                val res = api.mark(MarkRequest(code))
                _state.value = _state.value.copy(markResult = res, isLoading = false, error = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = mensajeError(e))
            }
        }
    }

    /** Consulta el estado actual de marcaje (sin registrar). */
    fun status(code: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val res = api.status(MarkRequest(code))
                _state.value = _state.value.copy(markResult = res, isLoading = false, error = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = mensajeError(e))
            }
        }
    }

    /** Carga asistencias, permisos y liquidaciones del empleado autenticado. */
    private suspend fun loadEmployeeData(auth: AuthResponse) {
        val empId = auth.employeeId ?: _state.value.employeeId ?: return
        try {
            val attendances = api.listAttendances(empId, null, null)
            val permisos = api.listPermisos(empId, null, null)
            val liquidaciones = api.listLiquidaciones(empId)
            _state.value = _state.value.copy(
                employeeId = empId,
                attendances = attendances,
                permisos = permisos,
                liquidaciones = liquidaciones,
                error = null,
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = mensajeError(e))
        }
    }

    private fun mensajeError(e: Exception): String =
        when (e) {
            is retrofit2.HttpException -> "Error del servidor (${e.code()})"
            is java.net.UnknownHostException -> "No se pudo conectar al servidor"
            is java.net.ConnectException -> "No se pudo conectar al servidor"
            else -> e.message ?: "Error desconocido"
        }
}
