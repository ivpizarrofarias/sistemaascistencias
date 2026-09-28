package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.entity.Liquidacion;
import cl.ipfsoft.backend.entity.LiquidacionDetalle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El PDF de la liquidación es un documento que el trabajador recibe, revisa y
 * firma: tiene que imprimirse en una sola hoja, aprovechar el papel (no dejar
 * media hoja en blanco) y traer la firma y los descuentos correctos.
 */
@SpringBootTest
class PayrollPdfServiceTest {

    private static final float ALTO_A4 = 842f;

    @Autowired
    PayrollPdfService pdfService;

    private Liquidacion liquidacion;

    @BeforeEach
    void setUp() {
        Empleado e = new Empleado();
        e.setId(1L);
        e.setRut("16876543-6");
        e.setFirstName("Javiera");
        e.setLastName("Muñoz");
        e.setHireDate(LocalDate.of(2023, 5, 2));
        e.setDependents(1);
        e.setAfpFund("HABITAT");
        e.setPensionSystem("AFP");
        e.setHealthSystem("FONASA");
        e.setRolEconomico("9");

        liquidacion = new Liquidacion();
        liquidacion.setEmployee(e);
        liquidacion.setPeriodYear(2026);
        liquidacion.setPeriodMonth(9);
        liquidacion.setDiasMes(30);
        liquidacion.setDiasHabiles(22);
        liquidacion.setSueldoBase(new BigDecimal("980000"));
        liquidacion.setTotalHaberes(new BigDecimal("1141246"));
        liquidacion.setTotalDescuentos(new BigDecimal("314465"));
        liquidacion.setLiquidoAPagar(new BigDecimal("826781"));

        // Liquidación real de Javiera Muñoz: 5 haberes y 6 descuentos.
        agregarDetalle("Sueldo base", true, "980000");
        agregarDetalle("Gratificación legal", true, "86864");
        agregarDetalle("Asignación familiar (1 cargas)", true, "4382");
        agregarDetalle("Bono de movilización", true, "30000");
        agregarDetalle("Horas extra (4 h)", true, "40000");
        agregarDetalle("AFP (HABITAT 10%)", false, "110686");
        agregarDetalle("Salud (7%)", false, "77480");
        agregarDetalle("Cesantía (trabajador)", false, "6641");
        agregarDetalle("Impuesto Único 2ª categoría", false, "568");
        agregarDetalle("Descuento por inasistencia (1 días)", false, "44545");
        agregarDetalle("Anticipo de sueldo", false, "30000");
    }

    @Test
    @DisplayName("La liquidación completa se imprime en una sola hoja")
    void pdfCaeEnUnaSolaHoja() {
        assertEquals(1, paginas(pdfService.generatePdf(liquidacion)),
                "La liquidación debe imprimirse en una sola hoja");
    }

    @Test
    @DisplayName("El PDF aprovecha la hoja en vez de dejarla en blanco")
    void pdfOcupaLaHoja() {
        byte[] pdf = pdfService.generatePdf(liquidacion);
        float ocupacion = ocupacionVertical(pdf);

        assertTrue(ocupacion >= 80f,
                "El contenido debería ocupar al menos el 80% del alto de la hoja, ocupa " + ocupacion + "%");
    }

    @Test
    @DisplayName("El PDF incluye la firma del trabajador con su nombre y RUT")
    void pdfIncluyeFirmaDelTrabajador() {
        String texto = texto(pdfService.generatePdf(liquidacion));

        assertTrue(texto.contains("Firma del trabajador"), "Debe pedir la firma del trabajador");
        assertTrue(texto.contains("Javiera Muñoz"), "Debe imprimir el nombre del trabajador");
        assertTrue(texto.contains("16.876.543-6"), "Debe imprimir el RUT del trabajador");
    }

