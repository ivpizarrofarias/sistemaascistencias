package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.dto.auth.ActualizarPerfilRequest;
import cl.ipfsoft.backend.dto.auth.AuthResponse;
import cl.ipfsoft.backend.dto.auth.AvatarResponse;
import cl.ipfsoft.backend.dto.auth.CambiarPasswordRequest;
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

        // 5. Nombre completo: prioridad al perfil del usuario, luego empleado, luego username.
        String fullName = resolverFullName(user);

        return new AuthResponse(token, user.getUsername(), user.getRole(), employeeId, fullName, user.getFotoBase64());
    }

    private String resolverFullName(Usuario user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        if (user.getEmployee() != null) {
            return user.getEmployee().getFullName();
        }
        return user.getUsername();
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     * Valida que la contraseña actual coincida antes de guardar la nueva codificada.
     */
    @Override
    public void cambiarPassword(String username, CambiarPasswordRequest request) {
        Usuario user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Usuario deshabilitado");
        }

        // Verificar que la contraseña actual sea correcta.
        if (!passwordEncoder.matches(request.passwordActual(), user.getPassword())) {
            throw new BadCredentialsException("La contraseña actual es incorrecta");
        }

        // Guardar la nueva contraseña codificada.
        user.setPassword(passwordEncoder.encode(request.nuevaPassword()));
        userRepository.save(user);
    }

    @Override
    public AuthResponse actualizarPerfil(String usernameActual, ActualizarPerfilRequest request) {
        Usuario user = userRepository.findByUsername(usernameActual)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Usuario deshabilitado");
        }

        String nuevoUsername = request.username().trim();
        if (!nuevoUsername.equals(user.getUsername())) {
            if (userRepository.existsByUsername(nuevoUsername)) {
                throw new BadCredentialsException("El nombre de usuario ya está en uso");
            }
            user.setUsername(nuevoUsername);
        }

        user.setFullName(request.fullName());
        user.setFotoBase64(request.fotoBase64());
        Usuario saved = userRepository.save(user);

        Long employeeId = saved.getEmployee() != null ? saved.getEmployee().getId() : null;
        String token = jwtService.generateToken(saved.getUsername(), saved.getRole().name(), employeeId);
        String fullName = resolverFullName(saved);
        return new AuthResponse(token, saved.getUsername(), saved.getRole(), employeeId, fullName, saved.getFotoBase64());
    }

    @Override
    public AvatarResponse obtenerAvatarPorUsername(String username) {
        if (username == null || username.isBlank()) {
            return new AvatarResponse(username, null);
        }
        return userRepository.findByUsername(username.trim())
                .filter(Usuario::isEnabled)
                .map(u -> new AvatarResponse(u.getUsername(), u.getFotoBase64()))
                .orElse(new AvatarResponse(username, null));
    }
}
