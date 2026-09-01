package cl.ipfsoft.backend.dto.permiso;

import cl.ipfsoft.backend.entity.FormaCompensacion;
import cl.ipfsoft.backend.entity.PermisoTipo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Petición de solicitud de permiso laboral.
 * El trabajador indica el tipo (por horas o por días), el motivo y cómo lo compensará
 * (devolver el tiempo después o que se descuente del sueldo).
 */
public record PermisoLaboralRequest(
        @NotNull(message = "La fecha del permiso es obligatoria") LocalDate date,
        @NotNull(message = "El tipo de permiso es obligatorio") PermisoTipo tipo,
        @DecimalMin(value = "0.01", message = "Las horas deben ser mayores a 0") BigDecimal horas,
        @Min(value = 1, message = "Los días deben ser al menos 1") Integer dias,
        @NotBlank(message = "El motivo es obligatorio") String motivo,
        @NotNull(message = "La forma de compensación es obligatoria") FormaCompensacion formaCompensacion
) {
}
