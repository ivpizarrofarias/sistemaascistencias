package cl.ipfsoft.backend.dto.payroll;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LicenciaRequest(
        @NotNull(message = "La fecha de inicio es obligatoria") LocalDate startDate,
        @NotNull(message = "La fecha de término es obligatoria") LocalDate endDate,
        @NotBlank(message = "El motivo es obligatorio") String motivo,
        boolean employerPays
) {
}
