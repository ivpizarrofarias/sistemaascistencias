package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.dto.auth.AuthResponse;
import cl.ipfsoft.backend.dto.auth.LoginRequest;
import cl.ipfsoft.backend.entity.Usuario;
import cl.ipfsoft.backend.entity.Rol;
import cl.ipfsoft.backend.repository.AppUserRepository;
import cl.ipfsoft.backend.security.JwtService;
import cl.ipfsoft.backend.service.AuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de autenticación.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(AppUserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Valida las credenciales del usuario y, si son correctas, genera un token JWT.
     * El token incluye el username, el rol y (si corresponde) el id del empleado asociado.
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Buscar el usuario por nombre de usuario.
        Usuario user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

        // 2. Verificar que el usuario esté habilitado.
        if (!user.isEnabled()) {
            throw new BadCredentialsException("Usuario deshabilitado");
        }

        // 3. Verificar la contraseña con el codificador (BCrypt).
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        // 4. Generar el token JWT con los datos del usuario.
        Long employeeId = user.getEmployee() != null ? user.getEmployee().getId() : null;
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name(), employeeId);

        // 5. Preparar el nombre completo (del empleado si existe, si no el username).
        String fullName = user.getEmployee() != null
                ? user.getEmployee().getFullName()
                : user.getUsername();

        return new AuthResponse(token, user.getUsername(), user.getRole(), employeeId, fullName);
    }
}
