package cl.ipfsoft.backend.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipfsoft.backend.app.data.ApiService
import cl.ipfsoft.backend.app.data.AuthResponse
import cl.ipfsoft.backend.app.data.AttendanceResponse
import cl.ipfsoft.backend.app.data.LiquidacionResponse
import cl.ipfsoft.backend.app.data.LoginRequest
import cl.ipfsoft.backend.app.data.MarkRequest
import cl.ipfsoft.backend.app.data.MarkResponse
import cl.ipfsoft.backend.app.data.PermisoLaboralResponse
import cl.ipfsoft.backend.app.data.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Estado de la interfaz de la app.
 */
data class UiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    // Sesión
    val isAuthenticated: Boolean = false,
    val username: String? = null,
    val role: String? = null,
    val employeeId: Long? = null,
    val fullName: String? = null,

    // Marcaje
    val markResult: MarkResponse? = null,

    // Datos del empleado
    val attendances: List<AttendanceResponse> = emptyList(),
    val permisos: List<PermisoLaboralResponse> = emptyList(),
    val liquidaciones: List<LiquidacionResponse> = emptyList(),
)

/**
 * ViewModel principal que orquesta el marcaje público y la sesión del empleado.
 */
class MainViewModel(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Restaura la sesión guardada (si existe) al iniciar la app. */
    fun restoreSession() {
        viewModelScope.launch {
            val auth = tokenStore.session.firstOrNull() ?: return@launch
            _state.value = _state.value.copy(
                isAuthenticated = true,
                username = auth.username,
                role = auth.role,
                employeeId = auth.employeeId,
                fullName = auth.fullName,
            )
            auth.employeeId?.let { loadEmployeeData(auth.token, it) }
        }
    }

    /** Inicia sesión con las credenciales y guarda la sesión. */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val auth: AuthResponse = api.login(LoginRequest(username, password))
                tokenStore.saveSession(auth)
                _state.value = _state.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    username = auth.username,
                    role = auth.role,
                    employeeId = auth.employeeId,
                    fullName = auth.fullName,
                )
                auth.employeeId?.let { loadEmployeeData(auth.token, it) }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al iniciar sesión",
                )
            }
        }
    }

    /** Cierra la sesión y borra el token guardado. */
    fun logout() {
        viewModelScope.launch {
            tokenStore.clear()
            _state.value = UiState()
        }
    }

    /** Marca el siguiente paso del día usando el RUT (endpoint público). */
    fun mark(rut: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val result = api.mark(MarkRequest(rut))
                _state.value = _state.value.copy(isLoading = false, markResult = result)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "No se pudo registrar la marca",
                )
            }
        }
    }

    /** Consulta el estado actual del marcaje del día (endpoint público). */
    fun status(rut: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val result = api.status(MarkRequest(rut))
                _state.value = _state.value.copy(isLoading = false, markResult = result)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "No se pudo consultar el estado",
                )
            }
        }
    }

    /** Carga las asistencias, permisos y liquidaciones del empleado autenticado. */
    fun loadEmployeeData(token: String, employeeId: Long) {
        viewModelScope.launch {
            try {
                val attendances = api.myAttendance("Bearer $token", employeeId)
                val permisos = api.myPermisos("Bearer $token", employeeId)
                val liquidaciones = api.myLiquidaciones("Bearer $token", employeeId)
                _state.value = _state.value.copy(
                    attendances = attendances,
                    permisos = permisos,
                    liquidaciones = liquidaciones,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "No se pudieron cargar sus datos")
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
