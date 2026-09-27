package cl.ipfsoft.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Horario de trabajo SEMANAL de un empleado.
 *
 * <p>Los horarios NO son fijos: rotan semana a semana, por lo que cada empleado
 * puede tener un horario distinto cada semana. Una {@link HorarioSemanal}
 * corresponde a la semana que inicia en {@code startDate} (lunes) y contiene
 * un {@link DiaHorario} por día (lunes a sábado) con su hora de entrada/salida
 * y si ese día se trabaja o no.</p>
 */
@Entity
@Table(name = "horarios_semanales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HorarioSemanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Empleado employee;

    /** Lunes de la semana a la que corresponde este horario. */
    @Column(nullable = false)
    private LocalDate startDate;

    /** Etiqueta / nota opcional (p. ej. "Semana A"). */
    @Column(length = 100)
    private String label;

    /** Contiene una fila por día (lunes a sábado) de la semana. */
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("dayNumber ASC")
    private List<DiaHorario> days = new ArrayList<>();
}
