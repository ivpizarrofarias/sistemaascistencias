package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.Liquidacion;
import cl.ipfsoft.backend.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LiquidacionRepository extends JpaRepository<Liquidacion, Long> {

    List<Liquidacion> findByEmployeeIdOrderByPeriodYearDescPeriodMonthDesc(Long employeeId);

    Optional<Liquidacion> findByEmployeeIdAndPeriodYearAndPeriodMonth(Long employeeId, int year, int month);

    boolean existsByEmployeeIdAndPeriodYearAndPeriodMonth(Long employeeId, int year, int month);

    Optional<Liquidacion> findTopByEmployeeOrderByPeriodYearDescPeriodMonthDesc(Empleado employee);
}
