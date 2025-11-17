package com.investmentcalc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvExporterTest {

    @Test
    void testAnnualCsvExport(@TempDir Path tempDir) throws Exception {
        System.out.println("\n=== Test: Annual CSV Export ===");
        YearlyData y1 = new YearlyData(1, new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("50"), new BigDecimal("1150"));
        YearlyData y2 = new YearlyData(2, new BigDecimal("1150"), new BigDecimal("100"), new BigDecimal("57.50"), new BigDecimal("1307.50"));

        List<YearlyData> years = Arrays.asList(y1, y2);
        InvestmentResult result = new InvestmentResult(new BigDecimal("1000"), 2, new BigDecimal("5"), "Annually", new BigDecimal("1307.50"), new BigDecimal("1200"), new BigDecimal("107.50"), null, years);

        Path out = tempDir.resolve("annual_test.csv");
        CsvExporter.writeScheduleCsvToFile(result, false, out.toFile());

        List<String> lines = Files.readAllLines(out, StandardCharsets.UTF_8);
        boolean notEmpty = !lines.isEmpty();
        boolean headerOk = notEmpty && "Year,Start Balance,Contributions,Interest,End Balance".equals(lines.get(0));
        boolean firstLineStarts = lines.size() > 1 && lines.get(1).startsWith("1,");
        boolean containsStart = lines.size() > 1 && lines.get(1).contains("1000.00");
        boolean containsEnd = lines.size() > 2 && lines.get(2).contains("1307.50");

        System.out.println("Lines: " + lines.size());
        System.out.println("Header OK: " + headerOk);
        System.out.println("First line starts with '1,': " + firstLineStarts);
        System.out.println("Contains start balance: " + containsStart);
        System.out.println("Contains end balance: " + containsEnd);

        boolean passed = headerOk && firstLineStarts && containsStart && containsEnd;
        System.out.printf("Result:   %s%n", passed ? "✅ PASS" : "❌ FAIL");

        assertTrue(notEmpty, "CSV should not be empty");
        assertEquals("Year,Start Balance,Contributions,Interest,End Balance", lines.get(0));
        assertTrue(firstLineStarts, "First data line should start with '1,'");
        assertTrue(containsStart);
        assertTrue(containsEnd);
    }

    @Test
    void testMonthlyCsvExport(@TempDir Path tempDir) throws Exception {
        System.out.println("\n=== Test: Monthly CSV Export ===");
        MonthlyData m1 = new MonthlyData("2025-01", new BigDecimal("1000"), new BigDecimal("10"), new BigDecimal("4.17"), new BigDecimal("1014.17"));
        MonthlyData m2 = new MonthlyData("2025-02", new BigDecimal("1014.17"), new BigDecimal("10"), new BigDecimal("4.23"), new BigDecimal("1028.40"));

        List<MonthlyData> months = Arrays.asList(m1, m2);
        InvestmentResult result = new InvestmentResult(new BigDecimal("1000"), 1, new BigDecimal("5"), "Monthly", new BigDecimal("1028.40"), new BigDecimal("1020"), new BigDecimal("28.40"), months, null);

        Path out = tempDir.resolve("monthly_test.csv");
        CsvExporter.writeScheduleCsvToFile(result, true, out.toFile());

        List<String> lines = Files.readAllLines(out, StandardCharsets.UTF_8);
        boolean notEmpty = !lines.isEmpty();
        boolean headerOk = notEmpty && "Month,Start Balance,Contributions,Interest,End Balance".equals(lines.get(0));
        boolean firstLineStarts = lines.size() > 1 && lines.get(1).startsWith("2025-01,");
        boolean containsStart = lines.size() > 1 && lines.get(1).contains("1000.00");
        boolean containsEnd = lines.size() > 2 && lines.get(2).contains("1028.40");

        System.out.println("Lines: " + lines.size());
        System.out.println("Header OK: " + headerOk);
        System.out.println("First line starts with '2025-01,': " + firstLineStarts);
        System.out.println("Contains start balance: " + containsStart);
        System.out.println("Contains end balance: " + containsEnd);

        boolean passed = headerOk && firstLineStarts && containsStart && containsEnd;
        System.out.printf("Result:   %s%n", passed ? "✅ PASS" : "❌ FAIL");

        assertTrue(notEmpty, "CSV should not be empty");
        assertEquals("Month,Start Balance,Contributions,Interest,End Balance", lines.get(0));
        assertTrue(firstLineStarts);
        assertTrue(containsStart);
        assertTrue(containsEnd);
    }
}
