package cl.ipfsoft.backend.config;

import cl.ipfsoft.backend.entity.Usuario;
import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.entity.Rol;
import cl.ipfsoft.backend.entity.ParametroSistema;
import cl.ipfsoft.backend.repository.AppUserRepository;
import cl.ipfsoft.backend.repository.EmployeeRepository;
import cl.ipfsoft.backend.repository.SystemParamRepository;
import cl.ipfsoft.backend.util.CodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Inicializador de datos de arranque de la aplicación.
 * Se ejecuta una sola vez al levantar el servidor y crea:
 *   - El usuario administrador por defecto (si no existe).
 *   - Los parámetros del sistema necesarios para calcular liquidaciones
 *     conforme a la legislación chilena (UF, asignación familiar, etc.).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final SystemParamRepository paramRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    // Datos del administrador por defecto desde application.properties.
    @Value("${app.admin.username}") private String adminUsername;
    @Value("${app.admin.password}") private String adminPassword;

    public DataInitializer(AppUserRepository userRepository,
                           SystemParamRepository paramRepository,
                           EmployeeRepository employeeRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.paramRepository = paramRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createDefaultAdmin();
        createDefaultParams();
        // Asignar código de marcaje a los empleados creados antes de esta funcionalidad.
        backfillEmployeeCodes();
    }

    /**
     * Crea el usuario admin por defecto si todavía no existe.
     */
    private void createDefaultAdmin() {
        if (!userRepository.existsByUsername(adminUsername)) {
            Usuario admin = new Usuario();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Rol.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println(">>> Usuario admin creado: " + adminUsername);
        }
    }

    /**
     * Crea (si no existen) los parámetros del sistema con valores por defecto
     * basados en la legislación laboral chilena vigente.
     */
    private void createDefaultParams() {
        put(ParametroSistema.UF, "Valor de la UF en pesos", new BigDecimal("38000.00"));
        put(ParametroSistema.UTM, "Valor de la UTM en pesos (para Impuesto Único)", new BigDecimal("67000.00"));
        put(ParametroSistema.ASIGNACION_FAMILIAR_TRAMO1, "Asignación familiar tramo 1 (por carga, en CLP)",
                new BigDecimal("21569.00"));
        put(ParametroSistema.GRATIFICACION_PCT, "Porcentaje de gratificación legal (15% o 4.25 1/M)",
                new BigDecimal("0.15"));
        put(ParametroSistema.SIS_PCT, "Seguro de Invalidez y Sobrevivencia (porcentaje)",
                new BigDecimal("0.0144"));
        put(ParametroSistema.CESANTIA_EMPLEADO_PCT, "Seguro de Cesantía - parte del trabajador (0.6%)",
                new BigDecimal("0.006"));
        put(ParametroSistema.HORAS_EXTRA_MULT, "Multiplicador de las horas extra sobre el valor hora",
                new BigDecimal("1.5"));
        put(ParametroSistema.BONO_MOVILIZACION, "Bono de movilización obligatorio (CLP)",
                new BigDecimal("30000.00"));
    }

    /**
     * Inserta un parámetro si no existe.
     */
    private void put(String key, String description, BigDecimal value) {
        if (!paramRepository.existsById(key)) {
            paramRepository.save(new ParametroSistema(key, description, value));
        }
    }

    /**
     * Asigna un código de marcaje de 4 dígitos a los empleados que no tienen uno
     * válido: los registros antiguos que aún usan el formato anterior (4 dígitos
     * del RUT + letras) y los que nunca lo tuvieron.
     *
     * Es idempotente: solo toca a quienes no cumplen el formato, así que puede
     * ejecutarse en cada arranque sin alterar los códigos ya buenos.
     */
    private void backfillEmployeeCodes() {
        employeeRepository.findAll().forEach(e -> {
            if (!CodeGenerator.esCodigoValido(e.getCode())) {
                e.setCode(CodeGenerator.uniqueCode(employeeRepository::existsByCode));
                employeeRepository.save(e);
            }
        });
    }


}
