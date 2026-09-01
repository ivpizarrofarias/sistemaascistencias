package cl.ipfsoft.backend.app.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Definición de los endpoints de la API que consume la app móvil.
 *
 * La app se centra en el trabajo principal del trabajador:
 *  - Marcaje público por RUT (kiosco).
 *  - Consulta del estado del día.
 *  - Login del empleado.
 *  - Mis asistencias, mis permisos y mis liquidaciones (con token).
 */

interface ApiService {

    // ---- Endpoints PÚBLICOS (marcaje por RUT) ----

    @POST("api/attendance/mark")
    suspend fun mark(@Body body: MarkRequest): MarkResponse

    @POST("api/attendance/status")
    suspend fun status(@Body body: MarkRequest): MarkResponse

    // ---- Endpoints AUTENTICADOS (token JWT) ----

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/attendance/employee/{employeeId}")
    suspend fun myAttendance(
        @Header("Authorization") token: String,
        @Path("employeeId") employeeId: Long
    ): List<AttendanceResponse>

    @GET("api/permisos/employee/{employeeId}")
    suspend fun myPermisos(
        @Header("Authorization") token: String,
        @Path("employeeId") employeeId: Long
    ): List<PermisoLaboralResponse>

    @GET("api/payroll/liquidaciones/employee/{employeeId}")
    suspend fun myLiquidaciones(
        @Header("Authorization") token: String,
        @Path("employeeId") employeeId: Long
    ): List<LiquidacionResponse>
}
