package cl.ipfsoft.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Horas extra trabajadas por el empleado en una fecha concreta.
 * El administrador las registra manualmente (botón "Horas extra" en la lista
 * de empleados) y se suman a la liquidación de sueldo del período.
 *
 * <p>{@code horas} es solo informativo (cuántas horas se trabajaron de más).
 * El dinero que se paga es {@code monto}, tal como lo ingresa el administrador:
 * no se recalcula con el valor hora ni se le aplica multiplicador, para que la
 * liquidación muestre exactamente el monto acordado.</p>
 */
@Entity
@Table(name = "horas_extra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoraExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Empleado employee;

    @Column(nullable = false)
    private LocalDate date;

    /** Cantidad de horas extra trabajadas (informativo). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal horas;

    /** Monto a pagar en CLP. Es el valor exacto que se suma a la liquidación. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto = BigDecimal.ZERO;
}
