package com.prestape.report;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.prestape.dto.response.KitListResponse;
import com.prestape.report.configuration.ExcelColumnConfiguration;
import com.prestape.report.configuration.ReportFileConfiguration;

@Component
public class KitExcelReport {
    private final ExcelReportWriter writer;

    public KitExcelReport(ExcelReportWriter writer) {
        this.writer = writer;
    }

    public ReportFileConfiguration generate(List<KitListResponse> kits) {
        return writer.write("Kits", "reporte_kits_", columns(), kits);
    }

    private List<ExcelColumnConfiguration<KitListResponse>> columns() {
        return List.of(
                new ExcelColumnConfiguration<>("Código", KitListResponse::code),
                new ExcelColumnConfiguration<>("Nombre", KitListResponse::name),
                new ExcelColumnConfiguration<>("Disponibilidad", kit -> kit.availability().getLabel()),
                new ExcelColumnConfiguration<>("Cantidad de equipos", kit -> kit.items().size()),
                new ExcelColumnConfiguration<>("Equipos", this::equipmentSummary),
                new ExcelColumnConfiguration<>("Descripción", KitListResponse::description),
                new ExcelColumnConfiguration<>("Fecha de registro", KitListResponse::createdAt)
        );
    }

    private String equipmentSummary(KitListResponse kit) {
        return kit.items().stream()
                .map(item -> item.code() + " - " + item.name())
                .collect(Collectors.joining(", "));
    }
}