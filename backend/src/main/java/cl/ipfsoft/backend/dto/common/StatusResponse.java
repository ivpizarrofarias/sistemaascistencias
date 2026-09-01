package cl.ipfsoft.backend.dto.common;

/**
 * Respuesta de estado simple (por ejemplo: "created", "ok", "error").
 */
public record StatusResponse(String status, String message, Integer count) {
    public static StatusResponse ok(String message) {
        return new StatusResponse("ok", message, null);
    }
    
    public static StatusResponse ok(String message, int count) {
        return new StatusResponse("ok", message, count);
    }
}
