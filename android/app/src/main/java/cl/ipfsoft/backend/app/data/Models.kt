package cl.ipfsoft.backend.app.data

import com.google.gson.annotations.SerializedName

/**
 * Modelos de datos (DTOs) que reflejan las respuestas JSON de la API REST del
 * backend Spring Boot. Los campos nulos son omitidos por el backend cuando no
 * aplican.
 */

/** Respuesta de inicio de sesión. */
data class AuthResponse(
    val token: String?,
    val username: String?,
    val role: String?,
    @SerializedName("employeeId") val employeeId: Long?,
    @SerializedName("fullName") val fullName: String?,
)

/** Respuesta de marcaje/estado del marcador. */
data class MarkResponse(
    @SerializedName("employeeId") val employeeId: Long?,
    @SerializedName("fullName") val fullName: String?,
    val date: String?,
    @SerializedName("nextStep") val nextStep: String?,
    @SerializedName("entryTime") val entryTime: String?,
    @SerializedName("colacionSalida1") val colacionSalida1: String?,
    @SerializedName("colacionEntrada1") val colacionEntrada1: String?,
    @SerializedName("colacionSalida2") val colacionSalida2: String?,
    @SerializedName("colacionEntrada2") val colacionEntrada2: String?,
    @SerializedName("exitTime") val exitTime: String?,
    @SerializedName("colacionSalidaCount") val colacionSalidaCount: Int = 0,
    @SerializedName("colacionEntradaCount") val colacionEntradaCount: Int = 0,
    val complete: Boolean = false,
    val blocked: Boolean = false,
    val message: String?,
)

/** Registro de asistencia de un empleado. */
data class AttendanceResponse(
    val id: Long,
    @SerializedName("employeeId") val employeeId: Long,
    @SerializedName("employeeName") val employeeName: String?,
    val rut: String?,
    val date: String,
    @SerializedName("entryTime") val entryTime: String?,
    @SerializedName("colacionSalida1") val colacionSalida1: String?,
    @SerializedName("colacionEntrada1") val colacionEntrada1: String?,
    @SerializedName("colacionSalida2") val colacionSalida2: String?,
    @SerializedName("colacionEntrada2") val colacionEntrada2: String?,
    @SerializedName("exitTime") val exitTime: String?,
    val complete: Boolean = false,
    val absence: Boolean = false,
    @SerializedName("medicalLicense") val medicalLicense: Boolean = false,
    @SerializedName("adminCorrected") val adminCorrected: Boolean = false,
)

/** Permiso laboral de un empleado. */
data class PermisoLaboralResponse(
    val id: Long,
    @SerializedName("employeeId") val employeeId: Long,
    @SerializedName("employeeName") val employeeName: String?,
    val date: String?,
    val tipo: String?,
    val horas: Double?,
    val dias: Int = 0,
    val motivo: String?,
    val estado: String?,
    @SerializedName("formaCompensacion") val formaCompensacion: String?,
    @SerializedName("horasDevueltas") val horasDevueltas: Double?,
    @SerializedName("diasDevueltos") val diasDevueltos: Int = 0,
    @SerializedName("fechaSolicitud") val fechaSolicitud: String?,
    @SerializedName("fechaValidacion") val fechaValidacion: String?,
    @SerializedName("validadoPor") val validadoPor: String?,
    @SerializedName("saldoPendiente") val saldoPendiente: Boolean = false,
)

/** Detalle de un concepto de una liquidación (haber o descuento). */
data class LiquidacionDetalleDto(
    val concepto: String?,
    val haber: Boolean = false,
    val monto: Double?,
)

/** Liquidación de sueldo de un empleado. */
data class LiquidacionResponse(
    val id: Long,
    @SerializedName("employeeId") val employeeId: Long,
    @SerializedName("employeeName") val employeeName: String?,
    val rut: String?,
    @SerializedName("periodYear") val periodYear: Int,
    @SerializedName("periodMonth") val periodMonth: Int,
    @SerializedName("diasMes") val diasMes: Int = 0,
    @SerializedName("diasHabiles") val diasHabiles: Int = 0,
    @SerializedName("diasTrabajados") val diasTrabajados: Int = 0,
    @SerializedName("diasInasistencia") val diasInasistencia: Int = 0,
    @SerializedName("diasLicencia") val diasLicencia: Int = 0,
    @SerializedName("sueldoBase") val sueldoBase: Double?,
    @SerializedName("gratificacion") val gratificacion: Double?,
    @SerializedName("asignacionFamiliar") val asignacionFamiliar: Double?,
    @SerializedName("bonoMovilizacion") val bonoMovilizacion: Double?,
    @SerializedName("subsidioLicenciaEmpleador") val subsidioLicenciaEmpleador: Double?,
    @SerializedName("horasExtras") val horasExtras: Double?,
    @SerializedName("montoHorasExtras") val montoHorasExtras: Double?,
    @SerializedName("otrosHaberes") val otrosHaberes: Double?,
    @SerializedName("baseImponible") val baseImponible: Double?,
    @SerializedName("totalHaberes") val totalHaberes: Double?,
    val afp: Double?,
    val sis: Double?,
    val salud: Double?,
    @SerializedName("impuestoUnico") val impuestoUnico: Double?,
    val cesantia: Double?,
    @SerializedName("descuentoFaltas") val descuentoFaltas: Double?,
    @SerializedName("descuentoLicenciaDias") val descuentoLicenciaDias: Int = 0,
    @SerializedName("descuentoLicencia") val descuentoLicencia: Double?,
    val anticipos: Double?,
    @SerializedName("descuentoHorasExtras") val descuentoHorasExtras: Double?,
    @SerializedName("otrosDescuentos") val otrosDescuentos: Double?,
    @SerializedName("totalDescuentos") val totalDescuentos: Double?,
    @SerializedName("liquidoAPagar") val liquidoAPagar: Double?,
    val pagada: Boolean = false,
    val detalles: List<LiquidacionDetalleDto>? = emptyList(),
)
