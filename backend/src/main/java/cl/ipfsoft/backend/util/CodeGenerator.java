package cl.ipfsoft.backend.util;

import java.security.SecureRandom;
import java.util.function.Predicate;

/**
 * Genera el código de marcaje de los empleados.
 *
 * El empleado no tiene usuario ni contraseña: su credencial es un CÓDIGO de
 * 4 dígitos, único dentro del sistema. Ejemplo: {@code 0483}.
 *
 * El código es aleatorio (y no derivado del RUT ni del nombre) por dos motivos:
 *   - no permite adivinar el código de un compañero mirando el suyo, ya que
 *     los dígitos no se relacionan entre sí;
 *   - el RUT puede corregirse o cambiar de dígito verificador sin que el
 *     trabajador tenga que aprender una credencial nueva.
 *
 * Como solo hay 9000 combinaciones posibles (1000-9999), el kiosco de marcaje
 * debe seguir siendo un dispositivo físico de la empresa.
 *
 * El código se asigna una sola vez al crear al empleado y NUNCA se modifica:
 * es la credencial con la que el trabajador marca asistencia.
 */
public final class CodeGenerator {

    /** Cantidad de dígitos del código de marcaje. */
    public static final int CODE_LENGTH = 4;

    /** Rango inclusivo de los códigos: de 1000 a 9999 (4 dígitos, sin 0 inicial). */
    private static final int MIN_CODE = 1000;
    private static final int MAX_CODE = 9999;

    /** Intentos aleatorios antes de caer al barrido secuencial que garantiza terminación. */
    private static final int MAX_INTENTOS_ALEATORIOS = 200;

    private static final SecureRandom RANDOM = new SecureRandom();

    private CodeGenerator() {
    }

    /**
     * Genera un código de marcaje de 4 dígitos que aún no esté en uso.
     *
     * @param isUsed función que indica si un código ya está registrado. Si es
     *               {@code null} se considera que no hay ninguno en uso.
     * @return código de 4 dígitos, único.
     */
    public static String uniqueCode(Predicate<String> isUsed) {
        // 1. Se prueban candidatos aleatorios: es lo habitual y reparte los
        //    códigos de forma impredecible.
        for (int i = 0; i < MAX_INTENTOS_ALEATORIOS; i++) {
            String candidato = randomCode();
            if (!enUso(candidato, isUsed)) {
                return candidato;
            }
        }

        // 2. Si la tabla está muy llena, se recorre el rango en orden para
        //    garantizar que el método siempre termine.
        for (int n = MIN_CODE; n <= MAX_CODE; n++) {
            String candidato = String.valueOf(n);
            if (!enUso(candidato, isUsed)) {
                return candidato;
            }
        }

        throw new IllegalStateException(
                "No quedan códigos de marcaje disponibles: se agotaron los " + (MAX_CODE - MIN_CODE + 1) + " códigos de 4 dígitos");
    }

    /** Construye un código aleatorio de 4 dígitos, sin verificar disponibilidad. */
    public static String randomCode() {
        return String.valueOf(MIN_CODE + RANDOM.nextInt(MAX_CODE - MIN_CODE + 1));
    }

    /** Indica si un texto tiene el formato de un código de marcaje (4 dígitos). */
    public static boolean esCodigoValido(String code) {
        return code != null && code.trim().matches("\\d{" + CODE_LENGTH + "}");
    }

    private static boolean enUso(String codigo, Predicate<String> isUsed) {
        return isUsed != null && isUsed.test(codigo);
    }
}
