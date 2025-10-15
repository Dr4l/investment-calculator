package com.investmentcalc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FinalInvestmentEngineTest {
    
    private FinalInvestmentEngine engine;
    
    @BeforeEach
    void setUp() {
        engine = new FinalInvestmentEngine();
    }
    
    @Test
    void testSimpleAnnualCompounding() {
        System.out.println("\n=== Test: Simple Annual Compounding ===");
        
        // Input
        BigDecimal startingAmount = new BigDecimal("1000");
        int years = 1;
        BigDecimal returnRate = new BigDecimal("10");
        String frequency = "Annually";
        
        System.out.printf("Input: $%s at %s%% %s for %d year(s)\n", 
            startingAmount, returnRate, frequency.toLowerCase(), years);
        
        // Calculate
        InvestmentResult result = engine.calculateInvestment(
            startingAmount, years, returnRate, frequency,
            new BigDecimal("0"), 0, false);
        
        BigDecimal expected = new BigDecimal("1100.00");
        BigDecimal actual = result.getEndBalance().setScale(2, RoundingMode.HALF_UP);
        
        System.out.printf("Expected: $%s\n", expected);
        System.out.printf("Actual:   $%s\n", actual);
        System.out.printf("Result:   %s\n", expected.equals(actual) ? "✅ PASS" : "❌ FAIL");
        
        assertEquals(expected, actual);
    }
    
    @Test
    void testMonthlyVsAnnualCompounding() {
        System.out.println("\n=== Test: Monthly vs Annual Compounding ===");
        
        BigDecimal startingAmount = new BigDecimal("1000");
        int years = 1;
        BigDecimal returnRate = new BigDecimal("12");
        
        System.out.printf("Input: $%s at %s%% for %d year(s)\n", 
            startingAmount, returnRate, years);
        
        // Annual compounding
        InvestmentResult annualResult = engine.calculateInvestment(
            startingAmount, years, returnRate, "Annually",
            new BigDecimal("0"), 0, false);
        BigDecimal annualBalance = annualResult.getEndBalance().setScale(2, RoundingMode.HALF_UP);
        
        // Monthly compounding
        InvestmentResult monthlyResult = engine.calculateInvestment(
            startingAmount, years, returnRate, "Monthly",
            new BigDecimal("0"), 0, false);
        BigDecimal monthlyBalance = monthlyResult.getEndBalance().setScale(2, RoundingMode.HALF_UP);
        
        System.out.printf("Annual compounding:  $%s\n", annualBalance);
        System.out.printf("Monthly compounding: $%s\n", monthlyBalance);
        System.out.printf("Difference: $%s\n", monthlyBalance.subtract(annualBalance));
        
        boolean passed = monthlyBalance.compareTo(annualBalance) > 0;
        System.out.printf("Result: %s (Monthly should be higher)\n", passed ? "✅ PASS" : "❌ FAIL");
        
        assertTrue(passed, "Monthly compounding should yield more than annual compounding");
    }
    
    @Test
    void testDailyVsMonthlyCompounding() {
        System.out.println("\n=== Test: Daily vs Monthly Compounding ===");
        
        BigDecimal startingAmount = new BigDecimal("1000");
        int years = 1;
        BigDecimal returnRate = new BigDecimal("12");
        
        System.out.printf("Input: $%s at %s%% for %d year(s)\n", 
            startingAmount, returnRate, years);
        
        // Monthly compounding
        InvestmentResult monthlyResult = engine.calculateInvestment(
            startingAmount, years, returnRate, "Monthly",
            new BigDecimal("0"), 0, false);
        BigDecimal monthlyBalance = monthlyResult.getEndBalance().setScale(2, RoundingMode.HALF_UP);
        
        // Daily compounding
        InvestmentResult dailyResult = engine.calculateInvestment(
            startingAmount, years, returnRate, "Daily",
            new BigDecimal("0"), 0, false);
        BigDecimal dailyBalance = dailyResult.getEndBalance().setScale(2, RoundingMode.HALF_UP);
        
        System.out.printf("Monthly compounding: $%s\n", monthlyBalance);
        System.out.printf("Daily compounding:   $%s\n", dailyBalance);
        System.out.printf("Difference: $%s\n", dailyBalance.subtract(monthlyBalance));
        
        boolean passed = dailyBalance.compareTo(monthlyBalance) > 0;
        System.out.printf("Result: %s (Daily should be higher)\n", passed ? "✅ PASS" : "❌ FAIL");
        
        assertTrue(passed, "Daily compounding should yield more than monthly compounding");
    }
    
    @Test
    void testWithContributions() {
        System.out.println("\n=== Test: With Additional Contributions ===");
        
        BigDecimal startingAmount = new BigDecimal("1000");
        int years = 2;
        BigDecimal returnRate = new BigDecimal("10");
        BigDecimal contribution = new BigDecimal("1000");
        
        System.out.printf("Input: $%s starting + $%s/year at %s%% annually for %d years\n", 
            startingAmount, contribution, returnRate, years);
        System.out.println("Manual calculation:");
        System.out.println("  Year 1: ($1000 + $1000) × 1.10 = $2200");
        System.out.println("  Year 2: ($2200 + $1000) × 1.10 = $3520");
        
        InvestmentResult result = engine.calculateInvestment(
            startingAmount, years, returnRate, "Annually",
            contribution, 1, true);
        
        BigDecimal expected = new BigDecimal("3520.00");
        BigDecimal actual = result.getEndBalance().setScale(2, RoundingMode.HALF_UP);
        
        System.out.printf("Expected: $%s\n", expected);
        System.out.printf("Actual:   $%s\n", actual);
        System.out.printf("Result:   %s\n", expected.equals(actual) ? "✅ PASS" : "❌ FAIL");
        
        assertEquals(expected, actual);
    }
    
    @Test
    void testNegativeContributions() {
        System.out.println("\n=== Test: Negative Contributions (Withdrawals) ===");
        
        BigDecimal startingAmount = new BigDecimal("10000");
        int years = 1;
        BigDecimal returnRate = new BigDecimal("10");
        BigDecimal withdrawal = new BigDecimal("-1000");
        
        System.out.printf("Input: $%s starting, withdraw $%s, %s%% annually for %d year\n", 
            startingAmount, withdrawal.abs(), returnRate, years);
        System.out.println("Manual calculation: ($10000 - $1000) × 1.10 = $9900");
        
        InvestmentResult result = engine.calculateInvestment(
            startingAmount, years, returnRate, "Annually",
            withdrawal, 1, true);
        
        BigDecimal expected = new BigDecimal("9900.00");
        BigDecimal actual = result.getEndBalance().setScale(2, RoundingMode.HALF_UP);
        
        System.out.printf("Expected: $%s\n", expected);
        System.out.printf("Actual:   $%s\n", actual);
        System.out.printf("Result:   %s\n", expected.equals(actual) ? "✅ PASS" : "❌ FAIL");
        
        assertEquals(expected, actual);
    }
    
    @Test
    void testKnownCompoundInterestFormula() {
        System.out.println("\n=== Test: Known Compound Interest Formula ===");
        
        BigDecimal startingAmount = new BigDecimal("5000");
        int years = 3;
        BigDecimal returnRate = new BigDecimal("6");
        String frequency = "Quarterly";
        
        System.out.printf("Input: $%s at %s%% %s for %d years\n", 
            startingAmount, returnRate, frequency.toLowerCase(), years);
        System.out.println("Formula: A = P(1 + r/n)^(nt)");
        System.out.println("A = 5000(1 + 0.06/4)^(4×3) = 5000(1.015)^12 ≈ $5978.09");
        
        InvestmentResult result = engine.calculateInvestment(
            startingAmount, years, returnRate, frequency,
            new BigDecimal("0"), 0, false);
            
        BigDecimal expected = new BigDecimal("5978.09");
        BigDecimal actual = result.getEndBalance().setScale(2, RoundingMode.HALF_UP);
        BigDecimal difference = expected.subtract(actual).abs();
        
        System.out.printf("Expected: ~$%s\n", expected);
        System.out.printf("Actual:   $%s\n", actual);
        System.out.printf("Difference: $%s\n", difference);
        
        boolean passed = difference.compareTo(new BigDecimal("1.00")) < 0;
        System.out.printf("Result: %s (within $1.00 tolerance)\n", passed ? "✅ PASS" : "❌ FAIL");
        
        assertTrue(passed, String.format("Expected around %s, got %s", expected, actual));
    }
}