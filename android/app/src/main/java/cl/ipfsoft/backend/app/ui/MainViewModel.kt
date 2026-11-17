package cl.ipfsoft.backend.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipfsoft.backend.app.data.ApiService
import cl.ipfsoft.backend.app.data.LoginRequest
import cl.ipfsoft.backend.app.data.MarkRequest
import cl.ipfsoft.backend.app.data.TokenHolder
import cl.ipfsoft.backend.app.data.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
            val session = tokenStore.loadSession() ?: return@launch
            // El interceptor necesita el token en memoria antes de cualquier llamada.
            TokenHolder.set(session.token)
            _state.value = _state.value.copy(
                token = session.token,
                username = session.username,
                role = session.role,
                employeeId = session.employeeId,
                fullName = session.fullName,
                isAuthenticated = true,
            )
            loadEmployeeData()
        }
    }

    /** Muestra el portal del empleado (pantalla de login). */
    fun openLogin() {
        _state.value = _state.value.copy(showLogin = true, error = null)
    }

    /** Vuelve al kiosco de marcaje. */
    fun openKiosk() {
        _state.value = _state.value.copy(showLogin = false, error = null)
    }

    /** Inicia sesión con usuario y contraseña. */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val auth = api.login(LoginRequest(username, password))
                tokenStore.saveSession(auth)
                TokenHolder.set(auth.token)
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
                loadEmployeeData()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = mensajeError(e))
            }
        }
    }

    /** Cierra la sesión y borra el token local. */
    fun logout() {
        viewModelScope.launch {
            tokenStore.clear()
            TokenHolder.clear()
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

    /** Carga asistencias, permisos y liquidaciones del empleado indicado. */
    private suspend fun loadEmployeeData(empId: Long? = _state.value.employeeId) {
        if (empId == null) {
            // ADMIN/SUPERVISOR sin empleado propio: listar empleados y elegir el primero.
            loadEmployees()
            return
        }
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

    /** Obtiene el listado de empleados del servidor (solo ADMIN/SUPERVISOR). */
    private suspend fun loadEmployees() {
        try {
            val lista = api.listEmployees()
            _state.value = _state.value.copy(employees = lista)
            lista.firstOrNull()?.let { loadEmployeeData(it.id) }
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = mensajeError(e))
        }
    }

    /** Cambia el empleado cuyos datos se muestran en el portal. */
    fun selectEmployee(id: Long) {
        viewModelScope.launch { loadEmployeeData(id) }
    }

    private fun mensajeError(e: Exception): String =
        when (e) {
            is retrofit2.HttpException -> mensajeDelServidor(e) ?: "Error del servidor (${e.code()})"
            is java.net.UnknownHostException -> "No se pudo conectar al servidor"
            is java.net.ConnectException -> "No se pudo conectar al servidor"
            else -> e.message ?: "Error desconocido"
        }

    /** Extrae el campo "message" del body de error del backend (si viene). */
    private fun mensajeDelServidor(e: retrofit2.HttpException): String? {
        val raw = runCatching { e.response()?.errorBody()?.string() }.getOrNull() ?: return null
        return runCatching {
            org.json.JSONObject(raw).optString("message").takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
