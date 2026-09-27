package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.Asistencia;
import cl.ipfsoft.backend.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Asistencia, Long> {

    Optional<Asistencia> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<Asistencia> findByEmployeeIdAndDateBetweenOrderByDateAsc(Long employeeId, LocalDate start, LocalDate end);

    List<Asistencia> findByEmployeeIdOrderByDateAsc(Long employeeId);

    List<Asistencia> findByEmployeeIdAndCompleteFalseOrderByDateDesc(Long employeeId);

    /** Último registro (de mayor fecha) del empleado, para validar secuencia. */
    Optional<Asistencia> findTopByEmployeeOrderByDateDesc(Empleado employee);

    List<Asistencia> findByDateBetween(LocalDate start, LocalDate end);
}
