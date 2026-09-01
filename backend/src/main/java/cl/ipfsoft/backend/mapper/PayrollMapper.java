package cl.ipfsoft.backend.mapper;

import cl.ipfsoft.backend.dto.payroll.AdvanceRequest;
import cl.ipfsoft.backend.dto.payroll.AdvanceResponse;
import cl.ipfsoft.backend.dto.payroll.HoraExtraRequest;
import cl.ipfsoft.backend.dto.payroll.HoraExtraResponse;
import cl.ipfsoft.backend.dto.payroll.LicenciaRequest;
import cl.ipfsoft.backend.dto.payroll.LicenciaResponse;
import cl.ipfsoft.backend.dto.payroll.LiquidacionDetalleDto;
import cl.ipfsoft.backend.dto.payroll.LiquidacionResponse;
import cl.ipfsoft.backend.entity.Advance;
import cl.ipfsoft.backend.entity.HoraExtra;
import cl.ipfsoft.backend.entity.LicenciaMedica;
import cl.ipfsoft.backend.entity.Liquidacion;
import cl.ipfsoft.backend.entity.LiquidacionDetalle;
import cl.ipfsoft.backend.util.RutUtils;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class PayrollMapper {

    public AdvanceResponse toAdvanceResponse(Advance a) {
        return new AdvanceResponse(a.getId(), a.getEmployee().getId(), a.getDate(), a.getAmount(), a.getDescription());
    }

    public Advance applyAdvanceRequest(Advance a, AdvanceRequest r) {
        a.setDate(r.date() != null ? r.date() : java.time.LocalDate.now());
        a.setAmount(r.amount());
        a.setDescription(r.description());
        return a;
    }

    public HoraExtraResponse toHoraExtraResponse(HoraExtra h) {
        return new HoraExtraResponse(h.getId(), h.getEmployee().getId(), h.getDate(), h.getHoras());
    }

    public HoraExtra applyHoraExtraRequest(HoraExtra h, HoraExtraRequest r) {
        h.setDate(r.date());
        h.setHoras(r.horas());
        return h;
    }

    public LicenciaResponse toLicenciaResponse(LicenciaMedica l) {
        int totalDays = (int) ChronoUnit.DAYS.between(l.getStartDate(), l.getEndDate()) + 1;
        return new LicenciaResponse(l.getId(), l.getEmployee().getId(), l.getStartDate(), l.getEndDate(),
                totalDays, l.getMotivo(), l.isEmployerPays());
    }

    public LicenciaMedica applyLicenciaRequest(LicenciaMedica l, LicenciaRequest r) {
        l.setStartDate(r.startDate());
        l.setEndDate(r.endDate());
        l.setMotivo(r.motivo());
        l.setEmployerPays(r.employerPays());
        return l;
    }

    public LiquidacionResponse toLiquidacionResponse(Liquidacion l) {
        List<LiquidacionDetalleDto> detalles = l.getDetalles().stream()
                .map(d -> new LiquidacionDetalleDto(d.getConcepto(), d.isHaber(), d.getMonto()))
                .toList();
        return new LiquidacionResponse(
                l.getId(),
                l.getEmployee().getId(),
                l.getEmployee().getFullName(),
                RutUtils.format(l.getEmployee().getRut()),
                l.getPeriodYear(),
                l.getPeriodMonth(),
                l.getDiasMes(),
                l.getDiasHabiles(),
                l.getDiasTrabajados(),
                l.getDiasInasistencia(),
                l.getDiasLicencia(),
                l.getSueldoBase(),
                l.getGratificacion(),
                l.getAsignacionFamiliar(),
                l.getBonoMovilizacion(),
                l.getSubsidioLicenciaEmpleador(),
                l.getHorasExtras(),
                l.getMontoHorasExtras(),
                l.getOtrosHaberes(),
                l.getBaseImponible(),
                l.getTotalHaberes(),
                l.getAfp(),
                l.getSis(),
                l.getSalud(),
                l.getImpuestoUnico(),
                l.getCesantia(),
                l.getDescuentoFaltas(),
                l.getDescuentoLicenciaDias(),
                l.getDescuentoLicencia(),
                l.getAnticipos(),
                l.getDescuentoHorasExtras(),
                l.getOtrosDescuentos(),
                l.getTotalDescuentos(),
                l.getLiquidoAPagar(),
                l.isPagada(),
                detalles
        );
    }
}
