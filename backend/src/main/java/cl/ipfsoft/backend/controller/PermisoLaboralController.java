package cl.ipfsoft.backend.controller;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.permiso.DevolucionRequest;
import cl.ipfsoft.backend.dto.permiso.PermisoLaboralRequest;
import cl.ipfsoft.backend.dto.permiso.PermisoLaboralResponse;
import cl.ipfsoft.backend.dto.permiso.ValidacionRequest;
import cl.ipfsoft.backend.entity.Rol;
import cl.ipfsoft.backend.entity.Usuario;
import cl.ipfsoft.backend.exception.ForbiddenException;
import cl.ipfsoft.backend.repository.AppUserRepository;
import cl.ipfsoft.backend.repository.PermisoLaboralRepository;
import cl.ipfsoft.backend.service.PermisoLaboralService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de permisos laborales.
 * Permite al trabajador solicitar permisos (por horas o días) y devolver/compensar
 * el tiempo, y al administrador aprobar/rechazar las solicitudes.
 */
@RestController
@RequestMapping("/api/permisos")
public class PermisoLaboralController {

    private final PermisoLaboralService permisoService;
    private final AppUserRepository userRepository;
    private final PermisoLaboralRepository permisoRepository;

    public PermisoLaboralController(PermisoLaboralService permisoService,
                                    AppUserRepository userRepository,
                                    PermisoLaboralRepository permisoRepository) {
        this.permisoService = permisoService;
        this.userRepository = userRepository;
        this.permisoRepository = permisoRepository;
    }

    private Usuario usuarioActual(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ForbiddenException("Usuario no encontrado"));
    }

    private void validarAccesoEmpleado(Long employeeId, Authentication authentication) {
        Usuario user = usuarioActual(authentication);
        if (user.getRole() == Rol.ADMIN || user.getRole() == Rol.SUPERVISOR) return;
        if (user.getEmployee() == null || !user.getEmployee().getId().equals(employeeId)) {
            throw new ForbiddenException("No tiene permisos para acceder a la información de este empleado");
        }
    }

    private void validarAccesoPermiso(Long permisoId, Authentication authentication) {
        Usuario user = usuarioActual(authentication);
        if (user.getRole() == Rol.ADMIN || user.getRole() == Rol.SUPERVISOR) return;
        Long ownerId = permisoRepository.findById(permisoId)
                .map(p -> p.getEmployee().getId())
                .orElseThrow(() -> new cl.ipfsoft.backend.exception.ResourceNotFoundException("Permiso no encontrado con id " + permisoId));
        if (user.getEmployee() == null || !user.getEmployee().getId().equals(ownerId)) {
            throw new ForbiddenException("No tiene permisos para acceder a este permiso");
        }
    }

    /** El trabajador solicita un permiso laboral (solo propio, o admin/supervisor). */
    @PostMapping("/employee/{employeeId}")
    public ResponseEntity<PermisoLaboralResponse> request(@PathVariable Long employeeId,
                                                          @Valid @RequestBody PermisoLaboralRequest request,
                                                          Authentication authentication) {
        validarAccesoEmpleado(employeeId, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(permisoService.request(employeeId, request));
    }

    /** El administrador crea un permiso a nombre de un empleado, dejándolo APROBADO. */
    @PostMapping("/admin/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<PermisoLaboralResponse> createAsAdmin(
            @PathVariable Long employeeId,
            @Valid @RequestBody PermisoLaboralRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permisoService.createAsAdmin(employeeId, request));
    }

    /** Lista permisos de un empleado en un rango de fechas (propios o admin/supervisor). */
    @GetMapping("/employee/{employeeId}")
    public List<PermisoLaboralResponse> listByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        validarAccesoEmpleado(employeeId, authentication);
        return permisoService.listByEmployee(employeeId, from, to);
    }

    /** Lista todos los permisos (admin). */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public List<PermisoLaboralResponse> findAll() {
        return permisoService.findAll();
    }

    /** El administrador aprueba o rechaza un permiso. */
    @PutMapping("/{permisoId}/validar")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public PermisoLaboralResponse validate(@PathVariable Long permisoId,
                                           @Valid @RequestBody ValidacionRequest request) {
        return permisoService.validate(permisoId, request);
    }

    /** El trabajador devuelve (compensa) horas o días del permiso (solo propio, o admin/supervisor). */
    @PutMapping("/{permisoId}/devolver")
    public PermisoLaboralResponse registerDevolucion(@PathVariable Long permisoId,
                                                     @Valid @RequestBody DevolucionRequest request,
                                                     Authentication authentication) {
        validarAccesoPermiso(permisoId, authentication);
        return permisoService.registerDevolucion(permisoId, request);
    }

    /** Elimina un permiso (admin). */
    @DeleteMapping("/{permisoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusResponse delete(@PathVariable Long permisoId) {
        return permisoService.delete(permisoId);
    }
}
