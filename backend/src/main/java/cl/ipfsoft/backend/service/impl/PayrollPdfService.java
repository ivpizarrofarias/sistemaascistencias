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
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font smallBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);

        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ========== ENCABEZADO: EMPRESA Y PERÍODO ==========
            Paragraph empresa = new Paragraph(companyName, titleFont);
            empresa.setAlignment(Element.ALIGN_CENTER);
            doc.add(empresa);

            Paragraph rutEmpresa = new Paragraph("RUT: " + companyRut, normalFont);
            rutEmpresa.setAlignment(Element.ALIGN_CENTER);
            doc.add(rutEmpresa);

            Paragraph titulo = new Paragraph("LIQUIDACIÓN DE SUELDO", headerFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);

            Paragraph periodo = new Paragraph("Período: " + nombreMes(l.getPeriodYear(), l.getPeriodMonth()), boldFont);
            periodo.setAlignment(Element.ALIGN_CENTER);
            doc.add(periodo);

            doc.add(new Paragraph(" ", normalFont));

            // ========== DATOS DEL TRABAJADOR ==========
            PdfPTable datosTrabajador = new PdfPTable(4);
            datosTrabajador.setWidthPercentage(100);
            datosTrabajador.setWidths(new float[]{2f, 2f, 2f, 2f});
            datosTrabajador.setSpacingAfter(10);

            addCell(datosTrabajador, "Trabajador:", l.getEmployee().getFullName(), boldFont, normalFont);
            addCell(datosTrabajador, "RUT:", RutUtils.format(l.getEmployee().getRut()), boldFont, normalFont);
            addCell(datosTrabajador, "Cargo:", l.getEmployee().getRolEconomico() != null ? l.getEmployee().getRolEconomico() : "-", boldFont, normalFont);
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
            doc.add(new Paragraph(" ", normalFont));

            // ========== HABERES ==========
            addSectionTitle(doc, "HABERES", headerFont);
            PdfPTable haberesTable = new PdfPTable(3);
            haberesTable.setWidthPercentage(100);
            haberesTable.setWidths(new float[]{5f, 1f, 2f});
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
            descuentosTable.setWidths(new float[]{5f, 1f, 2f});
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
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12), 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            doc.add(resumenTable);

            doc.add(new Paragraph(" ", normalFont));
            doc.add(new Paragraph("Documento generado por " + companyName, smallFont));

            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF de la liquidación", e);
        }
        return out.toByteArray();
    }

    private void addSectionTitle(Document doc, String title, Font font) throws DocumentException {
        Paragraph p = new Paragraph(title, font);
        p.setSpacingBefore(8);
        p.setSpacingAfter(4);
        doc.add(p);
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
        c1.setPadding(6);
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont));
        c2.setBorder(Rectangle.BOX);
        c2.setPadding(6);
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
        c.setPadding(4);
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
}