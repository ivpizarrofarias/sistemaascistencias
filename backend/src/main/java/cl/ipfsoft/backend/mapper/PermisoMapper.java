package cl.ipfsoft.backend.mapper;

import cl.ipfsoft.backend.dto.permiso.PermisoLaboralRequest;
import cl.ipfsoft.backend.dto.permiso.PermisoLaboralResponse;
import cl.ipfsoft.backend.entity.PermisoLaboral;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Conversión entre la entidad PermisoLaboral y sus DTOs.
 */
@Component
public class PermisoMapper {

    /**
     * Convierte una entidad a su respuesta, calculando si el trabajador aún
     * tiene "saldo pendiente" por devolver (cuando es por DEVOLUCIÓN).
     */
    public PermisoLaboralResponse toResponse(PermisoLaboral p) {
        boolean saldoPendiente = isSaldoPendiente(p);
        return new PermisoLaboralResponse(
                p.getId(),
                p.getEmployee().getId(),
                p.getEmployee().getFullName(),
                p.getDate(),
                p.getTipo(),
                p.getHoras(),
                p.getDias(),
                p.getMotivo(),
                p.getEstado(),
                p.getFormaCompensacion(),
                p.getHorasDevueltas(),
                p.getDiasDevueltos(),
                p.getFechaSolicitud(),
                p.getFechaValidacion(),
                p.getValidadoPor(),
                saldoPendiente
        );
    }

    /**
     * Aplica los datos de la solicitud a una entidad nueva (aún pendiente).
     */
    public PermisoLaboral applyRequest(PermisoLaboral p, PermisoLaboralRequest r) {
        p.setDate(r.date());
        p.setTipo(r.tipo());
        p.setHoras(r.horas() != null ? r.horas() : BigDecimal.ZERO);
        p.setDias(r.dias() != null ? r.dias() : 0);
        p.setMotivo(r.motivo());
        p.setFormaCompensacion(r.formaCompensacion());
        return p;
    }

    /**
     * Indica si al trabajador le queda tiempo por devolver.
     * Solo aplica a permisos con compensación DEVOLVER y estado APROBADO.
     */
    public boolean isSaldoPendiente(PermisoLaboral p) {
        boolean aprobado = p.getEstado() != null
                && p.getEstado().name().equals("APROBADO");
        boolean devolver = p.getFormaCompensacion() != null
                && p.getFormaCompensacion().name().equals("DEVOLVER");
        if (!aprobado || !devolver) {
            return false;
        }
        if (p.getTipo().name().equals("POR_HORA")) {
            return p.getHorasDevueltas().compareTo(p.getHoras()) < 0;
        }
        return p.getDiasDevueltos() < p.getDias();
    }
}
