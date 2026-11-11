package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Genera CSV UTF-8 compatible con hojas de cálculo y protege contra fórmulas inyectadas. */
public final class CsvReportWriter {
    private static final char UTF8_BOM = '\uFEFF';

    private CsvReportWriter() { }

    public static byte[] toUtf8(List<List<String>> rows) {
        StringBuilder csv = new StringBuilder().append(UTF8_BOM);
        for (List<String> row : rows) {
            for (int column = 0; column < row.size(); column++) {
                if (column > 0) csv.append(',');
                csv.append(escape(row.get(column)));
            }
            csv.append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static ResponseEntity<byte[]> attachment(String filename, List<List<String>> rows) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(toUtf8(rows));
    }

    private static String escape(String value) {
        String cell = value == null ? "" : value;
        int firstVisible = 0;
        while (firstVisible < cell.length() && Character.isWhitespace(cell.charAt(firstVisible))) firstVisible++;
        if (firstVisible < cell.length() && "=+-@".indexOf(cell.charAt(firstVisible)) >= 0) {
            cell = "'" + cell;
        }
        return "\"" + cell.replace("\"", "\"\"") + "\"";
    }
}
