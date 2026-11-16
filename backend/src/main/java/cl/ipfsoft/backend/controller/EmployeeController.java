package cl.ipfsoft.backend.controller;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.employee.EmployeeRequest;
import cl.ipfsoft.backend.dto.employee.EmployeeResponse;
import cl.ipfsoft.backend.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de empleados.
 * Las operaciones de consulta y edición están disponibles para ADMIN y SUPERVISOR.
 * Habilitar/deshabilitar y eliminar empleados son acciones exclusivas de ADMIN.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /** Lista todos los empleados. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public List<EmployeeResponse> findAll() {
        return employeeService.findAll();
    }

    /** Busca un empleado por id. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public EmployeeResponse findById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    /** Crea un nuevo empleado. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    /** Actualiza un empleado. */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return employeeService.update(id, request);
    }

    /** Habilita o deshabilita a un empleado (solo ADMIN). */
    @PutMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusResponse setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        return employeeService.setEnabled(id, enabled);
    }

    /** Elimina un empleado (solo ADMIN). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusResponse delete(@PathVariable Long id) {
        return employeeService.delete(id);
    }
}
