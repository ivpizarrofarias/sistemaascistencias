package cl.ipfsoft.backend.entity;

/**
 * Forma de compensación de un permiso laboral.
 * DEVOLVER: el trabajador repone después las horas/días pedidos (no se descuentan
 *           mientras se repongan; si no los repone antes de la liquidación, se descuentan).
 * DESCUENTO: directamente se descuentan del sueldo las horas/días del permiso.
 */
public enum FormaCompensacion {
    DEVOLVER, DESCUENTO
}
