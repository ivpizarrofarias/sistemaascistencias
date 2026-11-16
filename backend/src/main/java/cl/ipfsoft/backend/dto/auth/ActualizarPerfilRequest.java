package cl.ipfsoft.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición para actualizar el perfil del usuario autenticado.
 */
public record ActualizarPerfilRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 60, message = "El usuario no puede superar 60 caracteres")
        String username,
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre completo no puede superar 120 caracteres")
        String fullName,
        /** Foto de perfil en base64. Null para eliminar la foto. */
        String fotoBase64
) {
}
