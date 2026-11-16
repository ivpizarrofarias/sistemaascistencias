package cl.ipfsoft.backend.controller;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.schedule.WeeklyScheduleRequest;
import cl.ipfsoft.backend.dto.schedule.WeeklyScheduleResponse;
import cl.ipfsoft.backend.service.ScheduleService;
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

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de horarios SEMANALES por empleado.
 *
 * <p>El horario es rotativo y se gestiona en una vista separada (no dentro del
 * formulario del trabajador). Acceso para ADMIN y SUPERVISOR; eliminar es solo ADMIN.</p>
 */
@RestController
@RequestMapping("/api/schedules")
@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /** Lista todos los horarios semanales de un empleado. */
    @GetMapping("/{employeeId}")
    public List<WeeklyScheduleResponse> listByEmployee(@PathVariable Long employeeId) {
        return scheduleService.listByEmployee(employeeId);
    }

    /**
     * Devuelve el horario de un empleado para la semana que inicia en la fecha
     * indicada (lunes). Si no existe, devuelve una plantilla vacía para editar.
     */
    @GetMapping("/{employeeId}/week")
    public WeeklyScheduleResponse getByEmployeeAndWeek(@PathVariable Long employeeId,
                                                       @RequestParam LocalDate startDate) {
        return scheduleService.getByEmployeeAndWeek(employeeId, startDate);
    }

    /** Crea (o actualiza) el horario de la semana de un empleado. */
    @PostMapping
    public ResponseEntity<WeeklyScheduleResponse> create(@Valid @RequestBody WeeklyScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.save(request));
    }

    /** Actualiza un horario existente por id. */
    @PutMapping("/{id}")
    public WeeklyScheduleResponse update(@PathVariable Long id, @Valid @RequestBody WeeklyScheduleRequest request) {
        return scheduleService.update(id, request);
    }

    /** Elimina un horario semanal (solo ADMIN). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusResponse delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return StatusResponse.ok("Horario eliminado");
    }

    /**
     * Descarga el PDF del horario de un empleado para la semana que inicia en la
     * fecha indicada (lunes).
     */
    @GetMapping("/{employeeId}/week/{startDate}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long employeeId,
                                               @PathVariable LocalDate startDate) {
        byte[] pdf = scheduleService.generateSchedulePdf(employeeId, startDate);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=horario-" + startDate + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
