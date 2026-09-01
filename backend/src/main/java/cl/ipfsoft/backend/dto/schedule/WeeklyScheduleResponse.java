package cl.ipfsoft.backend.dto.schedule;

import java.time.LocalDate;
import java.util.List;

/**
 * Horario de una semana de un empleado para mostrar.
 */
public record WeeklyScheduleResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate startDate,
        LocalDate endDate,
        String label,
        List<ScheduleDayResponse> days
) {
}
