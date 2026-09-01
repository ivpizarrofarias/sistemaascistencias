package cl.ipfsoft.backend.service;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.employee.EmployeeRequest;
import cl.ipfsoft.backend.dto.employee.EmployeeResponse;

import java.util.List;

/**
 * Servicio de gestión de empleados.
 * Solo los administradores pueden crear, modificar o deshabilitar empleados.
 */
public interface EmployeeService {

    /** Lista todos los empleados. */
    List<EmployeeResponse> findAll();

    /** Busca un empleado por su id. */
    EmployeeResponse findById(Long id);

    /** Crea un nuevo empleado (con su usuario de acceso) y valida el RUT. */
    EmployeeResponse create(EmployeeRequest request);

    /** Actualiza los datos de un empleado existente. */
    EmployeeResponse update(Long id, EmployeeRequest request);

    /** Habilita o deshabilita a un empleado para que pueda marcar asistencia. */
    StatusResponse setEnabled(Long id, boolean enabled);

    /** Elimina un empleado. */
    StatusResponse delete(Long id);
}
