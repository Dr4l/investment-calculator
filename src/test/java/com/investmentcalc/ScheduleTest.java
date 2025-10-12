package com.investmentcalc;

import java.math.BigDecimal;
import java.util.List;

/**
 * Simple test to verify schedule generation works without format errors
 */
public class ScheduleTest {
    
    public static void main(String[] args) {
        System.out.println("Testing Schedule Generation...");
        
        FinalInvestmentEngine engine = new FinalInvestmentEngine();
        
        // Test with simple parameters
        InvestmentResult result = engine.calculateInvestment(
            new BigDecimal("1000"), // Starting amount
            2, // 2 years
            new BigDecimal("5"), // 5% annual return
            "Monthly", // Monthly compounding
            new BigDecimal("1200"), // $1200 annual contribution
            12, // 12 contributions per year (monthly)
            true // Contribute at beginning of period
        );
        
        System.out.println("Calculation successful!");
        System.out.printf("End Balance: $%,.2f%n", result.getEndBalance());
        System.out.printf("Total Contributions: $%,.2f%n", result.getTotalContributions());
        System.out.printf("Total Interest: $%,.2f%n", result.getTotalInterest());
        
        // Test schedule generation
        System.out.println("\nAnnual Schedule:");
        List<YearlyData> yearlyData = result.getYearlyData();
        for (YearlyData data : yearlyData) {
            System.out.printf("Year %d: Balance = $%,.2f%n", data.getYear(), data.getEndBalance());
        }
        
        System.out.println("\nMonthly Schedule (first 6 months):");
        List<MonthlyData> monthlyData = result.getMonthlyData();
        for (int i = 0; i < Math.min(6, monthlyData.size()); i++) {
            MonthlyData data = monthlyData.get(i);
            System.out.printf("Month %s: Balance = $%,.2f%n", data.getMonth(), data.getEndBalance());
        }
        
        System.out.println("\nTest completed successfully - no format errors!");
    }
}