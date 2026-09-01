package cl.ipfsoft.backend.exception;

import cl.ipfsoft.backend.dto.common.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones de la API REST.
 * Convierte cualquier excepción en una respuesta JSON uniforme (ApiError),
 * con su mensaje y código HTTP apropiado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Excepciones de negocio personalizadas (BusinessException).
     * Llevan el código HTTP dentro de la propia excepción.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        int status = ex.getStatus();
        return ResponseEntity.status(status).body(ApiError.of(status, ex.getMessage()));
    }

    /**
     * Credenciales incorrectas (usuario o contraseña inválidos en el login).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(401, "Usuario o contraseña incorrectos"));
    }

    /**
     * Usuario no encontrado durante la autenticación.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUsernameNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(401, "Usuario o contraseña incorrectos"));
    }

    /**
     * Errores de validación de campos en los DTO (Bean Validation).
     * Se agrupan todos los errores de campo en un mapa.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(400, "Error de validación", fieldErrors));
    }

    /**
     * Recurso no encontrado (id inexistente) -> 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, ex.getMessage()));
    }

    /**
     * Petición de un usuario autenticado pero sin rol suficiente -> 403.
     * @PreAuthorize lanza una denial que debe traducirse a 403 para no responder 500.
     */
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ApiError> handleAccessDenied(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(403, "No tiene permisos para realizar esta operación"));
    }

    /**
     * Cualquier otra excepción no controlada se responde como error 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(500, "Error interno del servidor: " + ex.getMessage()));
    }
}
