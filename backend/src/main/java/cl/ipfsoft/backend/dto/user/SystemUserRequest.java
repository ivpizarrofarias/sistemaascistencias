package cl.ipfsoft.backend.dto.user;

import cl.ipfsoft.backend.entity.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SystemUserRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 60, message = "El usuario no puede superar 60 caracteres")
        String username,

        @Size(max = 120, message = "El nombre completo no puede superar 120 caracteres")
        String fullName,

        /** Contraseña: obligatoria al crear; opcional al editar (si es null/vacía se mantiene). */
        @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol role,

        boolean enabled
) {
}
