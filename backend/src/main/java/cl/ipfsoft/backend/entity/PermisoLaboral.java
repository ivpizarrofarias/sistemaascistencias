package cl.ipfsoft.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Permiso laboral solicitado por un trabajador.
 * <p>
 * El trabajador puede pedir permiso por horas (POR_HORA) o por días (POR_DIA),
 * y eligir cómo compensarlo:
 *  - DEVOLVER: repone después las horas/días solicitados (devuelve el tiempo).
 *  - DESCUENTO: se le descuenta del sueldo.
 * La aprobación la realiza el administrador.
 * </p>
 */
@Entity
@Table(name = "permisos_laborales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermisoLaboral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /** Fecha en que se toma el permiso. Para POR_HORA es el día; junto a horas indica el tiempo. */
    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermisoTipo tipo;

    /** Cantidad de horas pedidas (solo para POR_HORA). */
    @Column(nullable = false)
    private BigDecimal horas = BigDecimal.ZERO;

    /** Cantidad de días pedidos (solo para POR_DIA). */
    @Column(nullable = false)
    private int dias = 0;

    @Column(nullable = false, length = 300)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermisoEstado estado = PermisoEstado.PENDIENTE;

    /** Forma de compensación: devolver las horas/días o que se descuenten. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormaCompensacion formaCompensacion = FormaCompensacion.DESCUENTO;

    /** Horas ya devueltas/compensadas por el trabajador (para DEVOLVER). */
    @Column(nullable = false)
    private BigDecimal horasDevueltas = BigDecimal.ZERO;

    /** Días ya devueltos/compensados por el trabajador (para DEVOLVER). */
    @Column(nullable = false)
    private int diasDevueltos = 0;

    @Column
    private LocalDate fechaSolicitud = LocalDate.now();

    @Column
    private LocalDate fechaValidacion;

    /** Nombre del administrador que aprobó/rechazó el permiso. */
    @Column(length = 100)
    private String validadoPor;
}
