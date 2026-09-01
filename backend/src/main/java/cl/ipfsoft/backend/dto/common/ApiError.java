package cl.ipfsoft.backend.dto.common;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Respuesta de error uniforme de la API.
 */
public record ApiError(
        String message,
        int status,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors
) {
    public static ApiError of(int status, String message) {
        return new ApiError(message, status, LocalDateTime.now(), Map.of());
    }

    public static ApiError of(int status, String message, Map<String, String> fieldErrors) {
        return new ApiError(message, status, LocalDateTime.now(), fieldErrors);
    }
}
