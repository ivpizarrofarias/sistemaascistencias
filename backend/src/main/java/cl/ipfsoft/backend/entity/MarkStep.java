package cl.ipfsoft.backend.entity;

/**
 * Pasos de la secuencia de marcaje de un día. Se respetan estrictamente en orden.
 * ENTRADA -> COLACION_SALIDA -> COLACION_ENTRADA -> SALIDA (una única colación).
 */
public enum MarkStep {
    ENTRADA,
    COLACION_SALIDA,
    COLACION_ENTRADA,
    SALIDA,
    COMPLETE
}
