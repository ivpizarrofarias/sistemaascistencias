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
 * TODAS las operaciones de este controlador requieren rol ADMINISTRADOR
 * (solo el admin puede ingresar, modificar o deshabilitar empleados).
 */
@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /** Lista todos los empleados (admin). */
    @GetMapping
    public List<EmployeeResponse> findAll() {
        return employeeService.findAll();
    }

    /** Busca un empleado por id (admin). */
    @GetMapping("/{id}")
    public EmployeeResponse findById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    /** Crea un nuevo empleado (admin). Valida el RUT y crea el usuario de acceso. */
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    /** Actualiza un empleado (admin). */
    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return employeeService.update(id, request);
    }

    /** Habilita o deshabilita a un empleado (admin). */
    @PutMapping("/{id}/enabled")
    public StatusResponse setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        return employeeService.setEnabled(id, enabled);
    }

    /** Elimina un empleado (admin). */
    @DeleteMapping("/{id}")
    public StatusResponse delete(@PathVariable Long id) {
        return employeeService.delete(id);
    }
}
