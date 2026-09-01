package cl.ipfsoft.backend.dto.attendance;

import jakarta.validation.constraints.NotBlank;

/**
 * Petición de marcaje desde el marcador (móvil / pantalla).
 * Solo se envía el CÓDIGO de marcaje del empleado (generado automáticamente,
 * sin usuario ni contraseña); el backend determina automáticamente el
 * siguiente paso de la secuencia.
 */
public record MarkRequest(
        @NotBlank(message = "El código del empleado es obligatorio") String code
) {
}
