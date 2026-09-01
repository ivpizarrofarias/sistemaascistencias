package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.Employee;
import cl.ipfsoft.backend.entity.WeeklySchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyScheduleRepository extends JpaRepository<WeeklySchedule, Long> {

    /** Busca el horario de un empleado para la semana que inicia en la fecha indicada (lunes). */
    Optional<WeeklySchedule> findByEmployeeAndStartDate(Employee employee, LocalDate startDate);

    /** Lista todos los horarios semanales de un empleado, del más reciente al más antiguo. */
    List<WeeklySchedule> findByEmployeeOrderByStartDateDesc(Employee employee);

    /** Lista los horarios semanales de varios empleados en un rango (para consulta de un día). */
    List<WeeklySchedule> findByStartDate(LocalDate startDate);

    /** Elimina los horarios de un empleado (se usa al borrar el empleado). */
    void deleteByEmployee(Employee employee);
}
