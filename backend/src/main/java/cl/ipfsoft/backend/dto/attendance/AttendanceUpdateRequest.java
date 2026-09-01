package cl.ipfsoft.backend.dto.attendance;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Petición de un administrador para corregir/completar una asistencia.
 * Permite fijar manualmente los tiempos de marcaje, marcar ausencia o licencia,
 * o marcar el día como completo (desbloqueando al empleado).
 * Los campos null no se modifican.
 */
public record AttendanceUpdateRequest(
        LocalDate date,
        LocalTime entryTime,
        LocalTime colacionSalida1,
        LocalTime colacionEntrada1,
        LocalTime colacionSalida2,
        LocalTime colacionEntrada2,
        LocalTime exitTime,
        Boolean complete,
        Boolean absence,
        Boolean medicalLicense
) {
}
