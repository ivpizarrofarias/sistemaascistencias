package cl.ipfsoft.backend.controller;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.payroll.AdvanceRequest;
import cl.ipfsoft.backend.dto.payroll.AdvanceResponse;
import cl.ipfsoft.backend.dto.payroll.HoraExtraRequest;
import cl.ipfsoft.backend.dto.payroll.HoraExtraResponse;
import cl.ipfsoft.backend.dto.payroll.LicenciaRequest;
import cl.ipfsoft.backend.dto.payroll.LicenciaResponse;
import cl.ipfsoft.backend.dto.payroll.LiquidacionResponse;
import cl.ipfsoft.backend.service.PayrollService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
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
 * Controlador de remuneraciones (liquidaciones, anticipos y licencias médicas).
 * La generación/modificación de liquidaciones y datos salariales son tareas de administrador.
 * La consulta de liquidaciones está disponible para empleados y administradores.
 */
@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    // ---- Liquidaciones ----

    /** Genera (o recalcula) la liquidación de un empleado para un período (admin). */
    @PostMapping("/liquidaciones/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public LiquidacionResponse createLiquidacion(
            @PathVariable Long employeeId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) java.math.BigDecimal horasExtras) {
        return payrollService.createLiquidacion(employeeId, year, month, horasExtras);
    }

    /** Genera las liquidaciones de todos los empleados de un período (admin). */
    @PostMapping("/liquidaciones/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusResponse liquidarTodos(@RequestParam int year, @RequestParam int month) {
        int n = payrollService.liquidarTodos(year, month);
        return StatusResponse.ok("Se generaron " + n + " liquidaciones", n);
    }

    /** Lista las liquidaciones de un empleado (empleado o admin). */
    @GetMapping("/liquidaciones/employee/{employeeId}")
    public List<LiquidacionResponse> listLiquidaciones(@PathVariable Long employeeId) {
        return payrollService.listLiquidaciones(employeeId);
    }

    /** Obtiene una liquidación por id (empleado o admin). */
    @GetMapping("/liquidaciones/{id}")
    public LiquidacionResponse getLiquidacion(@PathVariable Long id) {
        return payrollService.getLiquidacion(id);
    }

    /** Descarga el PDF de una liquidación por id (empleado o admin). */
    @GetMapping("/liquidaciones/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        byte[] pdf = payrollService.generateLiquidacionPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=liquidacion-" + id + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /** Marca una liquidación como pagada (admin). */
    @PutMapping("/liquidaciones/{id}/pagar")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusResponse marcarPagada(@PathVariable Long id) {
        return payrollService.marcarPagada(id);
    }

    // ---- Anticipos ----

    /** Registra un anticipo de sueldo (admin). */
    @PostMapping("/employee/{employeeId}/advances")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdvanceResponse> addAdvance(@PathVariable Long employeeId,
                                                      @Valid @RequestBody AdvanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollService.addAdvance(employeeId, request));
    }

    /** Lista los anticipos de un empleado. */
    @GetMapping("/employee/{employeeId}/advances")
    public List<AdvanceResponse> listAdvances(@PathVariable Long employeeId) {
        return payrollService.listAdvances(employeeId);
    }

    /** Elimina un anticipo (admin). */
    @DeleteMapping("/advances/{advanceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusResponse deleteAdvance(@PathVariable Long advanceId) {
        return payrollService.deleteAdvance(advanceId);
    }

    // ---- Licencias médicas ----

    /** Registra una licencia médica (admin). */
    @PostMapping("/employee/{employeeId}/licencias")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenciaResponse> addLicencia(@PathVariable Long employeeId,
                                                        @Valid @RequestBody LicenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollService.addLicencia(employeeId, request));
    }

    /** Lista las licencias de un empleado. */
    @GetMapping("/employee/{employeeId}/licencias")
    public List<LicenciaResponse> listLicencias(@PathVariable Long employeeId) {
        return payrollService.listLicencias(employeeId);
    }

    /** Elimina una licencia médica (admin). */
    @DeleteMapping("/licencias/{licenciaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusResponse deleteLicencia(@PathVariable Long licenciaId) {
        return payrollService.deleteLicencia(licenciaId);
    }

    // ---- Horas extra ----

    /** Registra horas extra de un empleado en una fecha (admin). */
    @PostMapping("/employee/{employeeId}/horasextras")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HoraExtraResponse> addHoraExtra(@PathVariable Long employeeId,
                                                          @Valid @RequestBody HoraExtraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollService.addHoraExtra(employeeId, request));
    }

    /** Lista las horas extra registradas de un empleado. */
    @GetMapping("/employee/{employeeId}/horasextras")
    public List<HoraExtraResponse> listHorasExtras(@PathVariable Long employeeId) {
        return payrollService.listHorasExtras(employeeId);
    }

    /** Elimina un registro de horas extra (admin). */
    @DeleteMapping("/horasextras/{horaExtraId}")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusResponse deleteHoraExtra(@PathVariable Long horaExtraId) {
        return payrollService.deleteHoraExtra(horaExtraId);
    }
}
