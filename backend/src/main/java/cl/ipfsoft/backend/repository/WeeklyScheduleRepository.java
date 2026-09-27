package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.entity.HorarioSemanal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyScheduleRepository extends JpaRepository<HorarioSemanal, Long> {

    /** Busca el horario de un empleado para la semana que inicia en la fecha indicada (lunes). */
    Optional<HorarioSemanal> findByEmployeeAndStartDate(Empleado employee, LocalDate startDate);

    /** Lista todos los horarios semanales de un empleado, del más reciente al más antiguo. */
    List<HorarioSemanal> findByEmployeeOrderByStartDateDesc(Empleado employee);

    /** Lista los horarios semanales de varios empleados en un rango (para consulta de un día). */
    List<HorarioSemanal> findByStartDate(LocalDate startDate);

    /** Elimina los horarios de un empleado (se usa al borrar el empleado). */
    void deleteByEmployee(Empleado employee);
}
