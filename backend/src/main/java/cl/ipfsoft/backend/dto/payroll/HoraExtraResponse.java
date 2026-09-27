package cl.ipfsoft.backend.dto.payroll;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HoraExtraResponse(
        Long id,
        Long employeeId,
        LocalDate date,
        BigDecimal horas,
        BigDecimal monto
) {
}
