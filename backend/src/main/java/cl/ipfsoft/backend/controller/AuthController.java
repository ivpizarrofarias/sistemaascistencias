package cl.ipfsoft.backend.controller;

import cl.ipfsoft.backend.dto.auth.ActualizarPerfilRequest;
import cl.ipfsoft.backend.dto.auth.AuthResponse;
import cl.ipfsoft.backend.dto.auth.AvatarResponse;
import cl.ipfsoft.backend.dto.auth.CambiarPasswordRequest;
import cl.ipfsoft.backend.dto.auth.LoginRequest;
import cl.ipfsoft.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de autenticación. El login es público (no requiere token).
 * Devuelve un token JWT que el frontend usará en las peticiones autenticadas.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Inicia sesión con usuario y contraseña. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     * Requiere sesión activa. Si la contraseña actual es incorrecta, devuelve 401.
     */
    @PostMapping("/cambiar-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cambiarPassword(Authentication authentication,
                                                @Valid @RequestBody CambiarPasswordRequest request) {
        authService.cambiarPassword(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * Actualiza el perfil (usuario, nombre completo y foto) del usuario autenticado.
     * Los cambios se guardan en la base de datos y se devuelve un nuevo token.
     */
    @PutMapping("/perfil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> actualizarPerfil(Authentication authentication,
                                                         @Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(authService.actualizarPerfil(authentication.getName(), request));
    }

    /**
     * Obtiene la foto de perfil de un usuario para mostrarla en la pantalla de login.
     * Es público: no requiere token.
     */
    @GetMapping("/avatar")
    public ResponseEntity<AvatarResponse> obtenerAvatar(@RequestParam String username) {
        return ResponseEntity.ok(authService.obtenerAvatarPorUsername(username));
    }
}
