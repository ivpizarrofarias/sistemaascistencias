package cl.ipfsoft.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Liquidación de sueldo del empleado (liquidación de haberes y descuentos),
 * calculada conforme a la legislación laboral chilena para el período indicado.
 */
@Entity
@Table(name = "liquidaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Liquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private int periodYear;

    @Column(nullable = false)
    private int periodMonth;

    // ---- Días ----
    @Column(nullable = false)
    private int diasMes;

    @Column(nullable = false)
    private int diasHabiles;

    @Column(nullable = false)
    private int diasTrabajados;

    @Column(nullable = false)
    private int diasInasistencia;

    @Column(nullable = false)
    private int diasLicencia;

    // ---- Haberes (ingresos) ----
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal sueldoBase = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal gratificacion = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal asignacionFamiliar = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subsidioLicenciaEmpleador = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal otrosHaberes = BigDecimal.ZERO;

    /** Horas extra del período (ingresadas manualmente por el administrador). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal horasExtras = BigDecimal.ZERO;

    /** Monto pagado por las horas extra (valor hora x 1,5). */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montoHorasExtras = BigDecimal.ZERO;

    /** Base imponible (sueldo base + gratificación + asignación familiar + bono movilización + horas extras + otros haberes). */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseImponible = BigDecimal.ZERO;

    /** Bono de movilización obligatorio. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal bonoMovilizacion = BigDecimal.ZERO;

    /** Descuento por horas extras (según legislación). */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal descuentoHorasExtras = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalHaberes = BigDecimal.ZERO;

    // ---- Descuentos ----
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal afp = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal sis = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salud = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal impuestoUnico = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal cesantia = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal descuentoFaltas = BigDecimal.ZERO;

    /** Días de licencia médica no pagados por el empleador (se descuentan). */
    @Column(nullable = false)
    private int descuentoLicenciaDias = 0;

    /** Monto descontado por los días de licencia no pagados por el empleador. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal descuentoLicencia = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal anticipos = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal otrosDescuentos = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDescuentos = BigDecimal.ZERO;

    // ---- Resultado ----
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal liquidoAPagar = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean pagada = false;

    @OneToMany(mappedBy = "liquidacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LiquidacionDetalle> detalles = new ArrayList<>();
}
