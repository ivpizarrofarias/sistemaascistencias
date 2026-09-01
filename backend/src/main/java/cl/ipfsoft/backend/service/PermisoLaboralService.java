package cl.ipfsoft.backend.service;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.permiso.DevolucionRequest;
import cl.ipfsoft.backend.dto.permiso.PermisoLaboralRequest;
import cl.ipfsoft.backend.dto.permiso.PermisoLaboralResponse;
import cl.ipfsoft.backend.dto.permiso.ValidacionRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de permisos laborales.
 * Permite:
 *  - Al trabajador solicitar permisos por horas o por días, indicando si los
 *    DEVUELVE (compensa después) o si se le DESCUENTAN del sueldo.
 *  - Al trabajador registrar la devolución (compensación) del tiempo pedido.
 *  - Al administrador aprobar/rechazar las solicitudes.
 */
public interface PermisoLaboralService {

    /** El trabajador solicita un permiso. */
    PermisoLaboralResponse request(Long employeeId, PermisoLaboralRequest request);

    /** El administrador crea un permiso a nombre de un empleado, dejándolo APROBADO. */
    PermisoLaboralResponse createAsAdmin(Long employeeId, PermisoLaboralRequest request);

    /** Lista permisos de un empleado en un rango de fechas. */
    List<PermisoLaboralResponse> listByEmployee(Long employeeId, LocalDate from, LocalDate to);

    /** Lista todos los permisos (para el administrador). */
    List<PermisoLaboralResponse> findAll();

    /** El administrador aprueba o rechaza un permiso. */
    PermisoLaboralResponse validate(Long permisoId, ValidacionRequest request);

    /** El trabajador registra la devolución/compensación de horas o días del permiso. */
    PermisoLaboralResponse registerDevolucion(Long permisoId, DevolucionRequest request);

    /** Elimina un permiso. */
    StatusResponse delete(Long permisoId);
}
