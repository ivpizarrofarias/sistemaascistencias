package cl.ipfsoft.backend.util;

import java.text.Normalizer;
import java.util.function.Predicate;

/**
 * Genera el código de marcaje de los empleados.
 *
 * El empleado no tiene usuario ni contraseña: su credencial es un CÓDIGO
 * legible y fácil de recordar, formado por:
 *   - los primeros 4 dígitos del RUT, y
 *   - las primeras 4 LETRAS del nombre de pila (si el nombre tiene menos
 *     de 4 letras se usan las que tenga).
 *
 * Ejemplo: RUT 1556XXXX-K + Ivan -> 1556IVAN.
 *
 * Como el código se deriva del RUT y del nombre, es posible que dos empleados
 * obtengan la misma base; en ese caso {@link #uniqueCode} agrega un sufijo
 * numérico (2, 3, ...) hasta lograr uno único en la tabla.
 */
public final class CodeGenerator {

    /** Cantidad de letras del nombre que se incluyen en el código. */
    private static final int NAME_LETTERS = 4;

    /** Primeros dígitos del RUT que se incluyen en el código. */
    private static final int RUT_PREFIX_DIGITS = 4;

    private CodeGenerator() {
    }

    /**
     * Construye el código base (sin garantizar unicidad):
     * primeros 4 dígitos del RUT + primeras 4 letras del nombre, en MAYÚSCULAS.
     */
    public static String baseCode(String rut, String firstName) {
        // Primeros 4 dígitos del RUT (se ignoran puntos, guiones y el dígito verificador).
        String digits = rut == null ? "" : rut.replaceAll("\\D", "");
        String prefix = digits.length() > RUT_PREFIX_DIGITS
                ? digits.substring(0, RUT_PREFIX_DIGITS)
                : digits;

        // Primeras 4 letras del nombre de pila (normalizado: sin acentos ni signos).
        String normalized = normalize(firstName == null ? "" : firstName);
        String namePart = normalized.length() > NAME_LETTERS
                ? normalized.substring(0, NAME_LETTERS)
                : normalized;

        return (prefix + namePart).toUpperCase();
    }

    /**
     * Genera un código de marcaje único: parte del código base y, si ya está en
     * uso, agrega un sufijo numérico (1556IVAN, 1556IVAN2, ...) hasta encontrar uno libre.
     *
     * @param isUsed  función que indica si el código ya está registrado.
     */
    public static String uniqueCode(String rut, String firstName, Predicate<String> isUsed) {
        String base = baseCode(rut, firstName);
        String code = base;
        int suffix = 2;
        while (isUsed != null && isUsed.test(code)) {
            code = base + suffix++;
        }
        return code;
    }

    /** Quita tildes y caracteres que no son letras, dejando el texto en minúsculas. */
    private static String normalize(String s) {
        String nfd = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD);
        // Se eliminan las marcas combinantes (tildes) y todo lo que no sea a-z o ñ.
        return nfd.replaceAll("\\p{M}", "").toLowerCase().replaceAll("[^a-zñ]", "");
    }
}
