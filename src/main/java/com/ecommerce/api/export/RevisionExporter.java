package com.ecommerce.api.export;

import com.ecommerce.api.constant.RevisionConstants;
import com.ecommerce.api.dto.RevisionDTO;
import com.ecommerce.api.exception.RevisionException;
import com.ecommerce.api.util.RevisionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevisionExporter {

    private final ObjectMapper objectMapper;

    /**
     * Export format enum
     */
    public enum ExportFormat {
        JSON,
        CSV,
        EXCEL,
        PDF
    }

    /**
     * Export revisions in specified format
     */
    public byte[] exportRevisions(List<RevisionDTO> revisions, ExportFormat format) {
        try {
            return switch (format) {
                case JSON -> exportToJson(revisions);
                case CSV -> exportToCsv(revisions);
                case EXCEL -> exportToExcel(revisions);
                case PDF -> exportToPdf(revisions);
            };
        } catch (Exception e) {
            log.error("Error exporting revisions to {}: {}", format, e.getMessage());
            throw RevisionException.processingError(
                    "Error exporting revisions to " + format, e);
        }
    }

    /**
     * Export to JSON
     */
    private byte[] exportToJson(List<RevisionDTO> revisions) throws IOException {
        ObjectMapper mapper = objectMapper.copy()
                .registerModule(new JavaTimeModule());
        return mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(revisions);
    }

    /**
     * Export to CSV
     */
    private byte[] exportToCsv(List<RevisionDTO> revisions) throws IOException {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(RevisionDTO.class)
                .withHeader()
                .withColumnSeparator(',');

        return csvMapper.writer(schema)
                .writeValueAsBytes(revisions);
    }

    /**
     * Export to Excel
     */
    private byte[] exportToExcel(List<RevisionDTO> revisions) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Revisions");

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCell(headerRow, 0, "ID");
            createHeaderCell(headerRow, 1, "Entity Name");
            createHeaderCell(headerRow, 2, "Entity ID");
            createHeaderCell(headerRow, 3, "Type");
            createHeaderCell(headerRow, 4, "Username");
            createHeaderCell(headerRow, 5, "Timestamp");
            createHeaderCell(headerRow, 6, "Changes");
            createHeaderCell(headerRow, 7, "Reason");
            createHeaderCell(headerRow, 8, "IP Address");
            createHeaderCell(headerRow, 9, "User Agent");

            // Create data rows
            int rowNum = 1;
            for (RevisionDTO revision : revisions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(revision.getId());
                row.createCell(1).setCellValue(revision.getEntityName());
                row.createCell(2).setCellValue(revision.getEntityId());
                row.createCell(3).setCellValue(revision.getRevisionType().toString());
                row.createCell(4).setCellValue(revision.getUsername());
                row.createCell(5).setCellValue(
                        RevisionUtils.timestampToLocalDateTime(revision.getTimestamp()).toString()
                );
                row.createCell(6).setCellValue(formatChanges(revision.getChanges()));
                row.createCell(7).setCellValue(revision.getReason());
                row.createCell(8).setCellValue(revision.getIpAddress());
                row.createCell(9).setCellValue(revision.getUserAgent());
            }

            // Autosize columns
            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export to PDF
     */
    private byte[] exportToPdf(List<RevisionDTO> revisions) throws IOException {
        // Using iText or Apache PDFBox for PDF generation
        // This is a placeholder implementation
        throw new UnsupportedOperationException("PDF export not yet implemented");
    }

    /**
     * Create header cell
     */
    private void createHeaderCell(Row row, int column, String value) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = cell.getSheet().getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);
        cell.setCellStyle(style);
    }

    /**
     * Format changes map for display
     */
    private String formatChanges(Map<String, Object> changes) {
        return RevisionUtils.formatChangesForDisplay(changes);
    }

    /**
     * Generate export filename
     */
    public String generateExportFilename(ExportFormat format, String entityName) {
        String timestamp = LocalDateTime.now().format(RevisionConstants.DATE_TIME_FORMATTER)
                .replace(" ", "_")
                .replace(":", "-");

        String extension = switch (format) {
            case JSON -> "json";
            case CSV -> "csv";
            case EXCEL -> "xlsx";
            case PDF -> "pdf";
        };

        return String.format("revisions_%s_%s.%s",
                entityName != null ? entityName : "all",
                timestamp,
                extension
        );
    }

    /**
     * Get content type for format
     */
    public String getContentType(ExportFormat format) {
        return switch (format) {
            case JSON -> "application/json";
            case CSV -> "text/csv";
            case EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case PDF -> "application/pdf";
        };
    }

    /**
     * Export single revision
     */
    public byte[] exportSingleRevision(RevisionDTO revision, ExportFormat format) {
        return exportRevisions(List.of(revision), format);
    }

    /**
     * Export revisions with custom fields
     */
    public byte[] exportRevisionsWithFields(List<RevisionDTO> revisions,
                                            List<String> fields,
                                            ExportFormat format) {
        // Filter revision data to include only specified fields
        List<Map<String, Object>> filteredData = revisions.stream()
                .map(revision -> objectMapper.convertValue(revision,
                        new TypeReference<Map<String, Object>>() {}))
                .map(map -> {
                    map.keySet().retainAll(fields);
                    return map;
                })
                .toList();

        try {
            return switch (format) {
                case JSON -> objectMapper.writeValueAsBytes(filteredData);
                case CSV -> {
                    CsvMapper csvMapper = new CsvMapper();
                    CsvSchema.Builder schemaBuilder = CsvSchema.builder();
                    fields.forEach(schemaBuilder::addColumn);
                    yield csvMapper.writer(schemaBuilder.build().withHeader())
                            .writeValueAsBytes(filteredData);
                }
                case EXCEL -> {
                    try (Workbook workbook = new XSSFWorkbook()) {
                        Sheet sheet = workbook.createSheet("Revisions");

                        // Create header row
                        Row headerRow = sheet.createRow(0);
                        for (int i = 0; i < fields.size(); i++) {
                            createHeaderCell(headerRow, i, fields.get(i));
                        }

                        // Create data rows
                        int rowNum = 1;
                        for (Map<String, Object> data : filteredData) {
                            Row row = sheet.createRow(rowNum++);
                            for (int i = 0; i < fields.size(); i++) {
                                Object value = data.get(fields.get(i));
                                row.createCell(i).setCellValue(
                                        value != null ? value.toString() : ""
                                );
                            }
                        }

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        workbook.write(outputStream);
                        yield outputStream.toByteArray();
                    }
                }
                case PDF -> throw new UnsupportedOperationException(
                        "PDF export not yet implemented");
            };
        } catch (Exception e) {
            throw RevisionException.processingError (
                    "Error exporting revisions with custom fields", e);
        }
    }
}
