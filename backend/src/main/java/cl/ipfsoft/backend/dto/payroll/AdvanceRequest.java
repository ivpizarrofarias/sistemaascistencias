package cl.ipfsoft.backend.dto.payroll;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdvanceRequest(
        LocalDate date,
        BigDecimal amount,
        String description
) {
}
