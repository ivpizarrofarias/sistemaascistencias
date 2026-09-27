package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.entity.DiaHorario;
import cl.ipfsoft.backend.entity.HorarioSemanal;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio que genera el PDF de un horario de trabajo semanal de un empleado.
 *
 * <p>El documento incluye el encabezado con los datos de la empresa (nombre y
 * RUT), la semana a la que corresponde el horario, los datos del trabajador y
 * una tabla día por día (lunes a sábado) con su fecha concreta, si se trabaja
 * o no, y las horas de entrada y salida.</p>
 */
@Service
public class SchedulePdfService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String[] DIAS = {"LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO"};

    private final String companyName;
    private final String companyRut;

    public SchedulePdfService(
            @Value("${app.company.name}") String companyName,
            @Value("${app.company.rut}") String companyRut) {
        this.companyName = companyName;
        this.companyRut = companyRut;
    }

    /** Genera el PDF (bytes) del horario semanal indicado. */
    public byte[] generatePdf(HorarioSemanal ws) {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ---------- Encabezado: empresa y semana ----------
            Paragraph empresa = new Paragraph(companyName, titleFont);
            empresa.setAlignment(Element.ALIGN_CENTER);
            doc.add(empresa);

            Paragraph rutEmpresa = new Paragraph("RUT: " + companyRut, normalFont);
            rutEmpresa.setAlignment(Element.ALIGN_CENTER);
            doc.add(rutEmpresa);

            Paragraph titulo = new Paragraph("HORARIO DE TRABAJO SEMANAL", boldFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);

            LocalDate inicio = ws.getStartDate();
            LocalDate fin = inicio.plusDays(5);
            Paragraph semana = new Paragraph("Semana del " + FECHA.format(inicio) + " al " + FECHA.format(fin), boldFont);
            semana.setAlignment(Element.ALIGN_CENTER);
            doc.add(semana);

            doc.add(new Paragraph(" ", normalFont));

            // ---------- Datos del trabajador ----------
            PdfPTable datosTrabajador = new PdfPTable(2);
            datosTrabajador.setWidthPercentage(100);
            celda(datosTrabajador, "Trabajador: " + ws.getEmployee().getFullName(), smallFont, Rectangle.LEFT);
            celda(datosTrabajador, "RUT: " + RutUtils.format(ws.getEmployee().getRut()), smallFont, Rectangle.LEFT);
            celda(datosTrabajador, "Código: " + ws.getEmployee().getCode(), smallFont, Rectangle.LEFT);
            celda(datosTrabajador, ws.getLabel() == null || ws.getLabel().isBlank()
                    ? "Etiqueta: " + FECHA.format(inicio)
                    : "Etiqueta: " + ws.getLabel(), smallFont, Rectangle.LEFT);
            doc.add(datosTrabajador);

            doc.add(new Paragraph(" ", normalFont));

            // ---------- Tabla día por día ----------
            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{1.5f, 1.5f, 1f, 1.5f, 1.5f});
            celda(tabla, "DÍA", boldFont, Rectangle.BOX);
            celda(tabla, "FECHA", boldFont, Rectangle.BOX);
            celda(tabla, "TRABAJA", boldFont, Rectangle.BOX);
            celda(tabla, "ENTRADA", boldFont, Rectangle.BOX);
            celda(tabla, "SALIDA", boldFont, Rectangle.BOX);

            List<DiaHorario> dias = ws.getDays();
            for (int i = 0; i < 6; i++) {
                int dayNumber = i + 1;
                LocalDate fecha = inicio.plusDays(i);
                DiaHorario day = diaDe(dias, dayNumber);
                boolean trabajado = day != null && day.isWorked();
                celda(tabla, DIAS[i], normalFont, Rectangle.BOX);
                celda(tabla, FECHA.format(fecha), normalFont, Rectangle.BOX);
                celda(tabla, trabajado ? "Sí" : "No", normalFont, Rectangle.BOX);
                if (trabajado && day.getStartTime() != null && day.getEndTime() != null) {
                    celda(tabla, hhmm(day.getStartTime().getHour(), day.getStartTime().getMinute()), normalFont, Rectangle.BOX);
                    celda(tabla, hhmm(day.getEndTime().getHour(), day.getEndTime().getMinute()), normalFont, Rectangle.BOX);
                } else {
                    celda(tabla, "—", normalFont, Rectangle.BOX);
                    celda(tabla, "—", normalFont, Rectangle.BOX);
                }
            }

            doc.add(tabla);

            doc.add(new Paragraph(" ", normalFont));
            doc.add(new Paragraph("Documento generado por " + companyName, smallFont));

            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF del horario", e);
        }
        return out.toByteArray();
    }

    /** Busca el día (por dayNumber) dentro del listado de días del horario. */
    private DiaHorario diaDe(List<DiaHorario> dias, int dayNumber) {
        if (dias == null) {
            return null;
        }
        return dias.stream().filter(d -> d.getDayNumber() == dayNumber).findFirst().orElse(null);
    }

    /** Formatea una hora como HH:mm. */
    private String hhmm(int h, int m) {
        return String.format("%02d:%02d", h, m);
    }

    private void celda(PdfPTable tabla, String texto, Font fuente, int border) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fuente));
        c.setBorder(border);
        c.setPaddingTop(4);
        c.setPaddingBottom(4);
        c.setPaddingLeft(6);
        c.setPaddingRight(6);
        tabla.addCell(c);
    }
}