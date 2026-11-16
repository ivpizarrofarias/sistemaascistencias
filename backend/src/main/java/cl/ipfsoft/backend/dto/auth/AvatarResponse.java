package cl.ipfsoft.backend.dto.auth;

/**
 * Respuesta con la foto de perfil de un usuario (para la pantalla de login).
 */
public record AvatarResponse(
        String username,
        String fotoBase64
) {
}
