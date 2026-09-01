package cl.ipfsoft.backend.entity;

/**
 * Estado de una solicitud de permiso laboral.
 * PENDIENTE: esperando aprobación del administrador.
 * APROBADO:  permiso aceptado (puede ser con devolución o descuento).
 * RECHAZADO: permiso no aceptado (el trabajador debe cumplir su jornada).
 */
public enum PermisoEstado {
    PENDIENTE, APROBADO, RECHAZADO
}
