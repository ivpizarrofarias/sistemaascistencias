package cl.ipfsoft.backend.dto.payroll;

import java.math.BigDecimal;
import java.util.List;

public record LiquidacionResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String rut,
        int periodYear,
        int periodMonth,
        int diasMes,
        int diasHabiles,
        int diasTrabajados,
        int diasInasistencia,
        int diasLicencia,
        BigDecimal sueldoBase,
        BigDecimal gratificacion,
        BigDecimal asignacionFamiliar,
        BigDecimal bonoMovilizacion,
        BigDecimal horasExtras,
        BigDecimal montoHorasExtras,
        BigDecimal otrosHaberes,
        BigDecimal baseImponible,
        BigDecimal totalHaberes,
        BigDecimal afp,
        BigDecimal sis,
        BigDecimal salud,
        BigDecimal impuestoUnico,
        BigDecimal cesantia,
        BigDecimal descuentoFaltas,
        int descuentoLicenciaDias,
        BigDecimal descuentoLicencia,
        BigDecimal anticipos,
        BigDecimal descuentoHorasExtras,
        BigDecimal otrosDescuentos,
        BigDecimal totalDescuentos,
        BigDecimal liquidoAPagar,
        boolean pagada,
        List<LiquidacionDetalleDto> detalles
) {
}
