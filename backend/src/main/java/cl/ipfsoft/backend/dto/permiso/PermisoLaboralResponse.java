package cl.ipfsoft.backend.dto.permiso;

import cl.ipfsoft.backend.entity.FormaCompensacion;
import cl.ipfsoft.backend.entity.PermisoEstado;
import cl.ipfsoft.backend.entity.PermisoTipo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Respuesta con los datos de un permiso laboral.
 */
public record PermisoLaboralResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate date,
        PermisoTipo tipo,
        BigDecimal horas,
        int dias,
        String motivo,
        PermisoEstado estado,
        FormaCompensacion formaCompensacion,
        BigDecimal horasDevueltas,
        int diasDevueltos,
        LocalDate fechaSolicitud,
        LocalDate fechaValidacion,
        String validadoPor,
        boolean saldoPendiente
) {
}
