package cl.ipfsoft.backend.dto.schedule;

import java.time.LocalTime;

/**
 * Un día del horario semanal para mostrar en el frontend.
 */
public record ScheduleDayResponse(
        Long id,
        int dayNumber,
        boolean worked,
        LocalTime startTime,
        LocalTime endTime
) {
}
