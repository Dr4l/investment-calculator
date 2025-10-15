package com.investmentcalc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Final working investment calculator engine based on verified manual calculations.
 * This version is corrected to handle misaligned contribution and compounding frequencies
 * with improved precision and clearer logic.
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

        // Yearly data tracking
        BigDecimal yearlyStartBalance = startingAmount;
        BigDecimal yearlyContributions = BigDecimal.ZERO;
        BigDecimal yearlyInterest = BigDecimal.ZERO;

        // Monthly data tracking (for display purposes)
        BigDecimal monthlyStartBalance = startingAmount;
        BigDecimal monthlyContributionsAgg = BigDecimal.ZERO;
        BigDecimal monthlyInterestAgg = BigDecimal.ZERO;

        int compoundingPeriodsPerYear = getCompoundingPeriods(compoundingFrequency);
        // Use high precision for the periodic rate to avoid rounding errors during calculation
        BigDecimal periodicRate = annualReturnRate.divide(BigDecimal.valueOf(100 * compoundingPeriodsPerYear), 20, RoundingMode.HALF_UP);
        BigDecimal multiplier = BigDecimal.ONE.add(periodicRate);

        BigDecimal contributionAmountPerEvent = BigDecimal.ZERO;
        if (contributionsPerYear > 0) {
            // Use high precision for contribution amount as well
            contributionAmountPerEvent = additionalContribution.divide(BigDecimal.valueOf(contributionsPerYear), 10, RoundingMode.HALF_UP);
        }

        int totalPeriods = years * compoundingPeriodsPerYear;
        int currentMonthForDisplay = 1;

        // CORRECTED: Use BigDecimal for the tracker to avoid floating-point errors
        BigDecimal contributionScheduleTracker = BigDecimal.ZERO;
        BigDecimal contributionsPerCompoundingPeriod = BigDecimal.ZERO;
        if (compoundingPeriodsPerYear > 0) {
            contributionsPerCompoundingPeriod = BigDecimal.valueOf(contributionsPerYear)
                    .divide(BigDecimal.valueOf(compoundingPeriodsPerYear), 20, RoundingMode.HALF_UP);
        }


        // Main calculation loop iterates over each compounding period
        for (int period = 1; period <= totalPeriods; period++) {

            // --- 1. Calculate Contributions for this Period ---
            BigDecimal contributionsThisPeriod = BigDecimal.ZERO;
            if (contributionsPerYear > 0) {
                contributionScheduleTracker = contributionScheduleTracker.add(contributionsPerCompoundingPeriod);
                while (contributionScheduleTracker.compareTo(BigDecimal.ONE) >= 0) {
                    contributionsThisPeriod = contributionsThisPeriod.add(contributionAmountPerEvent);
                    contributionScheduleTracker = contributionScheduleTracker.subtract(BigDecimal.ONE);
                }
            }

            // Update total and yearly contribution trackers
            totalContributions = totalContributions.add(contributionsThisPeriod);
            yearlyContributions = yearlyContributions.add(contributionsThisPeriod);
            monthlyContributionsAgg = monthlyContributionsAgg.add(contributionsThisPeriod);

            // --- 2. Apply Contributions and Calculate Interest (CORRECTED LOGIC) ---
            BigDecimal interestThisPeriod;
            if (contributeAtBeginning) {
                // Add contributions first, then apply interest to the new total
                BigDecimal balanceAfterContribution = currentBalance.add(contributionsThisPeriod);
                BigDecimal balanceAfterCompounding = balanceAfterContribution.multiply(multiplier);
                interestThisPeriod = balanceAfterCompounding.subtract(balanceAfterContribution);
                currentBalance = balanceAfterCompounding;
            } else {
                // Apply interest first, then add contributions at the end
                BigDecimal balanceAfterCompounding = currentBalance.multiply(multiplier);
                interestThisPeriod = balanceAfterCompounding.subtract(currentBalance);
                currentBalance = balanceAfterCompounding.add(contributionsThisPeriod);
            }

            // Update total and yearly interest trackers
            totalInterest = totalInterest.add(interestThisPeriod);
            yearlyInterest = yearlyInterest.add(interestThisPeriod);
            monthlyInterestAgg = monthlyInterestAgg.add(interestThisPeriod);

            // --- 3. Aggregate data for display (Yearly & Monthly schedules) ---

            // Check if a year has ended
            if (period % compoundingPeriodsPerYear == 0 && period > 0) {
                int currentYear = period / compoundingPeriodsPerYear;
                yearlyData.add(new YearlyData(currentYear, yearlyStartBalance, yearlyContributions, yearlyInterest, currentBalance));

                // Reset for the next year
                yearlyStartBalance = currentBalance;
                yearlyContributions = BigDecimal.ZERO;
                yearlyInterest = BigDecimal.ZERO;
            }
            
            // Check if a month has ended (for display purposes)
            // This approximates which compounding period corresponds to a month-end
            int monthEquivalent = (int) Math.floor(((double) period / compoundingPeriodsPerYear) * 12);
            if (monthEquivalent >= currentMonthForDisplay && currentMonthForDisplay <= years * 12) {
                 int year = ((currentMonthForDisplay - 1) / 12) + 1;
                 String monthLabel = String.format("Year %d, Month %d", year, ((currentMonthForDisplay - 1) % 12) + 1);

                monthlyData.add(new MonthlyData(monthLabel, monthlyStartBalance, monthlyContributionsAgg, monthlyInterestAgg, currentBalance));
                
                // Reset for the next month's aggregation
                monthlyStartBalance = currentBalance;
                monthlyContributionsAgg = BigDecimal.ZERO;
                monthlyInterestAgg = BigDecimal.ZERO;
                currentMonthForDisplay++;
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

    /**
     * Returns the number of compounding periods in a year for a given frequency.
     * @param compoundingFrequency The frequency string (e.g., "Annually", "Monthly").
     * @return The number of periods per year.
     */
    private int getCompoundingPeriods(String compoundingFrequency) {
        switch (compoundingFrequency) {
            case "Annually":
                return 1;
            case "Quarterly":
                return 4;
            case "Monthly":
                return 12;
            case "Weekly":
                return 52;
            case "Daily":
                return 365;
            default:
                return 12; // Default to monthly if frequency is unknown
        }
    }
}