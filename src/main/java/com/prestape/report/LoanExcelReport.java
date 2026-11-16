package com.prestape.report;

import java.util.List;

import org.springframework.stereotype.Component;

import com.prestape.dto.response.LoanListResponse;
import com.prestape.dto.response.LoanListResponse.ItemInfo;
import com.prestape.dto.types.LoanStatusFilter;
import com.prestape.entity.enums.EquipmentCondition;
import com.prestape.report.configuration.ExcelColumnConfiguration;
import com.prestape.report.configuration.ExcelGroupedSheetConfiguration;
import com.prestape.report.configuration.ReportFileConfiguration;

@Component
public class LoanExcelReport {
    private final ExcelReportWriter writer;

    public LoanExcelReport(ExcelReportWriter writer) {
        this.writer = writer;
    }

    public ReportFileConfiguration generate(List<LoanListResponse> loans) {
        return writer.writeGrouped("Préstamos", "reporte_prestamos_", configuration(), loans);
    }

    private ExcelGroupedSheetConfiguration<LoanListResponse, ItemInfo> configuration() {
        return new ExcelGroupedSheetConfiguration<>(
                loanColumns(),
                itemColumns(),
                handoverColumns(),
                LoanListResponse::items);
    }

    private List<ExcelColumnConfiguration<LoanListResponse>> loanColumns() {
        return List.of(
                new ExcelColumnConfiguration<>("Código", LoanListResponse::code),
                new ExcelColumnConfiguration<>("Estado", loan -> LoanStatusFilter.valueOf(loan.status()).getLabel()),
                new ExcelColumnConfiguration<>("Solicitante", loan -> loan.requester().fullName()),
                new ExcelColumnConfiguration<>("Código solicitante", loan -> loan.requester().code()),
                new ExcelColumnConfiguration<>("Kit", loan -> loan.kit() != null ? loan.kit().name() : null),
                new ExcelColumnConfiguration<>("Cantidad de equipos", loan -> loan.items().size())
        );
    }

    private List<ExcelColumnConfiguration<ItemInfo>> itemColumns() {
        return List.of(
                new ExcelColumnConfiguration<>("Código equipo", ItemInfo::code),
                new ExcelColumnConfiguration<>("Nombre equipo", ItemInfo::name),
                new ExcelColumnConfiguration<>("Estado recojo", item -> conditionLabel(item.pickupCondition())),
                new ExcelColumnConfiguration<>("Estado devolución", item -> conditionLabel(item.returnCondition()))
        );
    }

    private List<ExcelColumnConfiguration<LoanListResponse>> handoverColumns() {
        return List.of(
                ExcelColumnConfiguration.dateTime("Recojo programado", LoanListResponse::scheduledPickupAt),
                ExcelColumnConfiguration.dateTime("Recojo registrado", LoanListResponse::pickedUpAt),
                new ExcelColumnConfiguration<>("Entregado por", LoanListResponse::deliveredBy),
                ExcelColumnConfiguration.dateTime("Devolución programada", LoanListResponse::scheduledReturnAt),
                ExcelColumnConfiguration.dateTime("Devolución registrada", LoanListResponse::returnedAt),
                new ExcelColumnConfiguration<>("Recibido por", LoanListResponse::receivedBy),
                ExcelColumnConfiguration.dateTime("Fecha de cancelación", LoanListResponse::cancelledAt),
                new ExcelColumnConfiguration<>("Registrado por", LoanListResponse::registeredBy),
                new ExcelColumnConfiguration<>("Observaciones", LoanListResponse::notes),
                ExcelColumnConfiguration.dateTime("Fecha de registro", LoanListResponse::createdAt)
        );
    }

    private String conditionLabel(EquipmentCondition condition) {
        return condition != null ? condition.getLabel() : null;
    }
}
