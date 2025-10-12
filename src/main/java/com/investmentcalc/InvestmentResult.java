package com.investmentcalc;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data class to hold investment calculation results
 */
public class InvestmentResult {
    private final BigDecimal startingAmount;
    private final int years;
    private final BigDecimal annualReturnRate;
    private final String compoundingFrequency;
    private final BigDecimal endBalance;
    private final BigDecimal totalContributions;
    private final BigDecimal totalInterest;
    private final List<MonthlyData> monthlyData;
    private final List<YearlyData> yearlyData;
    
    public InvestmentResult(BigDecimal startingAmount,
                          int years,
                          BigDecimal annualReturnRate,
                          String compoundingFrequency,
                          BigDecimal endBalance,
                          BigDecimal totalContributions,
                          BigDecimal totalInterest,
                          List<MonthlyData> monthlyData,
                          List<YearlyData> yearlyData) {
        this.startingAmount = startingAmount;
        this.years = years;
        this.annualReturnRate = annualReturnRate;
        this.compoundingFrequency = compoundingFrequency;
        this.endBalance = endBalance;
        this.totalContributions = totalContributions;
        this.totalInterest = totalInterest;
        this.monthlyData = monthlyData;
        this.yearlyData = yearlyData;
    }
    
    // Getters
    public BigDecimal getStartingAmount() { return startingAmount; }
    public int getYears() { return years; }
    public BigDecimal getAnnualReturnRate() { return annualReturnRate; }
    public String getCompoundingFrequency() { return compoundingFrequency; }
    public BigDecimal getEndBalance() { return endBalance; }
    public BigDecimal getTotalContributions() { return totalContributions; }
    public BigDecimal getTotalInterest() { return totalInterest; }
    public List<MonthlyData> getMonthlyData() { return monthlyData; }
    public List<YearlyData> getYearlyData() { return yearlyData; }
}
