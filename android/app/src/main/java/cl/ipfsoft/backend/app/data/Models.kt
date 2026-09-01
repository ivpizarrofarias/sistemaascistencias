package cl.ipfsoft.backend.app.data

import com.google.gson.annotations.SerializedName

/**
 * Modelos de datos de la app Android.
 * Reflejan los DTO (records) del backend Spring Boot para mantener los contratos
 * de la API idénticos entre el backend y el móvil.
 */

// ============================================================================
// AUTH
// ============================================================================

data class LoginRequest(val username: String, val password: String)

data class AuthResponse(
    val token: String,
    val username: String,
    val role: String,
    val employeeId: Long?,
    val fullName: String?
)

// ============================================================================
// ATTENDANCE (marcaje público)
// ============================================================================

data class MarkRequest(val code: String)

data class MarkResponse(
    val employeeId: Long?,
    val fullName: String?,
    val date: String?,
    val nextStep: String?,
    val entryTime: String?,
    val colacionSalida1: String?,
    val colacionEntrada1: String?,
    val colacionSalida2: String?,
    val colacionEntrada2: String?,
    val exitTime: String?,
    val colacionSalidaCount: Int = 0,
    val colacionEntradaCount: Int = 0,
    val complete: Boolean = false,
    val blocked: Boolean = false,
    val message: String?
)

data class AttendanceResponse(
    val id: Long,
    val employeeId: Long,
    val employeeName: String,
    val rut: String,
    val date: String,
    val entryTime: String?,
    val colacionSalida1: String?,
    val colacionEntrada1: String?,
    val colacionSalida2: String?,
    val colacionEntrada2: String?,
    val exitTime: String?,
    val complete: Boolean = false,
    val absence: Boolean = false,
    val medicalLicense: Boolean = false,
    val adminCorrected: Boolean = false
)

// ============================================================================
// EMPLOYEE
// ============================================================================

// Un día del horario semanal (1=lunes ... 6=sábado).
data class ScheduleDayResponse(
    val id: Long? = null,
    val dayNumber: Int,
    val worked: Boolean = false,
    val startTime: String? = null, // HH:mm
    val endTime: String? = null    // HH:mm
)

// Horario de UNA semana de un empleado (la semana inicia en un lunes).
// El horario es rotativo: se define semana a semana, no en el formulario del empleado.
data class WeeklyScheduleResponse(
    val id: Long? = null,
    val employeeId: Long,
    val employeeName: String? = null,
    val startDate: String, // yyyy-MM-dd (lunes)
    val endDate: String,   // yyyy-MM-dd (sábado)
    val label: String? = null,
    val days: List<ScheduleDayResponse> = emptyList()
)

data class EmployeeResponse(
    val id: Long,
    val rut: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val phone: String?,
    val email: String?,
    val hireDate: String,
    val baseSalary: Double,
    val dependents: Int = 0,
    val pensionSystem: String?,
    val afpFund: String?,
    val healthSystem: String?,
    val enabled: Boolean = true,
    val asignacionFamiliar: Boolean = false,
    val code: String? = null
)

// ============================================================================
// PERMISO LABORAL
// ============================================================================

data class PermisoLaboralResponse(
    val id: Long,
    val employeeId: Long,
    val employeeName: String,
    val date: String,
    val tipo: String,
    val horas: Double?,
    val dias: Int = 0,
    val motivo: String,
    val estado: String,
    val formaCompensacion: String,
    val horasDevueltas: Double?,
    val diasDevueltos: Int = 0,
    val fechaSolicitud: String?,
    val fechaValidacion: String?,
    val validadoPor: String?,
    val saldoPendiente: Boolean = false
)

// ============================================================================
// PAYROLL (liquidaciones)
// ============================================================================

data class LiquidacionResponse(
    val id: Long,
    val employeeId: Long,
    val employeeName: String,
    val rut: String,
    val periodYear: Int,
    val periodMonth: Int,
    val diasMes: Int = 0,
    val diasTrabajados: Int = 0,
    val diasInasistencia: Int = 0,
    val diasLicencia: Int = 0,
    val sueldoBase: Double,
    val gratificacion: Double = 0.0,
    val asignacionFamiliar: Double = 0.0,
    val subsidioLicenciaEmpleador: Double = 0.0,
    val otrosHaberes: Double = 0.0,
    val totalHaberes: Double,
    val afp: Double = 0.0,
    val sis: Double = 0.0,
    val salud: Double = 0.0,
    val impuestoUnico: Double = 0.0,
    val cesantia: Double = 0.0,
    val descuentoFaltas: Double = 0.0,
    val anticipos: Double = 0.0,
    val otrosDescuentos: Double = 0.0,
    val totalDescuentos: Double,
    val liquidoAPagar: Double,
    val pagada: Boolean = false,
    val detalles: List<LiquidacionDetalleDto> = emptyList()
)

data class LiquidacionDetalleDto(
    val concepto: String,
    val haber: Boolean = true,
    val monto: Double
)
