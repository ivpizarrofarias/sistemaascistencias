package cl.ipfsoft.backend.dto.schedule;

import java.time.LocalTime;

/**
 * Un día del horario semanal enviado desde el frontend.
 * dayNumber: 1=lunes ... 6=sábado.
 */
public record ScheduleDayRequest(
        int dayNumber,
        boolean worked,
        LocalTime startTime,
        LocalTime endTime
) {
}
