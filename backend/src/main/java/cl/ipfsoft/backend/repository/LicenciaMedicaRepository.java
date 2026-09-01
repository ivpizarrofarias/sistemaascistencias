package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.LicenciaMedica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LicenciaMedicaRepository extends JpaRepository<LicenciaMedica, Long> {

    List<LicenciaMedica> findByEmployeeId(Long employeeId);

    List<LicenciaMedica> findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId, LocalDate end, LocalDate start);
}
