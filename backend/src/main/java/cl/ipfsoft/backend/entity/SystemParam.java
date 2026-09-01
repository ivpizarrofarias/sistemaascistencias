package cl.ipfsoft.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Parámetros del sistema para el cálculo de liquidaciones (valores legales chilenos).
 * Editables por el administrador.
 */
@Entity
@Table(name = "system_params")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemParam {

    public static final String UF = "uf";
    public static final String UTM = "utm";
    public static final String ASIGNACION_FAMILIAR_TRAMO1 = "asignacion_familiar_tramo1";
    public static final String GRATIFICACION_PCT = "gratificacion_pct";
    public static final String SIS_PCT = "sis_pct";
    public static final String CESANTIA_EMPLEADO_PCT = "cesantia_empleado_pct";
    public static final String HORAS_EXTRA_MULT = "horas_extra_mult";
    public static final String BONO_MOVILIZACION = "bono_movilizacion";

    @Id
    @Column(name = "param_key", length = 50)
    private String key;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(name = "param_value", nullable = false, precision = 20, scale = 4)
    private BigDecimal value;
}
