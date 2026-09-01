package cl.ipfsoft.backend.controller;

import cl.ipfsoft.backend.dto.auth.AuthResponse;
import cl.ipfsoft.backend.dto.auth.LoginRequest;
import cl.ipfsoft.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
