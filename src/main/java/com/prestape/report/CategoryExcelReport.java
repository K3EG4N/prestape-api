package com.prestape.report;

import java.util.List;

import org.springframework.stereotype.Component;

import com.prestape.dto.response.EquipmentCategoryListResponse;
import com.prestape.report.configuration.ExcelColumnConfiguration;
import com.prestape.report.configuration.ReportFileConfiguration;

@Component
public class CategoryExcelReport {
    private final ExcelReportWriter writer;

    public CategoryExcelReport(ExcelReportWriter writer) {
        this.writer = writer;
    }

    public ReportFileConfiguration generate(List<EquipmentCategoryListResponse> categories) {
        return writer.write("Categorías", "reporte_categorias_", columns(), categories);
    }

    private List<ExcelColumnConfiguration<EquipmentCategoryListResponse>> columns() {
        return List.of(
                new ExcelColumnConfiguration<>("Nombre", EquipmentCategoryListResponse::name),
                new ExcelColumnConfiguration<>("Descripción", EquipmentCategoryListResponse::description),
                new ExcelColumnConfiguration<>("Cantidad de equipos", EquipmentCategoryListResponse::equipmentCount)
        );
    }
}
