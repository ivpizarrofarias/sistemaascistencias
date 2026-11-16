package cl.ipfsoft.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol role;

    /** Empleado asociado (solo para rol EMPLOYEE). */
    @OneToOne
    @JoinColumn(name = "employee_id")
    private Empleado employee;

    @Column(nullable = false)
    private boolean enabled = true;

    /** Nombre completo para mostrar en la aplicación (opcional). */
    @Column(name = "full_name", length = 120)
    private String fullName;

    /** Foto de perfil en base64 (opcional). */
    @Column(name = "foto_base64", columnDefinition = "LONGTEXT")
    private String fotoBase64;
}
