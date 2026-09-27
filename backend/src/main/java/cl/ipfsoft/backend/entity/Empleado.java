package cl.ipfsoft.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "empleados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** RUT con guion y dígito verificador, normalizado (12345678-5). */
    @Column(nullable = false, unique = true, length = 12)
    private String rut;

    /**
     * Código de marcaje del empleado (generado automáticamente al crear).
     * El empleado NO tiene usuario ni contraseña: este código es su única
     * credencial para registrar asistencia en el kiosco/marcador móvil.
     * La columna admite NULL solo para los registros existentes; la aplicación
     * lo completa al arrancar (DataInitializer) y siempre lo asigna al crear.
     */
    @Column(unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(length = 50)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private LocalDate hireDate;

    /** Sueldo base mensual (CLP). */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;

    /** Número de cargas familiares declaradas. */
    @Column(nullable = false)
    private int dependents = 0;

    /** Sistema previsional elegido (afp o isapre) para liquidación. */
    @Column(nullable = false, length = 10)
    private String pensionSystem = "AFP";

    @Column(nullable = false, length = 20)
    private String afpFund = "APROVISIONA";

    @Column(nullable = false, length = 20)
    private String healthSystem = "ISAPRE";

    /**
     * Indica si el empleado está habilitado para marcar asistencia.
     * Solo el administrador puede deshabilitar.
     */
    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private String rolEconomico = "9";

    @OneToOne(mappedBy = "employee")
    private Usuario user;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
