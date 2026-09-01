package cl.ipfsoft.backend.dto.payroll;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HoraExtraRequest(
        @NotNull(message = "La fecha de las horas extra es obligatoria") LocalDate date,
        @NotNull(message = "La cantidad de horas es obligatoria")
        @DecimalMin(value = "0.01", message = "Las horas extra deben ser mayores a 0") BigDecimal horas
) {
}