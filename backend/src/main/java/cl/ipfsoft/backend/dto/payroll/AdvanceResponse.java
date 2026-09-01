package cl.ipfsoft.backend.dto.payroll;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdvanceResponse(
        Long id,
        Long employeeId,
        LocalDate date,
        BigDecimal amount,
        String description
) {
}