    @Test
    @DisplayName("El anticipo aparece solo como descuento, sin sección aparte")
    void pdfMuestraElAnticipoComoDescuento() {
        String texto = texto(pdfService.generatePdf(liquidacion));

        assertTrue(texto.contains("Anticipo de sueldo"), "El anticipo debe aparecer en el PDF");
        assertEquals(1, ocurrencias(texto, "Anticipo de sueldo"),
                "El anticipo no debe repetirse en una sección resumen");
        assertFalse(texto.contains("ANTICIPOS DESCONTADOS"),
                "No debe existir una sección separada de anticipos");
        assertFalse(texto.contains("Documento generado por"),
                "No debe figurar el piegeneratedor en un documento que firma el trabajador");
    }

    @Test
    @DisplayName("Aun con conceptos adicionales sigue entrando en una hoja")
    void pdfCaeEnUnaSolaHojaConConceptosAdicionales() {
        for (int i = 1; i <= 2; i++) {
            agregarDetalle("Haber adicional " + i, true, "1000");
        }

        assertEquals(1, paginas(pdfService.generatePdf(liquidacion)),
                "Una liquidación con 14 conceptos aún debe caber en una hoja");
    }

    private void agregarDetalle(String concepto, boolean haber, String monto) {
        LiquidacionDetalle d = new LiquidacionDetalle();
        d.setConcepto(concepto);
        d.setHaber(haber);
        d.setMonto(new BigDecimal(monto));
        liquidacion.getDetalles().add(d);
    }

    private int paginas(byte[] pdf) {
        Matcher m = Pattern.compile("/Count\\s+(\\d+)").matcher(new String(pdf, StandardCharsets.ISO_8859_1));
        assertTrue(m.find(), "El PDF debería tener un árbol de páginas legible");
        return Integer.parseInt(m.group(1));
    }

    /**
     * Porcentaje del alto de la hoja que ocupa el contenido, leído de las
     * posiciones absolutas (operador Tm) de los textos del PDF.
     */
    private float ocupacionVertical(byte[] pdf) {
        Matcher m = Pattern.compile("1 0 0 1 (-?[\\d.]+) (-?[\\d.]+) Tm")
                .matcher(textoCrudo(pdf));
        List<Float> ys = new ArrayList<>();
        while (m.find()) {
            ys.add(Float.parseFloat(m.group(2)));
        }
        assertFalse(ys.isEmpty(), "El PDF debería posicionar textos de forma absoluta");
        float max = ys.stream().max(Float::compareTo).orElseThrow();
        float min = ys.stream().min(Float::compareTo).orElseThrow();
        return (max - min) / ALTO_A4 * 100f;
    }

    private int ocurrencias(String texto, String aguja) {
        int n = 0;
        int i = texto.indexOf(aguja);
        while (i >= 0) {
            n++;
            i = texto.indexOf(aguja, i + aguja.length());
        }
        return n;
    }

    /** Texto del PDF: descomprime los streams y concatena lo que se dibuja. */
    private String texto(byte[] pdf) {
        return textoCrudo(pdf);
    }

    private String textoCrudo(byte[] pdf) {
        String iso = new String(pdf, StandardCharsets.ISO_8859_1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int i = 0;
        while (true) {
            int inicio = iso.indexOf("stream", i);
            if (inicio < 0) {
                break;
            }
            int desde = inicio + "stream".length();
            while (desde < iso.length() && (iso.charAt(desde) == '\r' || iso.charAt(desde) == '\n')) {
                desde++;
            }
            int hasta = iso.indexOf("endstream", desde);
            if (hasta < 0) {
                break;
            }
            Inflater inf = new Inflater();
            inf.setInput(iso.substring(desde, hasta).getBytes(StandardCharsets.ISO_8859_1));
            byte[] buffer = new byte[8192];
            try {
                int n;
                while ((n = inf.inflate(buffer)) > 0) {
                    out.write(buffer, 0, n);
                }
            } catch (DataFormatException ex) {
                // Stream que no es Flate (por ejemplo, una imagen): se ignora.
            } finally {
                inf.end();
            }
            i = hasta + "endstream".length();
        }
        return new String(out.toByteArray(), StandardCharsets.ISO_8859_1);
    }
}
