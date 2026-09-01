package cl.ipfsoft.backend.dto.permiso;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

/**
 * Petición para registrar la DEVOLUCIÓN (compensación) de horas o días
 * que el trabajador pidió por permiso con forma DEVOLVER.
 * Solo se aplica cuando el permiso está APROBADO.
 */
public record DevolucionRequest(
        @DecimalMin(value = "0.01", message = "Las horas devueltas deben ser mayores a 0")
        BigDecimal horasDevueltas,
        @Min(value = 1, message = "Los días devueltos deben ser al menos 1")
        Integer diasDevueltos
) {
}
