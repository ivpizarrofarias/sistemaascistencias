package cl.ipfsoft.backend.mapper;

import cl.ipfsoft.backend.dto.attendance.AttendanceResponse;
import cl.ipfsoft.backend.dto.attendance.MarkResponse;
import cl.ipfsoft.backend.entity.Attendance;
import cl.ipfsoft.backend.entity.Employee;
import cl.ipfsoft.backend.util.RutUtils;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {

    public MarkResponse toMarkResponse(Attendance a) {
        String message = a.isComplete()
                ? "Jornada completa. No puede seguir marcando hoy."
                : switch (a.getNextStep()) {
                    case "COLACION_SALIDA" -> "Salida a colación registrada. Puede marcar la entrada de colación.";
                    case "COLACION_ENTRADA" -> "Entrada de colación registrada.";
                    case "SALIDA" -> "Ahora puede marcar la salida del trabajo.";
                    default -> "Entrada registrada.";
                };
        return new MarkResponse(
                a.getEmployee().getId(),
                a.getEmployee().getFullName(),
                a.getDate(),
                a.getNextStep() != null ? cl.ipfsoft.backend.entity.MarkStep.valueOf(a.getNextStep()) : null,
                a.getEntryTime(),
                a.getColacionSalida1(),
                a.getColacionEntrada1(),
                a.getColacionSalida2(),
                a.getColacionEntrada2(),
                a.getExitTime(),
                a.getColacionSalidaCount(),
                a.getColacionEntradaCount(),
                a.isComplete(),
                !a.isComplete(),
                message
        );
    }

    public AttendanceResponse toResponse(Attendance a) {
        Employee e = a.getEmployee();
        return new AttendanceResponse(
                a.getId(),
                e.getId(),
                e.getFullName(),
                RutUtils.format(e.getRut()),
                a.getDate(),
                a.getEntryTime(),
                a.getColacionSalida1(),
                a.getColacionEntrada1(),
                a.getColacionSalida2(),
                a.getColacionEntrada2(),
                a.getExitTime(),
                a.isComplete(),
                a.isAbsence(),
                a.isMedicalLicense(),
                a.isAdminCorrected()
        );
    }
}
