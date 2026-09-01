package cl.ipfsoft.backend.service;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.payroll.AdvanceRequest;
import cl.ipfsoft.backend.dto.payroll.AdvanceResponse;
import cl.ipfsoft.backend.dto.payroll.HoraExtraRequest;
import cl.ipfsoft.backend.dto.payroll.HoraExtraResponse;
import cl.ipfsoft.backend.dto.payroll.LicenciaRequest;
import cl.ipfsoft.backend.dto.payroll.LicenciaResponse;
import cl.ipfsoft.backend.dto.payroll.LiquidacionResponse;

import java.util.List;

/**
 * Servicio de remuneraciones (liquidación de sueldo).
 *
 * Calcula la liquidación mensual de cada empleado conforme a la legislación
 * laboral chilena, considerando:
 *  - Sueldo base proporcional a los días trabajados.
 *  - Descuento por faltas (inasistencia).
 *  - Licencia médica (subsidio parte empleador).
 *  - Asignación familiar por cargas.
 *  - Gratificación legal.
 *  - Descuentos previsionales (AFP + SIS), salud (7%), cesantía.
 *  - Impuesto Único de 2ª categoría.
 *  - Anticipos de sueldo otorgados en el período.
 *  - Descuento por permisos laborales (o su saldo pendiente de devolución).
 */
public interface PayrollService {

    /** Calcula y guarda (o recalcula) la liquidación de un empleado para un período. */
    LiquidacionResponse createLiquidacion(Long employeeId, int year, int month);

    /** Igual que {@link #createLiquidacion} pero registra las horas extra del período. */
    LiquidacionResponse createLiquidacion(Long employeeId, int year, int month, java.math.BigDecimal horasExtras);

    /** Genera las liquidaciones de todos los empleados de un período y devuelve la cantidad creada. */
    int liquidarTodos(int year, int month);

    /** Lista las liquidaciones de un empleado (más recientes primero). */
    List<LiquidacionResponse> listLiquidaciones(Long employeeId);

    /** Obtiene una liquidación por id. */
    LiquidacionResponse getLiquidacion(Long liquidacionId);

    /** Genera el PDF (bytes) de una liquidación por id. */
    byte[] generateLiquidacionPdf(Long liquidacionId);

    /** Marca una liquidación como pagada. */
    StatusResponse marcarPagada(Long liquidacionId);

    // ---- Anticipos ----
    AdvanceResponse addAdvance(Long employeeId, AdvanceRequest request);

    List<AdvanceResponse> listAdvances(Long employeeId);

    StatusResponse deleteAdvance(Long advanceId);

    // ---- Licencias médicas ----
    LicenciaResponse addLicencia(Long employeeId, LicenciaRequest request);

    List<LicenciaResponse> listLicencias(Long employeeId);

    StatusResponse deleteLicencia(Long licenciaId);

    // ---- Horas extra (registro manual por fecha) ----
    HoraExtraResponse addHoraExtra(Long employeeId, HoraExtraRequest request);

    List<HoraExtraResponse> listHorasExtras(Long employeeId);

    StatusResponse deleteHoraExtra(Long horaExtraId);
}
