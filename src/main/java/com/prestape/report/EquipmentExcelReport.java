package com.prestape.report;

import java.util.List;

import org.springframework.stereotype.Component;

import com.prestape.dto.response.EquipmentListResponse;
import com.prestape.report.configuration.ExcelColumnConfiguration;
import com.prestape.report.configuration.ReportFileConfiguration;

@Component
public class EquipmentExcelReport {
    private final ExcelReportWriter writer;

    public EquipmentExcelReport(ExcelReportWriter writer) {
        this.writer = writer;
    }

    public ReportFileConfiguration generate(List<EquipmentListResponse> equipments) {
        return writer.write("Equipos", "reporte_equipos_", columns(), equipments);
    }

    private List<ExcelColumnConfiguration<EquipmentListResponse>> columns() {
        return List.of(
                new ExcelColumnConfiguration<>("Código", EquipmentListResponse::code),
                new ExcelColumnConfiguration<>("Nombre", EquipmentListResponse::name),
                new ExcelColumnConfiguration<>("Serie", EquipmentListResponse::serialNumber),
                new ExcelColumnConfiguration<>("Categoría", equipment -> equipment.category().name()),
                new ExcelColumnConfiguration<>("Disponibilidad", equipment -> equipment.availability().getLabel()),
                new ExcelColumnConfiguration<>("Condición", equipment -> equipment.condition().getLabel()),
                new ExcelColumnConfiguration<>("Notas de condición", EquipmentListResponse::conditionNotes),
                new ExcelColumnConfiguration<>("Descripción", EquipmentListResponse::description),
                new ExcelColumnConfiguration<>("Fecha de registro", EquipmentListResponse::createdAt)
        );
    }
}
