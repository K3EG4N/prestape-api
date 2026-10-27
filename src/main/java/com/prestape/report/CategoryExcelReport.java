package com.prestape.report;

import java.util.List;

import org.springframework.stereotype.Component;

import com.prestape.dto.response.EquipmentCategoryListResponse;

@Component
public class CategoryExcelReport {

    private static final String SHEET_NAME = "Categorías";
    private static final String FILE_PREFIX = "reporte_categorias_";
    private static final List<ExcelColumn<EquipmentCategoryListResponse>> COLUMNS = List.of(
            new ExcelColumn<>("Nombre", EquipmentCategoryListResponse::name),
            new ExcelColumn<>("Descripción", EquipmentCategoryListResponse::description),
            new ExcelColumn<>("Cantidad de equipos", EquipmentCategoryListResponse::equipmentCount)
    );

    private final ExcelReportWriter writer;

    public CategoryExcelReport(ExcelReportWriter writer) {
        this.writer = writer;
    }

    public ReportFile generate(List<EquipmentCategoryListResponse> categories) {
        return writer.write(SHEET_NAME, FILE_PREFIX, COLUMNS, categories);
    }
}
