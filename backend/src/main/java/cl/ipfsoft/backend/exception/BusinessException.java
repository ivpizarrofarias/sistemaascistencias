package cl.ipfsoft.backend.exception;

/**
 * Excepción de negocio con su código HTTP asociado.
 */
public class BusinessException extends RuntimeException {

    private final int status;

    public BusinessException(String message) {
        this(message, 400);
    }

    public BusinessException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
