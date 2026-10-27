package com.prestape.report;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.prestape.dto.response.EquipmentListResponse;
import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;

@Component
public class EquipmentExcelReport {

    private static final String SHEET_NAME = "Equipos";
    private static final String FILE_PREFIX = "reporte_equipos_";
    private static final Map<EquipmentAvailability, String> AVAILABILITY_LABELS = Map.of(
            EquipmentAvailability.AVAILABLE, "Disponible",
            EquipmentAvailability.LOANED, "Prestado",
            EquipmentAvailability.IN_MAINTENANCE, "En mantenimiento",
            EquipmentAvailability.RETIRED, "De baja");
    private static final Map<EquipmentCondition, String> CONDITION_LABELS = Map.of(
            EquipmentCondition.GOOD, "Bueno",
            EquipmentCondition.WORN, "Desgastado",
            EquipmentCondition.DAMAGED, "Dañado");
    private static final List<ExcelColumn<EquipmentListResponse>> COLUMNS = List.of(
            new ExcelColumn<>("Código", EquipmentListResponse::code),
            new ExcelColumn<>("Nombre", EquipmentListResponse::name),
            new ExcelColumn<>("Serie", EquipmentListResponse::serialNumber),
            new ExcelColumn<>("Categoría", e -> e.category().name()),
            new ExcelColumn<>("Disponibilidad", e -> AVAILABILITY_LABELS.get(e.availability())),
            new ExcelColumn<>("Condición", e -> CONDITION_LABELS.get(e.condition())),
            new ExcelColumn<>("Notas de condición", EquipmentListResponse::conditionNotes),
            new ExcelColumn<>("Descripción", EquipmentListResponse::description),
            new ExcelColumn<>("Fecha de registro", EquipmentListResponse::createdAt)
    );

    private final ExcelReportWriter writer;

    public EquipmentExcelReport(ExcelReportWriter writer) {
        this.writer = writer;
    }

    public ReportFile generate(List<EquipmentListResponse> equipments) {
        return writer.write(SHEET_NAME, FILE_PREFIX, COLUMNS, equipments);
    }
}
