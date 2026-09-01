package cl.ipfsoft.backend.dto.permiso;

import cl.ipfsoft.backend.entity.PermisoEstado;
import jakarta.validation.constraints.NotNull;

/**
 * Petición del administrador para aprobar o rechazar un permiso laboral.
 */
public record ValidacionRequest(
        @NotNull(message = "El estado es obligatorio (APROBADO o RECHAZADO)") PermisoEstado estado,
        String observacion
) {
}
