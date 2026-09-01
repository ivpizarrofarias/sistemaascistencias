package cl.ipfsoft.backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del util de validación y normalización del RUT chileno.
 * El RUT se valida con el algoritmo oficial (módulo 11) del SII.
 */
class RutUtilsTest {

    @Test
    void rutValidoConGuion_seAcepta() {
        // 11111111-1 es un RUT de prueba estándar y válido.
        assertTrue(RutUtils.isValid("11111111-1"));
    }

    @Test
    void rutValidoSinGuion_seAcepta() {
        assertTrue(RutUtils.isValid("111111111"));
    }

    @Test
    void rutValidoConK_seAcepta() {
        // RUT cuyo dígito verificador es K.
        assertTrue(RutUtils.isValid("21365487-K"));
        assertTrue(RutUtils.isValid("7600000-K"));
    }

    @Test
    void rutIncorrecto_seRechaza() {
        // Cambiamos el dígito verificador -> debe ser inválido.
        assertFalse(RutUtils.isValid("11111111-2"));
    }

    @Test
    void formatoInvalido_seRechaza() {
        assertFalse(RutUtils.isValid("123"));
        assertFalse(RutUtils.isValid("abc-def"));
        assertFalse(RutUtils.isValid(null));
        assertFalse(RutUtils.isValid(""));
    }

    @Test
    void normalize_formateaCorrectamente() {
        assertEquals("11111111-1", RutUtils.normalize("11111111-1"));
        assertEquals("11111111-1", RutUtils.normalize("11.111.111-1"));
    }

    @Test
    void format_agregaPuntosDeMiles() {
        assertEquals("15.560.647-9", RutUtils.format("15560647-9"));
        assertEquals("11.111.111-1", RutUtils.format("11111111-1"));
        assertEquals("7.600.000-K", RutUtils.format("7600000-K"));
    }

    @Test
    void format_idempotente() {
        assertEquals("15.560.647-9", RutUtils.format("15.560.647-9"));
    }

    @Test
    void format_nullOVacio_devuelveOriginal() {
        assertEquals(null, RutUtils.format(null));
        assertEquals("", RutUtils.format(""));
        assertEquals("abc", RutUtils.format("abc"));
    }

    @Test
    void normalize_rutInvalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> RutUtils.normalize("11111111-9"));
    }
}
