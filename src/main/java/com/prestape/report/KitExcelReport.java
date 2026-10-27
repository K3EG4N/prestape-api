package com.prestape.report;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.prestape.dto.response.KitListResponse;
import com.prestape.entity.enums.KitAvailability;

@Component
public class KitExcelReport {

    private static final String SHEET_NAME = "Kits";
    private static final String FILE_PREFIX = "reporte_kits_";
    private static final Map<KitAvailability, String> AVAILABILITY_LABELS = Map.of(
            KitAvailability.AVAILABLE, "Disponible",
            KitAvailability.UNAVAILABLE, "No disponible",
            KitAvailability.EMPTY, "Sin equipos");
    private static final List<ExcelColumn<KitListResponse>> COLUMNS = List.of(
            new ExcelColumn<>("Código", KitListResponse::code),
            new ExcelColumn<>("Nombre", KitListResponse::name),
            new ExcelColumn<>("Disponibilidad", k -> AVAILABILITY_LABELS.get(k.availability())),
            new ExcelColumn<>("Cantidad de equipos", k -> k.items().size()),
            new ExcelColumn<>("Equipos", k -> k.items().stream()
                    .map(item -> item.code() + " - " + item.name())
                    .collect(Collectors.joining(", "))),
            new ExcelColumn<>("Descripción", KitListResponse::description),
            new ExcelColumn<>("Fecha de registro", KitListResponse::createdAt)
    );

    private final ExcelReportWriter writer;

    public KitExcelReport(ExcelReportWriter writer) {
        this.writer = writer;
    }

    public ReportFile generate(List<KitListResponse> kits) {
        return writer.write(SHEET_NAME, FILE_PREFIX, COLUMNS, kits);
    }
}
