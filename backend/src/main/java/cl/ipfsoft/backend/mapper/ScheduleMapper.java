package cl.ipfsoft.backend.mapper;

import cl.ipfsoft.backend.dto.schedule.ScheduleDayRequest;
import cl.ipfsoft.backend.dto.schedule.ScheduleDayResponse;
import cl.ipfsoft.backend.dto.schedule.WeeklyScheduleRequest;
import cl.ipfsoft.backend.dto.schedule.WeeklyScheduleResponse;
import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.entity.DiaHorario;
import cl.ipfsoft.backend.entity.HorarioSemanal;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Conversión entre la entidad HorarioSemanal y sus DTOs.
 */
@Component
public class ScheduleMapper {

    private static final int MONDAY = 1;
    private static final int SATURDAY = 6;

    public WeeklyScheduleResponse toResponse(HorarioSemanal ws) {
        Empleado emp = ws.getEmployee();
        List<ScheduleDayResponse> days = ws.getDays().stream()
                .sorted(Comparator.comparingInt(DiaHorario::getDayNumber))
                .map(this::dayToResponse)
                .toList();
        return new WeeklyScheduleResponse(
                ws.getId(),
                emp.getId(),
                emp.getFullName(),
                ws.getStartDate(),
                ws.getStartDate().plusDays(5),
                ws.getLabel(),
                days
        );
    }

    private ScheduleDayResponse dayToResponse(DiaHorario d) {
        return new ScheduleDayResponse(d.getId(), d.getDayNumber(), d.isWorked(), d.getStartTime(), d.getEndTime());
    }

    /**
     * Aplica los datos del request al horario semanal, regenerando los 6 días.
     * Para cada día (lunes a sábado) se toman los datos que vienen en {@code days}
     * o se crea una fila por defecto (no trabajado) si falta.
     * <p>
     * Se REUTILIZAN las filas de día existentes (por dayNumber) para que, al
     * actualizar, Hibernate realice UPDATE en lugar de borrar+insertar y así no
     * violar el índice único (schedule_id, dayNumber).
     * </p>
     */
    public void applyRequest(HorarioSemanal ws, WeeklyScheduleRequest r, Empleado employee) {
        ws.setEmployee(employee);
        ws.setStartDate(r.startDate());
        // La etiqueta es opcional: una cadena en blanco se guarda como null para
        // no dejar espacios sueltos en la base ni en la UI.
        String etiqueta = r.label();
        ws.setLabel(etiqueta == null || etiqueta.isBlank() ? null : etiqueta.trim());

        // Mapa de días existentes (dayNumber -> DiaHorario) para reutilizarlos.
        java.util.Map<Integer, DiaHorario> existentes = new java.util.HashMap<>();
        for (DiaHorario d : ws.getDays()) {
            existentes.put(d.getDayNumber(), d);
        }

        List<DiaHorario> nuevos = new ArrayList<>();
        for (int day = MONDAY; day <= SATURDAY; day++) {
            final int d = day;
            ScheduleDayRequest req = r.days().stream()
                    .filter(x -> x.dayNumber() == d)
                    .findFirst()
                    .orElse(null);

            DiaHorario sd = existentes.get(day);
            if (sd == null) {
                sd = new DiaHorario();
                sd.setSchedule(ws);
                sd.setDayNumber(day);
            }
            if (req != null) {
                sd.setWorked(req.worked());
                sd.setStartTime(req.startTime());
                sd.setEndTime(req.endTime());
            } else {
                sd.setWorked(false);
            }
            nuevos.add(sd);
        }

        ws.getDays().clear();
        ws.getDays().addAll(nuevos);
    }

    /** Crea la semana con el primer día trabajado por defecto (lunes) para edición. */
    public HorarioSemanal empty(Empleado employee, java.time.LocalDate startDate) {
        HorarioSemanal ws = new HorarioSemanal();
        ws.setEmployee(employee);
        ws.setStartDate(startDate);
        ws.setLabel("Semana del " + startDate);
        applyRequest(ws, new WeeklyScheduleRequest(
                employee.getId(), startDate, ws.getLabel(), List.of()), employee);
        return ws;
    }
}
