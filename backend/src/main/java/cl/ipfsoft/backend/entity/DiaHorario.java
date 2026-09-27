package cl.ipfsoft.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Un día de un horario semanal ({@link HorarioSemanal}).
 *
 * <p>{@code dayNumber} va de 1 (lunes) a 6 (sábado). Si {@code worked} es false,
 * ese día el empleado no trabaja y las horas se ignoran.</p>
 */
@Entity
@Table(name = "dias_horario_semanal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_id", "dayNumber"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private HorarioSemanal schedule;

    /** Día de la semana: 1=lunes ... 6=sábado. */
    @Column(nullable = false)
    private int dayNumber;

    /** Indica si el empleado trabaja ese día. */
    @Column(nullable = false)
    private boolean worked = false;

    /** Hora de entrada de ese día. */
    @Column(name = "start_time")
    private LocalTime startTime;

    /** Hora de salida de ese día. */
    @Column(name = "end_time")
    private LocalTime endTime;
}
