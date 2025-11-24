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

    @Test
    void testEmptyExports(@TempDir Path tempDir) throws Exception {
        // Annual empty
        InvestmentResult annualEmpty = new InvestmentResult(new BigDecimal("0"), 0, new BigDecimal("0"), "Annually", new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), null, Arrays.asList());
        Path annualOut = tempDir.resolve("annual_empty.csv");
        CsvExporter.writeScheduleCsvToFile(annualEmpty, false, annualOut.toFile());
        List<String> annualLines = Files.readAllLines(annualOut, StandardCharsets.UTF_8);
        assertFalse(annualLines.isEmpty());
        assertEquals("Year,Start Balance,Contributions,Interest,End Balance", annualLines.get(0));

        // Monthly empty
        InvestmentResult monthlyEmpty = new InvestmentResult(new BigDecimal("0"), 0, new BigDecimal("0"), "Monthly", new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), Arrays.asList(), null);
        Path monthlyOut = tempDir.resolve("monthly_empty.csv");
        CsvExporter.writeScheduleCsvToFile(monthlyEmpty, true, monthlyOut.toFile());
        List<String> monthlyLines = Files.readAllLines(monthlyOut, StandardCharsets.UTF_8);
        assertFalse(monthlyLines.isEmpty());
        assertEquals("Month,Start Balance,Contributions,Interest,End Balance", monthlyLines.get(0));
    }

    @Test
    void testNullValueFormatting(@TempDir Path tempDir) throws Exception {
        // Create entries with null BigDecimal fields
        MonthlyData mNull = new MonthlyData("2025-03", null, null, null, null);
        InvestmentResult result = new InvestmentResult(null, 0, null, "Monthly", null, null, null, Arrays.asList(mNull), null);
        Path out = tempDir.resolve("monthly_nulls.csv");
        CsvExporter.writeScheduleCsvToFile(result, true, out.toFile());
        List<String> lines = Files.readAllLines(out, StandardCharsets.UTF_8);
        assertEquals("Month,Start Balance,Contributions,Interest,End Balance", lines.get(0));
        // data line should contain 0.00 for numeric nulls
        assertTrue(lines.get(1).contains("0.00"));
    }

    @Test
    void testFilePermissionError(@TempDir Path tempDir) throws Exception {
        Path out = tempDir.resolve("not_writable.csv");
        java.io.File f = out.toFile();
        // create file and make it read-only
        try (java.io.PrintWriter pw = new java.io.PrintWriter(f, "UTF-8")) {
            pw.println("test");
        }
        boolean madeReadOnly = f.setWritable(false);
        // On some platforms setWritable may fail; skip test if we can't make it read-only
        org.junit.jupiter.api.Assumptions.assumeTrue(madeReadOnly, "Cannot make file read-only on this platform");

        InvestmentResult result = new InvestmentResult(new BigDecimal("0"), 0, new BigDecimal("0"), "Annually", new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), null, Arrays.asList());
        try {
            assertThrows(java.io.IOException.class, () -> CsvExporter.writeScheduleCsvToFile(result, false, f));
        } finally {
            // restore writable so temp cleanup works
            f.setWritable(true);
        }
    }

    @Test
    void testLargeDatasetExport(@TempDir Path tempDir) throws Exception {
        int n = 2000; // reasonably large for a unit test
        java.util.ArrayList<MonthlyData> months = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            months.add(new MonthlyData("m" + i, new BigDecimal("1000"), new BigDecimal("10"), new BigDecimal("1"), new BigDecimal("1011")));
        }
        InvestmentResult result = new InvestmentResult(new BigDecimal("1000"), 0, new BigDecimal("0"), "Monthly", new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), months, null);
        Path out = tempDir.resolve("monthly_large.csv");
        CsvExporter.writeScheduleCsvToFile(result, true, out.toFile());
        List<String> lines = Files.readAllLines(out, StandardCharsets.UTF_8);
        // header + n lines
        assertEquals(n + 1, lines.size());
    }
}
