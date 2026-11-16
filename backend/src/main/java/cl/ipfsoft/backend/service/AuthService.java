package cl.ipfsoft.backend.service;

import cl.ipfsoft.backend.dto.auth.ActualizarPerfilRequest;
import cl.ipfsoft.backend.dto.auth.AuthResponse;
import cl.ipfsoft.backend.dto.auth.AvatarResponse;
import cl.ipfsoft.backend.dto.auth.CambiarPasswordRequest;
import cl.ipfsoft.backend.dto.auth.LoginRequest;

/**
 * Servicio de autenticación de la aplicación.
 */
public interface AuthService {

    /**
     * Autentica un usuario con su nombre de usuario y contraseña.
     * Si las credenciales son correctas devuelve un token JWT y los datos del usuario.
     *
     * @param request datos de login (username y password)
     * @return responde con el token y los datos del usuario autenticado
     */
    AuthResponse login(LoginRequest request);

    /**
     * Cambia la contraseña del usuario autenticado.
     *
     * @param username   usuario autenticado (se obtiene del token JWT).
     * @param request    contraseña actual y nueva contraseña.
     */
    void cambiarPassword(String username, CambiarPasswordRequest request);

    /**
     * Actualiza el perfil (usuario, nombre y foto) del usuario autenticado.
     * Devuelve los datos actualizados y un nuevo token si el username cambió.
     */
    AuthResponse actualizarPerfil(String usernameActual, ActualizarPerfilRequest request);

    /**
     * Obtiene la foto de perfil de un usuario por su nombre de usuario.
     * Usado por la pantalla de login; no requiere autenticación.
     */
    AvatarResponse obtenerAvatarPorUsername(String username);
}
