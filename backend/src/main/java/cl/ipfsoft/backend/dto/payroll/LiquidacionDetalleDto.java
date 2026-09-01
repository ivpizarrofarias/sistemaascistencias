package cl.ipfsoft.backend.dto.payroll;

import java.math.BigDecimal;

public record LiquidacionDetalleDto(
        String concepto,
        boolean haber,
        BigDecimal monto
) {
}
