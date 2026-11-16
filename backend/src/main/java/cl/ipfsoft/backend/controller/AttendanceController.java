package cl.ipfsoft.backend.controller;

import cl.ipfsoft.backend.dto.attendance.AttendanceResponse;
import cl.ipfsoft.backend.dto.attendance.AttendanceUpdateRequest;
import cl.ipfsoft.backend.dto.attendance.MarkRequest;
import cl.ipfsoft.backend.dto.attendance.MarkResponse;
import cl.ipfsoft.backend.entity.Usuario;
import cl.ipfsoft.backend.entity.Rol;
import cl.ipfsoft.backend.exception.ForbiddenException;
import cl.ipfsoft.backend.repository.AppUserRepository;
import cl.ipfsoft.backend.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
 * Controlador de asistencia.
 *
 * Los endpoints de marcaje (mark/status) son PÚBLICOS porque corresponden al
 * kiosco/marcador móvil donde el trabajador digita su código sin iniciar sesión.
 * Las operaciones de gestión (modificar asistencias) requieren ADMIN.
 * La consulta de las asistencias de un empleado está disponible para el ADMIN
 * (cualquier empleado) y para cada trabajador sobre sus propios registros.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AppUserRepository userRepository;

    public AttendanceController(AttendanceService attendanceService, AppUserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.userRepository = userRepository;
    }

    /**
     * Marca el siguiente paso de la secuencia del trabajador (entrada, colación o salida).
     * PÚBLICO: solo se requiere el código (RUT).
     */
    @PostMapping("/mark")
    public MarkResponse mark(@Valid @RequestBody MarkRequest request) {
        return attendanceService.mark(request);
    }

    /**
     * Consulta el estado actual de marcaje del trabajador para hoy.
     * PÚBLICO: se usa para que el móvil muestre en qué punto va el día.
     */
    @PostMapping("/status")
    public MarkResponse status(@Valid @RequestBody MarkRequest request) {
        return attendanceService.status(request);
    }

    /** Lista las asistencias de un empleado en un rango. */
    @GetMapping("/employee/{employeeId}")
    public List<AttendanceResponse> listByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        // El administrador puede ver las asistencias de cualquier empleado;
        // un empleado solo puede consultar sus propios registros.
        String username = authentication.getName();
        Usuario user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ForbiddenException("Usuario no encontrado"));
        if (user.getRole() != Rol.ADMIN && user.getRole() != Rol.SUPERVISOR) {
            if (user.getEmployee() == null || !user.getEmployee().getId().equals(employeeId)) {
                throw new ForbiddenException("No tiene permisos para ver las asistencias de este empleado");
            }
        }
        return attendanceService.listByEmployee(employeeId, from, to);
    }

    /** Modifica una asistencia: permite completar/corregir y desbloquear al empleado. */
    @PutMapping("/{attendanceId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public AttendanceResponse updateAttendance(@PathVariable Long attendanceId,
                                               @Valid @RequestBody AttendanceUpdateRequest request) {
        return attendanceService.updateAttendance(attendanceId, request);
    }

    /** Marca una ausencia no justificada de un empleado en una fecha. */
    @PutMapping("/absence/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public AttendanceResponse markAbsence(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "true") boolean absence) {
        return attendanceService.markAbsence(employeeId, date, absence);
    }
}
