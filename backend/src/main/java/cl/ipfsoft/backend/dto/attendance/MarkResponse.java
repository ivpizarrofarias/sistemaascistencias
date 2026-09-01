package cl.ipfsoft.backend.dto.attendance;

import cl.ipfsoft.backend.entity.MarkStep;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Estado actual del marcaje del empleado en una fecha dada.
 * Se usa tanto para responder a un marcaje como para consultar el estado.
 */
public record MarkResponse(
        Long employeeId,
        String fullName,
        LocalDate date,
        MarkStep nextStep,
        LocalTime entryTime,
        LocalTime colacionSalida1,
        LocalTime colacionEntrada1,
        LocalTime colacionSalida2,
        LocalTime colacionEntrada2,
        LocalTime exitTime,
        int colacionSalidaCount,
        int colacionEntradaCount,
        boolean complete,
        boolean blocked,
        String message
) {
}
