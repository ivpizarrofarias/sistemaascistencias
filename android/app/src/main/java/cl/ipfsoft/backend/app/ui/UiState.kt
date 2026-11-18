package cl.ipfsoft.backend.app.ui

import cl.ipfsoft.backend.app.data.AttendanceResponse
import cl.ipfsoft.backend.app.data.BackendEnvironment
import cl.ipfsoft.backend.app.data.EmployeeResponse
import cl.ipfsoft.backend.app.data.LiquidacionResponse
import cl.ipfsoft.backend.app.data.MarkResponse
import cl.ipfsoft.backend.app.data.PermisoLaboralResponse

/** Estado de UI compartido por las pantallas de la app. */
data class UiState(
    /** Backend al que apunta la app (local o producción). */
    val environment: BackendEnvironment = BackendEnvironment.LOCAL,
    /** true muestra el portal del empleado; false muestra el kiosco de marcaje. */
    val showLogin: Boolean = false,
    val isAuthenticated: Boolean = false,
    val token: String? = null,
    val username: String? = null,
    val role: String? = null,
    val employeeId: Long? = null,
    val fullName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val markResult: MarkResponse? = null,
    val attendances: List<AttendanceResponse> = emptyList(),
    val permisos: List<PermisoLaboralResponse> = emptyList(),
    val liquidaciones: List<LiquidacionResponse> = emptyList(),
    /** Empleados disponibles para admin/supervisor sin empleado propio. */
    val employees: List<EmployeeResponse> = emptyList(),
)
