package com.investmentcalc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.math.RoundingMode;

/**
 * Utility for exporting InvestmentResult schedules to CSV.
 */
public final class CsvExporter {

    private CsvExporter() {}

    public static void writeScheduleCsv(InvestmentResult result, boolean monthly, Writer writer) throws IOException {
        try (PrintWriter pw = new PrintWriter(writer)) {
            if (monthly) {
                pw.println("Month,Start Balance,Contributions,Interest,End Balance");
                List<MonthlyData> monthlyData = result.getMonthlyData();
                if (monthlyData != null) {
                    for (MonthlyData d : monthlyData) {
                        pw.printf(Locale.US, "%s,%.2f,%.2f,%.2f,%.2f%n",
                            d.getMonth(), asDouble(d.getStartBalance()), asDouble(d.getContributions()), asDouble(d.getInterestEarned()), asDouble(d.getEndBalance()));
                    }
                }
            } else {
                pw.println("Year,Start Balance,Contributions,Interest,End Balance");
                List<YearlyData> yearlyData = result.getYearlyData();
                if (yearlyData != null) {
                    for (YearlyData d : yearlyData) {
                        pw.printf(Locale.US, "%d,%.2f,%.2f,%.2f,%.2f%n",
                            d.getYear(), asDouble(d.getStartBalance()), asDouble(d.getContributions()), asDouble(d.getInterestEarned()), asDouble(d.getEndBalance()));
                    }
                }
            }
            pw.flush();
        }
    }

    public static void writeScheduleCsvToFile(InvestmentResult result, boolean monthly, File file) throws IOException {
        try (Writer w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writeScheduleCsv(result, monthly, w);
        }
    }

    private static double asDouble(BigDecimal bd) {
        if (bd == null) return 0.0;
        return bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
