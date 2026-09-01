package cl.ipfsoft.backend.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del generador de código de marcaje.
 *
 * El código base está formado por los 4 primeros dígitos del RUT + las primeras
 * 4 letras del nombre de pila (normalizado, sin acentos), en MAYÚSCULAS.
 */
class CodeGeneratorTest {

    @Test
    void baseCode_rutYNombre_componentesCorrectos() {
        // RUT 12345678-5 -> "1234"; FRANCISCA -> primeras 4 letras "FRAN" => "1234FRAN".
        assertEquals("1234FRAN", CodeGenerator.baseCode("12345678-5", "FRANCISCA"));
    }

    @Test
    void baseCode_nombreDeCuatroLetras() {
        // JUAN tiene exactamente 4 letras -> se usa el nombre completo.
        assertEquals("1111JUAN", CodeGenerator.baseCode("11111111-1", "Juan"));
    }

    @Test
    void baseCode_nombreLargo_cortaEnCuatroLetras() {
        // VERONICA -> "VERO" (primeras 4 letras).
        assertEquals("1234VERO", CodeGenerator.baseCode("12345678-5", "Veronica"));
        // SEBASTIAN -> "SEBA".
        assertEquals("1234SEBA", CodeGenerator.baseCode("12345678-5", "Sebastian"));
    }

    @Test
    void baseCode_ignoraPuntosGuionesYDv() {
        // El RUT puede venir con puntos y guion; solo se toman los primeros 4 dígitos.
        assertEquals("1111JUAN", CodeGenerator.baseCode("11.111.111-1", "Juan"));
    }

    @Test
    void baseCode_sinAcentos_TildesIgnoradas() {
        // MARIA (con tilde) se normaliza -> "MARI".
        assertEquals("1234MARI", CodeGenerator.baseCode("12345678-5", "María"));
    }

    @Test
    void baseCode_soloDigitosDelRut_sinPuntosNiDv() {
        // Se toman solo los primeros 4 dígitos (ignorando puntos, guiones y el DV).
        assertEquals("1234JUAN", CodeGenerator.baseCode("12.345.678-5", "Juan"));
        assertEquals("9999JUAN", CodeGenerator.baseCode("99999999-K", "Juan"));
    }

    @Test
    void baseCode_inputMinusculas_devuelveMayusculas() {
        // El código final siempre va en MAYÚSCULAS aunque se ingrese en minúsculas.
        assertEquals("1556IVAN", CodeGenerator.baseCode("1556XXXX-K", "ivan"));
    }

    @Test
    void uniqueCode_sinColision_devuelveBase() {
        assertEquals("1111JUAN", CodeGenerator.uniqueCode("11111111-1", "Juan", s -> false));
    }

    @Test
    void uniqueCode_conColision_agregaSufijo() {
        // El código base ya está en uso -> se agrega sufijo 2, luego 3, etc.
        assertEquals("1111JUAN2", CodeGenerator.uniqueCode("11111111-1", "Juan", s -> s.equals("1111JUAN")));
        assertEquals("1111JUAN3",
                CodeGenerator.uniqueCode("11111111-1", "Juan", s -> s.equals("1111JUAN") || s.equals("1111JUAN2")));
    }
}
