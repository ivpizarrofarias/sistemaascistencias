package cl.ipfsoft.backend.dto.schedule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Petición para crear o actualizar el horario de UNA semana de un empleado.
 * {@code days} contiene una entrada por cada día de lunes a sábado.
 */
public record WeeklyScheduleRequest(
        @NotNull(message = "El empleado es obligatorio") Long employeeId,
        @NotNull(message = "La fecha de inicio de la semana es obligatoria") LocalDate startDate,
        @NotBlank(message = "La etiqueta es obligatoria") String label,
        @Valid @NotNull(message = "Debe indicar los días de la semana") List<ScheduleDayRequest> days
) {
}
