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

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Registro de asistencia de un empleado para un día.
 * <p>
 * La secuencia de marcas respeta un orden estricto (state machine):
 * ENTRADA -> COLACION_SALIDA -> COLACION_ENTRADA -> SALIDA, con UNA única colación.
 * Si un paso no se completa, el empleado NO puede marcar pasos posteriores
 * ese día ni el día siguiente, hasta que un administrador corrija el registro.
 * </p>
 */
@Entity
@Table(name = "attendances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    // Marcas de tiempo (el día y hora exactos en que se marcó)
    @Column
    private LocalTime entryTime;

    // Colación: mínimo 1 salida, máximo 2 (y sus correspondientes entradas)
    @Column
    private LocalTime colacionSalida1;
    @Column
    private LocalTime colacionEntrada1;
    @Column
    private LocalTime colacionSalida2;
    @Column
    private LocalTime colacionEntrada2;

    @Column
    private LocalTime exitTime;

    /** Siguiente paso que el empleado puede marcar (se avanza en orden). */
    @Column(length = 20)
    private String nextStep;

    /** Cuenta cuántos marcajes de colación de salida se han hecho (0..2). */
    @Column(nullable = false)
    private int colacionSalidaCount = 0;

    /** Cuenta cuántos marcajes de colación de entrada se han hecho (0..2). */
    @Column(nullable = false)
    private int colacionEntradaCount = 0;

    /** True cuando el admin corrigió/a completó este registro (desbloquea secuencias). */
    @Column(nullable = false)
    private boolean adminCorrected = false;

    /** True cuando el día quedó completamente marcado (salida incluida). */
    @Column(nullable = false)
    private boolean complete = false;

    /** True si el empleado faltó (registro creado por ausencia no justificada). */
    @Column(nullable = false)
    private boolean absence = false;

    /** True si el día corresponde a licencia médica. */
    @Column(nullable = false)
    private boolean medicalLicense = false;
}
