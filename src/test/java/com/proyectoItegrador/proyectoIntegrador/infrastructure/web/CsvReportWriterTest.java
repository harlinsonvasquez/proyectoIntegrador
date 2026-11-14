package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvReportWriterTest {
    @Test
    void writesUtf8CsvWithBomAndEscapesQuotesAndSeparators() {
        String csv = new String(CsvReportWriter.toUtf8(List.of(
                List.of("programa", "cantidad"), List.of("Ingeniería, " + "\"" + "A" + "\"", "3"))),
                StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("\uFEFF"));
        assertTrue(csv.contains("\"Ingeniería, \"\"A\"\"\",\"3\"\r\n"));
    }

    @Test
    void prefixesSpreadsheetFormulaValuesAsText() {
        String csv = new String(CsvReportWriter.toUtf8(List.of(List.of("=HYPERLINK(\"https://bad\")"))),
                StandardCharsets.UTF_8);

        assertEquals("\uFEFF\"'=HYPERLINK(\"\"https://bad\"\")\"\r\n", csv);
    }
}
