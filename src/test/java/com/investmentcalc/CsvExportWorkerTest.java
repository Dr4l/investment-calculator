package com.investmentcalc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CsvExportWorkerTest {

    @Test
    public void testMonthlyExportWritesExpectedLines(@TempDir Path tempDir) throws Exception {
        int months = 120; // 10 years monthly
        List<MonthlyData> monthly = new ArrayList<>();
        for (int i = 1; i <= months; i++) {
            monthly.add(new MonthlyData("Month " + i,
                    BigDecimal.valueOf(1000 + i),
                    BigDecimal.valueOf(50),
                    BigDecimal.valueOf(2.5),
                    BigDecimal.valueOf(1050 + i)));
        }

        InvestmentResult result = new InvestmentResult(
                BigDecimal.valueOf(1000),
                10,
                BigDecimal.valueOf(7),
                "Monthly",
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(3000),
                monthly,
                new ArrayList<>()
        );

        Path out = tempDir.resolve("monthly_test.csv");
        CsvExportWorker worker = new CsvExportWorker(result, true, out.toFile());
        worker.execute();

        // Wait for completion with timeout
        worker.get(30, TimeUnit.SECONDS);

        assertTrue(Files.exists(out), "Output CSV should exist");
        long lines = Files.lines(out).count();
        assertEquals(months + 1, lines, "CSV should contain header + month rows");
    }
}
