package cl.ipfsoft.backend.app.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaces Retrofit que mapean los endpoints REST del backend Spring Boot.
 */
interface ApiService {
    // ----- Autenticación -----
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    // ----- Marcaje de asistencia (público) -----
    @POST("api/attendance/mark")
    suspend fun mark(@Body request: MarkRequest): MarkResponse

    @POST("api/attendance/status")
    suspend fun status(@Body request: MarkRequest): MarkResponse

    // ----- Asistencias del empleado -----
    @GET("api/attendance/employee/{employeeId}")
    suspend fun listAttendances(
        @Path("employeeId") employeeId: Long,
        @Query("from") from: String?,
        @Query("to") to: String?,
    ): List<AttendanceResponse>

    // ----- Permisos laborales -----
    @GET("api/permisos/employee/{employeeId}")
    suspend fun listPermisos(
        @Path("employeeId") employeeId: Long,
        @Query("from") from: String?,
        @Query("to") to: String?,
    ): List<PermisoLaboralResponse>

    // ----- Liquidaciones -----
    @GET("api/payroll/liquidaciones/employee/{employeeId}")
    suspend fun listLiquidaciones(@Path("employeeId") employeeId: Long): List<LiquidacionResponse>
}

/** Cuerpo de la petición de login. */
data class LoginRequest(
    val username: String,
    val password: String,
)

/** Cuerpo de la petición de marcaje: solo el código del trabajador. */
data class MarkRequest(
    val code: String,
)
