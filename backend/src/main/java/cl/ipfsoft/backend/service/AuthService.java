package cl.ipfsoft.backend.service;

import cl.ipfsoft.backend.dto.auth.AuthResponse;
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
}
