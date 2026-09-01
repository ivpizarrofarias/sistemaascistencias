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

/**
 * Detalle (ítem) de la liquidación: un haber o un descuento con su monto.
 * Ejemplo: "Sueldo base" +2.000.000 (haber), "AFP (10%)" -200.000 (descuento).
 */
@Entity
@Table(name = "liquidacion_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "liquidacion_id", nullable = false)
    private Liquidacion liquidacion;

    @Column(nullable = false, length = 100)
    private String concepto;

    /** True = haber (ingresa), False = descuento. */
    @Column(nullable = false)
    private boolean haber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;
}
