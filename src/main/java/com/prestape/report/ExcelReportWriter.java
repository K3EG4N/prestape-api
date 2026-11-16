package com.prestape.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.prestape.report.configuration.ExcelColumnConfiguration;
import com.prestape.report.configuration.ExcelGroupedSheetConfiguration;
import com.prestape.report.configuration.ReportFileConfiguration;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
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

    public <T> ReportFileConfiguration write(String sheetName, String filePrefix, List<ExcelColumnConfiguration<T>> columns, List<T> rows) {
        return build(sheetName, filePrefix, columns.size() + 1, (sheet) -> {
            Styles styles = new Styles(sheet.getWorkbook(), false);
            writeHeaders(sheet, columns.stream().map(ExcelColumnConfiguration::header).toList());

            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                row.createCell(0).setCellValue(r + 1);
                writeColumns(row, 1, columns, rows.get(r), styles);
            }
            return rows.size();
        });
    }

    public <T, D> ReportFileConfiguration writeGrouped(
            String sheetName, String filePrefix, ExcelGroupedSheetConfiguration<T, D> configuration, List<T> rows) {
        return build(sheetName, filePrefix, configuration.totalColumns() + 1, (sheet) -> {
            Styles groupStyles = new Styles(sheet.getWorkbook(), true);
            Styles detailStyles = new Styles(sheet.getWorkbook(), false);
            writeHeaders(sheet, Stream.of(
                            configuration.leadingColumns(), configuration.detailColumns(), configuration.trailingColumns())
                    .flatMap(columns -> columns.stream().map(ExcelColumnConfiguration::header))
                    .toList());

            int detailStart = configuration.leadingColumns().size() + 1;
            int trailingStart = detailStart + configuration.detailColumns().size();
            int lastColumn = trailingStart + configuration.trailingColumns().size() - 1;
            int rowIndex = 1;

            for (int r = 0; r < rows.size(); r++) {
                T group = rows.get(r);
                List<D> details = configuration.details().apply(group);
                int span = Math.max(details.size(), 1);

                for (int d = 0; d < span; d++) {
                    Row row = sheet.createRow(rowIndex + d);
                    if (d < details.size()) {
                        writeColumns(row, detailStart, configuration.detailColumns(), details.get(d), detailStyles);
                    }
                }

                Row first = sheet.getRow(rowIndex);
                Cell number = first.createCell(0);
                number.setCellValue(r + 1);
                number.setCellStyle(groupStyles.text());
                writeColumns(first, 1, configuration.leadingColumns(), group, groupStyles);
                writeColumns(first, trailingStart, configuration.trailingColumns(), group, groupStyles);

                if (span > 1) {
                    mergeGroup(sheet, rowIndex, span, 0, detailStart - 1);
                    mergeGroup(sheet, rowIndex, span, trailingStart, lastColumn);
                }
                rowIndex += span;
            }
            return rowIndex - 1;
        });
    }

    private ReportFileConfiguration build(String sheetName, String filePrefix, int totalColumns, SheetContent content) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            int lastRow = content.write(sheet);

            for (int i = 0; i < totalColumns; i++) {
                sheet.autoSizeColumn(i, true);
            }
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, Math.max(lastRow, 1), 0, totalColumns - 1));

            workbook.write(out);
            String filename = filePrefix + LocalDate.now(zoneId).format(FILE_DATE) + ".xlsx";
            return new ReportFileConfiguration(filename, ReportFileConfiguration.XLSX, out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el reporte de " + sheetName.toLowerCase(), e);
        }
    }

    private static void writeHeaders(Sheet sheet, List<String> headers) {
        CellStyle style = headerStyle(sheet.getWorkbook());
        Row row = sheet.createRow(0);
        writeHeader(row, 0, NUMBER_HEADER, style);
        for (int i = 0; i < headers.size(); i++) {
            writeHeader(row, i + 1, headers.get(i), style);
        }
    }

    private static void writeHeader(Row row, int index, String value, CellStyle style) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private <T> void writeColumns(Row row, int start, List<ExcelColumnConfiguration<T>> columns, T source, Styles styles) {
        for (int c = 0; c < columns.size(); c++) {
            ExcelColumnConfiguration<T> column = columns.get(c);
            writeValue(row.createCell(start + c), column.value().apply(source), styles, column.format());
        }
    }

    private void writeValue(Cell cell, Object value, Styles styles, String format) {
        switch (value) {
            case null -> cell.setCellValue("");
            case Number number -> cell.setCellValue(number.doubleValue());
            case Instant instant -> {
                cell.setCellValue(LocalDateTime.ofInstant(instant, zoneId));
                cell.setCellStyle(styles.date(format != null ? format : DATE_FORMAT));
                return;
            }
            default -> cell.setCellValue(value.toString());
        }
        if (styles.text() != null) {
            cell.setCellStyle(styles.text());
        }
    }

    private static void mergeGroup(Sheet sheet, int firstRow, int span, int firstColumn, int lastColumn) {
        for (int column = firstColumn; column <= lastColumn; column++) {
            sheet.addMergedRegion(new CellRangeAddress(firstRow, firstRow + span - 1, column, column));
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

    @FunctionalInterface
    private interface SheetContent {
        int write(Sheet sheet);
    }

    private static final class Styles {
        private final Workbook workbook;
        private final boolean centered;
        private final CellStyle text;
        private final Map<String, CellStyle> dates = new HashMap<>();

        Styles(Workbook workbook, boolean centered) {
            this.workbook = workbook;
            this.centered = centered;
            this.text = centered ? newStyle() : null;
        }

        CellStyle text() {
            return text;
        }

        CellStyle date(String format) {
            return dates.computeIfAbsent(format, key -> {
                CellStyle style = newStyle();
                style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(key));
                return style;
            });
        }

        private CellStyle newStyle() {
            CellStyle style = workbook.createCellStyle();
            if (centered) {
                style.setAlignment(HorizontalAlignment.CENTER);
                style.setVerticalAlignment(VerticalAlignment.CENTER);
            }
            return style;
        }
    }
}
