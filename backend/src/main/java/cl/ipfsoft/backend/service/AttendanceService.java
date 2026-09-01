package cl.ipfsoft.backend.service;

import cl.ipfsoft.backend.dto.attendance.AttendanceResponse;
import cl.ipfsoft.backend.dto.attendance.MarkRequest;
import cl.ipfsoft.backend.dto.attendance.MarkResponse;
import cl.ipfsoft.backend.dto.attendance.AttendanceUpdateRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de asistencia.
 * Incluye el marcaje self-service (kiosco/móvil) y la gestión de asistencia por el administrador.
 */
public interface AttendanceService {

    /**
     * Marca el siguiente paso de la secuencia para un empleado indicado por su código (RUT).
     * Respeta el orden estricto: ENTRADA -> SALIDA COLACIÓN -> ENTRADA COLACIÓN -> SALIDA.
     * Si hay registros incompletos de días anteriores, el empleado queda BLOQUEADO
     * (no puede marcar ese día ni el siguiente) hasta que un administrador corrija.
     *
     * @param request contiene el código (RUT) del empleado
     * @return estado actualizado de la asistencia del día
     */
    MarkResponse mark(MarkRequest request);

    /**
     * Consulta el estado actual de marcaje de un empleado (por su código RUT) para hoy.
     * No registra ninguna marca, solo informa.
     */
    MarkResponse status(MarkRequest request);

    /** Lista las asistencias de un empleado en un rango de fechas (admin). */
    List<AttendanceResponse> listByEmployee(Long employeeId, LocalDate from, LocalDate to);

    /**
     * Modifica una asistencia (admin). Permite corregir tiempos o marcar un día como completo,
     * lo cual desbloquea al empleado para seguir marcando.
     */
    AttendanceResponse updateAttendance(Long attendanceId, AttendanceUpdateRequest request);

    /**
     * Marca un día como ausencia no justificada (admin) -> descontará del sueldo.
     */
    AttendanceResponse markAbsence(Long employeeId, LocalDate date, boolean absence);
}
