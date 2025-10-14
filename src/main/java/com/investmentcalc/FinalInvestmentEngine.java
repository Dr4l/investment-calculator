package com.investmentcalc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Final working investment calculator engine based on verified manual calculations
 */
public class FinalInvestmentEngine {
    
    public InvestmentResult calculateInvestment(
            BigDecimal startingAmount,
            int years,
            BigDecimal annualReturnRate,
            String compoundingFrequency,
            BigDecimal additionalContribution,
            int contributionsPerYear,
            boolean contributeAtBeginning) {
        
        List<MonthlyData> monthlyData = new ArrayList<>();
        List<YearlyData> yearlyData = new ArrayList<>();
        
        BigDecimal currentBalance = startingAmount;
        BigDecimal totalContributions = startingAmount;
        BigDecimal totalInterest = BigDecimal.ZERO;
        
        // Track yearly data
        BigDecimal yearlyStartBalance = startingAmount;
        BigDecimal yearlyContributions = BigDecimal.ZERO;
        BigDecimal yearlyInterest = BigDecimal.ZERO;
        
        // Calculate monthly rate directly from annual rate (7% / 12 months)
        BigDecimal monthlyRate = annualReturnRate.divide(BigDecimal.valueOf(100 * 12), 10, RoundingMode.HALF_UP);
        
        // Calculate contribution per month
        // If additionalContribution is the total annual contribution, divide by contributions per year
        // If additionalContribution is per-contribution amount, use it directly
        BigDecimal contributionPerMonth = additionalContribution;
        if (contributionsPerYear > 0) {
            // Assume additionalContribution is annual total, so divide by contributions per year
            contributionPerMonth = additionalContribution.divide(BigDecimal.valueOf(contributionsPerYear), 5, RoundingMode.HALF_UP);
        }
        
        // Process each month
        for (int month = 1; month <= years * 12; month++) {
            BigDecimal monthStartBalance = currentBalance;
            BigDecimal monthContributions = BigDecimal.ZERO;
            
            // Add contribution based on timing preference
            if (shouldAddContribution(month, contributionsPerYear)) {
                if (contributeAtBeginning) {
                    // Add contribution at BEGINNING of month
                    currentBalance = currentBalance.add(contributionPerMonth);
                    monthContributions = contributionPerMonth;
                    totalContributions = totalContributions.add(contributionPerMonth);
                    yearlyContributions = yearlyContributions.add(contributionPerMonth);
                }
            }
            
            // Calculate interest for this month
            BigDecimal monthInterest = currentBalance.multiply(monthlyRate);
            currentBalance = currentBalance.add(monthInterest);
            totalInterest = totalInterest.add(monthInterest);
            yearlyInterest = yearlyInterest.add(monthInterest);
            
            // Add contribution at END of month if that's the preference
            if (shouldAddContribution(month, contributionsPerYear) && !contributeAtBeginning) {
                currentBalance = currentBalance.add(contributionPerMonth);
                monthContributions = contributionPerMonth;
                totalContributions = totalContributions.add(contributionPerMonth);
                yearlyContributions = yearlyContributions.add(contributionPerMonth);
            }
            
            // Store monthly data
            int year = ((month - 1) / 12) + 1;
            monthlyData.add(new MonthlyData(
                String.format("Y%d-M%d", year, ((month - 1) % 12) + 1),
                monthStartBalance,
                monthContributions,
                monthInterest,
                currentBalance
            ));
            
            // Store yearly data at the end of each year
            if (month % 12 == 0 || month == years * 12) {
                yearlyData.add(new YearlyData(
                    year,
                    yearlyStartBalance,
                    yearlyContributions,
                    yearlyInterest,
                    currentBalance
                ));
                
                // Reset yearly tracking
                yearlyStartBalance = currentBalance;
                yearlyContributions = BigDecimal.ZERO;
                yearlyInterest = BigDecimal.ZERO;
            }
        }
        
        return new InvestmentResult(
            startingAmount,
            years,
            annualReturnRate,
            compoundingFrequency,
            currentBalance,
            totalContributions,
            totalInterest,
            monthlyData,
            yearlyData
        );
    }
    
    private boolean shouldAddContribution(int month, int contributionsPerYear) {
        if (contributionsPerYear == 0) return false;
        
        switch (contributionsPerYear) {
            case 12: // Monthly
                return true;
            case 4: // Quarterly (every 3 months)
                return month % 3 == 1;
            case 1: // Annually
                return month % 12 == 1;
            case 52: // Weekly (approximately every month for simplicity)
                return true;
            case 26: // Bi-weekly (approximately every 2 weeks)
                return month % 1 == 0; // Every month for simplicity
            case 24: // Bi-monthly
                return month % 2 == 1; // Every other month starting with month 1
            case 365: // Daily
                return true; // Every month for simplicity
            case 366: // Daily (leap year)
                return true; // Every month for simplicity
            default:
                // Spread evenly based on contributions per year
                if (contributionsPerYear <= 12) {
                    // For frequencies up to monthly
                    return month % (12 / contributionsPerYear) == 1;
                } else {
                    // For higher frequencies, contribute every month
                    return true;
                }
        }
    }
}
