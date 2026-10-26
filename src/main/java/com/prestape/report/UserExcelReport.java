package com.prestape.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.prestape.config.ReportProperties;
import com.prestape.dto.response.PersonSummaryResponse;
import com.prestape.dto.response.UserListResponse;
import com.prestape.entity.enums.UserStatus;

@Component
public class UserExcelReport {

    private static final String SHEET_NAME = "Usuarios";
    private static final String FILE_PREFIX = "reporte_usuarios_";
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String DATE_FORMAT = "dd/mm/yyyy";
    private static final String[] HEADERS = {
            "N°", "Código", "Nombre completo", "DNI", "Celular", "Correo", "Rol", "Estado", "Fecha de registro"
    };

    private final ZoneId zoneId;

    public UserExcelReport(ReportProperties properties) {
        this.zoneId = properties.timezone();
    }

    public ReportFile generate(List<UserListResponse> users) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle dateStyle = dateStyle(workbook);

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < users.size(); i++) {
                writeRow(sheet.createRow(i + 1), i + 1, users.get(i), dateStyle);
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, Math.max(users.size(), 1), 0, HEADERS.length - 1));

            workbook.write(out);
            String filename = FILE_PREFIX + LocalDate.now(zoneId).format(FILE_DATE) + ".xlsx";
            return new ReportFile(filename, ReportFile.XLSX, out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el reporte de usuarios", e);
        }
    }

    private void writeRow(Row row, int number, UserListResponse user, CellStyle dateStyle) {
        PersonSummaryResponse person = user.person();

        row.createCell(0).setCellValue(number);
        row.createCell(1).setCellValue(person != null ? person.code() : "");
        row.createCell(2).setCellValue(person != null ? fullName(person) : "");
        row.createCell(3).setCellValue(person != null ? person.documentNumber() : "");
        row.createCell(4).setCellValue(person != null ? person.phone() : "");
        row.createCell(5).setCellValue(user.email());
        row.createCell(6).setCellValue(user.roles().stream()
                .map(role -> capitalize(role.externalName()))
                .collect(Collectors.joining(", ")));
        row.createCell(7).setCellValue(statusLabel(user));

        Cell createdAt = row.createCell(8);
        createdAt.setCellValue(LocalDateTime.ofInstant(user.createdAt(), zoneId));
        createdAt.setCellStyle(dateStyle);
    }

    private static String fullName(PersonSummaryResponse person) {
        return Stream.of(person.firstName(), person.middleName(), person.paternalSurname(), person.maternalSurname())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) return "";
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private static String statusLabel(UserListResponse user) {
        if (user.status() == UserStatus.SUSPENDED) return "Suspendido";
        return user.profileCompleted() ? "Activo" : "Perfil incompleto";
    }

    private static CellStyle headerStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private static CellStyle dateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(DATE_FORMAT));
        return style;
    }
}
