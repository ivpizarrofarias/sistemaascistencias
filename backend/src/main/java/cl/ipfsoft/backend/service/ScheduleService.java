package cl.ipfsoft.backend.service;

import cl.ipfsoft.backend.dto.schedule.WeeklyScheduleRequest;
import cl.ipfsoft.backend.dto.schedule.WeeklyScheduleResponse;

import java.util.List;

/**
 * Servicio de horarios SEMANALES por empleado.
 *
 * <p>El horario es rotativo: cada semana el administrador define qué días y a qué
 * horas trabaja cada empleado. Por eso el horario NO forma parte del empleado,
 * sino que se gestiona por separado en una vista de "Horarios".</p>
 */
public interface ScheduleService {

    /** Lista todos los horarios semanales de un empleado. */
    List<WeeklyScheduleResponse> listByEmployee(Long employeeId);

    /**
     * Devuelve el horario de un empleado para la semana que inicia en
     * {@code startDate} (lunes). Si aún no existe, devuelve una plantilla vacía
     * (id {@code null}) lista para editar.
     */
    WeeklyScheduleResponse getByEmployeeAndWeek(Long employeeId, java.time.LocalDate startDate);

    /** Crea o actualiza el horario de la semana de un empleado. */
    WeeklyScheduleResponse save(WeeklyScheduleRequest request);

    /** Actualiza un horario existente por id. */
    WeeklyScheduleResponse update(Long id, WeeklyScheduleRequest request);

    /** Elimina un horario semanal. */
    void delete(Long id);

    /**
     * Genera el PDF (bytes) del horario de un empleado para la semana que inicia
     * en {@code startDate} (lunes). Si no existe horario registrado, genera el
     * documento con todos los días "No".
     */
    byte[] generateSchedulePdf(Long employeeId, java.time.LocalDate startDate);
}
