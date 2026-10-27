package com.prestape.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

@Component
public class ExcelReportWriter {

    private static final String NUMBER_HEADER = "N°";
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String DATE_FORMAT = "dd/mm/yyyy";

    private final ZoneId zoneId;

    public ExcelReportWriter(ReportProperties properties) {
        this.zoneId = properties.timezone();
    }

    public <T> ReportFile write(String sheetName, String filePrefix, List<ExcelColumn<T>> columns, List<T> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle dateStyle = dateStyle(workbook);
            int totalColumns = columns.size() + 1;

            Row header = sheet.createRow(0);
            writeHeader(header, 0, NUMBER_HEADER, headerStyle);
            for (int i = 0; i < columns.size(); i++) {
                writeHeader(header, i + 1, columns.get(i).header(), headerStyle);
            }

            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                row.createCell(0).setCellValue(r + 1);
                for (int c = 0; c < columns.size(); c++) {
                    writeValue(row.createCell(c + 1), columns.get(c).value().apply(rows.get(r)), dateStyle);
                }
            }

            for (int i = 0; i < totalColumns; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, Math.max(rows.size(), 1), 0, totalColumns - 1));

            workbook.write(out);
            String filename = filePrefix + LocalDate.now(zoneId).format(FILE_DATE) + ".xlsx";
            return new ReportFile(filename, ReportFile.XLSX, out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el reporte de " + sheetName.toLowerCase(), e);
        }
    }

    private static void writeHeader(Row row, int index, String value, CellStyle style) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void writeValue(Cell cell, Object value, CellStyle dateStyle) {
        switch (value) {
            case null -> cell.setCellValue("");
            case Number number -> cell.setCellValue(number.doubleValue());
            case Instant instant -> {
                cell.setCellValue(LocalDateTime.ofInstant(instant, zoneId));
                cell.setCellStyle(dateStyle);
            }
            default -> cell.setCellValue(value.toString());
        }
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
