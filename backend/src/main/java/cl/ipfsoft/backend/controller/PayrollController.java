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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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

    /**
     * Genera (o recalcula) la liquidación de un empleado para un período (admin).
     * Las horas extra se toman de los registros por fecha del período; no se aceptan
     * a mano para evitar duplicarlas.
     */
    @PostMapping("/liquidaciones/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public LiquidacionResponse createLiquidacion(
            @PathVariable Long employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        return payrollService.createLiquidacion(employeeId, year, month);
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
        LiquidacionResponse l = payrollService.getLiquidacion(id);
        byte[] pdf = payrollService.generateLiquidacionPdf(id);
        String archivo = nombreArchivoLiquidacion(l);
        return ResponseEntity.ok()
                // Se envía la versión ASCII como respaldo y la real en UTF-8 (RFC 5987)
                // para que el navegador conserve tildes y la "ñ".
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ascii(archivo)
                        + "\"; filename*=UTF-8''" + urlEncoder(archivo))
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /** Nombre del PDF: Liquidación_Javiera_Muñoz_Septiembre_2026.pdf */
    private String nombreArchivoLiquidacion(LiquidacionResponse l) {
        String nombre = l.employeeName().trim().replaceAll("\\s+", "_");
        Locale es = new Locale("es", "CL");
        String mes = YearMonth.of(l.periodYear(), l.periodMonth())
                .format(DateTimeFormatter.ofPattern("MMMM", es));
        mes = mes.substring(0, 1).toUpperCase(es) + mes.substring(1);
        return "Liquidación_" + nombre + "_" + mes + "_" + l.periodYear() + ".pdf";
    }

    /** Sustituye todo lo que no sea ASCII por su equivalente sin acento. */
    private String ascii(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String urlEncoder(String texto) {
        return URLEncoder.encode(texto, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
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
