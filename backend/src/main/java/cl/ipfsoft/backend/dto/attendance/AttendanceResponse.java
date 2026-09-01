package cl.ipfsoft.backend.dto.attendance;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String rut,
        LocalDate date,
        LocalTime entryTime,
        LocalTime colacionSalida1,
        LocalTime colacionEntrada1,
        LocalTime colacionSalida2,
        LocalTime colacionEntrada2,
        LocalTime exitTime,
        boolean complete,
        boolean absence,
        boolean medicalLicense,
        boolean adminCorrected
) {
}
