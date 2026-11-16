package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.entity.Liquidacion;
import cl.ipfsoft.backend.entity.LiquidacionDetalle;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import cl.ipfsoft.backend.util.RutUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Servicio que genera el PDF de una liquidación de sueldo.
 */
@Service
public class PayrollPdfService {

    private final String companyName;
    private final String companyRut;

    public PayrollPdfService(
            @Value("${app.company.name}") String companyName,
            @Value("${app.company.rut}") String companyRut) {
        this.companyName = companyName;
        this.companyRut = companyRut;
    }

    /** Genera el PDF (bytes) de la liquidación indicada. */
    public byte[] generatePdf(Liquidacion l) {
        // Cuerpos grandes y espaciado holgado: la liquidación es un documento
        // que se lee e imprime, así que se aprovecha toda la hoja en vez de
        // dejar media página en blanco.
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10.5f);
        Font smallBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);

        // Márgenes menores que los de A4 por defecto: dan más área útil sin
        // tocar el borde de impresión de una impresora normal.
        Document doc = new Document(PageSize.A4, 30, 30, 24, 24);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ========== ENCABEZADO: EMPRESA Y PERÍODO ==========
            // Empresa a la izquierda y título/período a la derecha, en una sola
            // fila: ocupa la mitad de alto que cuatro párrafos centrados.
            PdfPTable encabezado = new PdfPTable(2);
            encabezado.setWidthPercentage(100);
            encabezado.setWidths(new float[]{3f, 3f});
            encabezado.setSpacingAfter(12);

            PdfPCell celdaEmpresa = new PdfPCell();
            celdaEmpresa.setBorder(Rectangle.NO_BORDER);
            celdaEmpresa.setPadding(0);
            celdaEmpresa.addElement(new Paragraph(companyName, titleFont));
            celdaEmpresa.addElement(new Paragraph("RUT: " + companyRut, smallFont));
            encabezado.addCell(celdaEmpresa);

            PdfPCell celdaTitulo = new PdfPCell();
            celdaTitulo.setBorder(Rectangle.NO_BORDER);
            celdaTitulo.setPadding(0);
            Paragraph titulo = new Paragraph("LIQUIDACIÓN DE SUELDO", headerFont);
            titulo.setAlignment(Element.ALIGN_RIGHT);
            Paragraph periodo = new Paragraph(
                    "Período: " + nombreMes(l.getPeriodYear(), l.getPeriodMonth()), boldFont);
            periodo.setAlignment(Element.ALIGN_RIGHT);
            celdaTitulo.addElement(titulo);
            celdaTitulo.addElement(periodo);
            encabezado.addCell(celdaTitulo);

            doc.add(encabezado);

            // ========== DATOS DEL TRABAJADOR ==========
            PdfPTable datosTrabajador = new PdfPTable(4);
            datosTrabajador.setWidthPercentage(100);
            datosTrabajador.setWidths(new float[]{2f, 2f, 2f, 2f});
            datosTrabajador.setSpacingAfter(8);

            addCell(datosTrabajador, "Trabajador:", l.getEmployee().getFullName(), boldFont, normalFont);
            addCell(datosTrabajador, "RUT:", RutUtils.format(l.getEmployee().getRut()), boldFont, normalFont);
            addCell(datosTrabajador, "Cargo:", nombreCargo(l.getEmployee().getRolEconomico()), boldFont, normalFont);
            addCell(datosTrabajador, "Fecha Ingreso:", l.getEmployee().getHireDate() != null ? l.getEmployee().getHireDate().toString() : "-", boldFont, normalFont);

            addCell(datosTrabajador, "AFP:", l.getEmployee().getAfpFund() != null ? l.getEmployee().getAfpFund() + " (" + l.getEmployee().getPensionSystem() + ")" : "-", boldFont, normalFont);
            addCell(datosTrabajador, "Salud:", l.getEmployee().getHealthSystem() != null ? l.getEmployee().getHealthSystem() : "-", boldFont, normalFont);
            addCell(datosTrabajador, "Días mes:", String.valueOf(l.getDiasMes()), boldFont, normalFont);
            addCell(datosTrabajador, "Días trabajados:", String.valueOf(l.getDiasTrabajados()), boldFont, normalFont);

            addCell(datosTrabajador, "Inasistencias:", String.valueOf(l.getDiasInasistencia()), boldFont, normalFont);
            addCell(datosTrabajador, "Licencias:", String.valueOf(l.getDiasLicencia()), boldFont, normalFont);
            addCell(datosTrabajador, "Cargas:", String.valueOf(l.getEmployee().getDependents()), boldFont, normalFont);
            addCell(datosTrabajador, "Sueldo Base:", "$" + formato(l.getSueldoBase()), boldFont, normalFont);

            doc.add(datosTrabajador);

            // ========== HABERES ==========
            addSectionTitle(doc, "HABERES", headerFont);
            PdfPTable haberesTable = new PdfPTable(3);
            haberesTable.setWidthPercentage(100);
            haberesTable.setWidths(new float[]{4.3f, 1.7f, 2f});
            haberesTable.setSpacingAfter(8);

            addHeaderRow(haberesTable, "CONCEPTO", "TIPO", "MONTO", headerFont);

            boolean hasHaber = false;
            for (LiquidacionDetalle d : l.getDetalles()) {
                if (d.isHaber()) {
                    hasHaber = true;
                    addRow(haberesTable, d.getConcepto(), "HABER", "$" + formato(d.getMonto()), normalFont, smallFont);
                }
            }
            if (!hasHaber) {
                addRow(haberesTable, "Sin haberes", "", "", normalFont, smallFont);
            }
            doc.add(haberesTable);

            // ========== DESCUENTOS ==========
            addSectionTitle(doc, "DESCUENTOS", headerFont);
            PdfPTable descuentosTable = new PdfPTable(3);
            descuentosTable.setWidthPercentage(100);
            descuentosTable.setWidths(new float[]{4.3f, 1.7f, 2f});
            descuentosTable.setSpacingAfter(8);

            addHeaderRow(descuentosTable, "CONCEPTO", "TIPO", "MONTO", headerFont);

            boolean hasDescuento = false;
            for (LiquidacionDetalle d : l.getDetalles()) {
                if (!d.isHaber()) {
                    hasDescuento = true;
                    addRow(descuentosTable, d.getConcepto(), "DESCUENTO", "$" + formato(d.getMonto()), normalFont, smallFont);
                }
            }
            if (!hasDescuento) {
                addRow(descuentosTable, "Sin descuentos", "", "", normalFont, smallFont);
            }
            doc.add(descuentosTable);

            // ========== RESUMEN / TOTALES ==========
            addSectionTitle(doc, "RESUMEN", headerFont);
            PdfPTable resumenTable = new PdfPTable(2);
            resumenTable.setWidthPercentage(100);
            resumenTable.setWidths(new float[]{3f, 2f});
            resumenTable.setSpacingAfter(8);

            addResumenRow(resumenTable, "Total Haberes:", "$" + formato(l.getTotalHaberes()), boldFont, boldFont);
            addResumenRow(resumenTable, "Total Descuentos:", "$" + formato(l.getTotalDescuentos()), boldFont, boldFont);
            addResumenRow(resumenTable, "LÍQUIDO A PAGAR:", "$" + formato(l.getLiquidoAPagar()),
                    totalFont, totalFont);
            doc.add(resumenTable);

            // ========== FIRMA DEL TRABAJADOR ==========
            // El trabajador recibe y firma la liquidación; se deja el espacio
            // para la firma manuscrita con su nombre y RUT impresos.
            doc.add(signatureBlock(l, normalFont, smallBoldFont));

            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF de la liquidación", e);
        }
        return out.toByteArray();
    }

    private void addSectionTitle(Document doc, String title, Font font) throws DocumentException {
        Paragraph p = new Paragraph(title, font);
        p.setSpacingBefore(7);
        p.setSpacingAfter(3);
        doc.add(p);
    }

    /**
     * Bloque de firma del trabajador: una línea horizontal para la firma
     * manuscrita, con el nombre y el RUT impresos debajo.
     */
    private PdfPTable signatureBlock(Liquidacion l, Font normalFont, Font labelFont) {
        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(70);
        tabla.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.setSpacingBefore(24);
        tabla.setKeepTogether(true);

        // Espacio en blanco para la firma manuscrita.
        PdfPCell espacio = new PdfPCell(new Phrase(" ", normalFont));
        espacio.setBorder(Rectangle.NO_BORDER);
        espacio.setFixedHeight(70f);
        tabla.addCell(espacio);

        // Línea sobre la que se firma.
        PdfPCell linea = new PdfPCell(new Phrase(" ", normalFont));
        linea.setBorder(Rectangle.TOP);
        linea.setPaddingTop(0);
        tabla.addCell(linea);

        // Nombre del trabajador.
        PdfPCell nombre = new PdfPCell(new Phrase(l.getEmployee().getFullName(), normalFont));
        nombre.setBorder(Rectangle.NO_BORDER);
        nombre.setPaddingTop(5);
        nombre.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(nombre);

        // RUT y leyenda de la firma.
        PdfPCell leyenda = new PdfPCell(new Phrase(
                "Firma del trabajador · RUT: " + RutUtils.format(l.getEmployee().getRut()), labelFont));
        leyenda.setBorder(Rectangle.NO_BORDER);
        leyenda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(leyenda);

        return tabla;
    }


    private void addHeaderRow(PdfPTable table, String c1, String c2, String c3, Font font) {
        addCell(table, c1, font, Rectangle.BOX, Element.ALIGN_CENTER);
        addCell(table, c2, font, Rectangle.BOX, Element.ALIGN_CENTER);
        addCell(table, c3, font, Rectangle.BOX, Element.ALIGN_CENTER);
    }

    private void addRow(PdfPTable table, String c1, String c2, String c3, Font font1, Font font2) {
        addCell(table, c1, font1, Rectangle.BOX, Element.ALIGN_LEFT);
        addCell(table, c2, font2, Rectangle.BOX, Element.ALIGN_CENTER);
        addCell(table, c3, font2, Rectangle.BOX, Element.ALIGN_RIGHT);
    }

    private void addResumenRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBorder(Rectangle.BOX);
        c1.setPadding(5);
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont));
        c2.setBorder(Rectangle.BOX);
        c2.setPadding(5);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);
    }

    private void addCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(3);
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(3);
        c2.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(c2);
    }

    private void addCell(PdfPTable table, String text, Font font, int border, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(border);
        c.setPadding(3.5f);
        c.setHorizontalAlignment(align);
        table.addCell(c);
    }

    /** Devuelve el nombre del mes en español, formato "AGOSTO 2026". */
    private String nombreMes(int year, int month) {
        return YearMonth.of(year, month)
                .format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "CL")))
                .toUpperCase();
    }

    /** Formatea un monto sin decimales (CLP), en miles separados por punto. */
    private String formato(BigDecimal valor) {
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor.setScale(0, RoundingMode.HALF_UP);
        StringBuilder sb = new StringBuilder(v.toBigInteger().toString());
        int i = sb.length() - 3;
        while (i > 0) {
            sb.insert(i, '.');
            i -= 3;
        }
        return sb.toString();
    }

    /**
     * Muestra el nombre del cargo en lugar del código numérico (rol económico).
     */
    private String nombreCargo(String rolEconomico) {
        if (rolEconomico == null || rolEconomico.isBlank()) return "-";
        String codigo = rolEconomico.trim();
        return switch (codigo) {
            case "1" -> "Gerente / Administrador";
            case "2" -> "Profesional";
            case "3" -> "Técnico";
            case "4" -> "Administrativo";
            case "5" -> "Vendedor";
            case "6" -> "Operario calificado";
            case "7" -> "Operario";
            case "8" -> "Auxiliar";
            case "9" -> "Trabajador";
            default -> {
                // Si ya viene un nombre (no numérico), mostrarlo tal cual.
                if (!codigo.matches("\\d+")) yield codigo;
                yield "Cargo " + codigo;
            }
        };
    }
}