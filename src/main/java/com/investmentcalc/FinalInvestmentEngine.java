package com.investmentcalc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Corrected FinalInvestmentEngine.
 *
 * Key points:
 * - Uses proper periodic rate: r_period = annualRate / compoundingPeriodsPerYear
 * - Computes monthly multiplier = (1 + r_period)^(compoundingPeriodsPerYear / 12.0)
 *   so the discrete compounding effect is preserved when simulating month-by-month.
 * - Respects contributeAtBeginning vs contributeAtEnd semantics.
 *
 * Note: MonthlyData, YearlyData and InvestmentResult classes are assumed to exist
 * and provide constructors/getters used here.
 */
public class FinalInvestmentEngine {

    public InvestmentResult calculateInvestment(
            BigDecimal startingAmount,
            int years,
            BigDecimal annualReturnRate,     // as percent, e.g. 7 for 7%
            String compoundingFrequency,
            BigDecimal additionalContribution,
            int contributionsPerYear,
            boolean contributeAtBeginning) {

        List<MonthlyData> monthlyData = new ArrayList<>();
        List<YearlyData> yearlyData = new ArrayList<>();

        // Generate monthly schedule and also compute totals from it
        generateMonthlySchedule(
            monthlyData,
            startingAmount,
            years,
            annualReturnRate,
            getCompoundingPeriods(compoundingFrequency),
            additionalContribution,
            contributionsPerYear,
            contributeAtBeginning
        );

        // Compute final totals from monthlyData
        BigDecimal currentBalance = startingAmount;
        BigDecimal totalContributions = startingAmount;
        BigDecimal totalInterest = BigDecimal.ZERO;

        BigDecimal yearStartBalance = startingAmount;
        BigDecimal yearContributions = BigDecimal.ZERO;
        BigDecimal yearInterest = BigDecimal.ZERO;

        for (int i = 0; i < monthlyData.size(); i++) {
            MonthlyData md = monthlyData.get(i);
            totalContributions = totalContributions.add(md.getContributions());
            totalInterest = totalInterest.add(md.getInterestEarned());
            currentBalance = md.getEndBalance();

            yearContributions = yearContributions.add(md.getContributions());
            yearInterest = yearInterest.add(md.getInterestEarned());

            if ((i + 1) % 12 == 0) {
                int year = (i + 1) / 12;
                yearlyData.add(new YearlyData(
                    year,
                    yearStartBalance,
                    yearContributions,
                    yearInterest,
                    currentBalance
                ));
                yearStartBalance = currentBalance;
                yearContributions = BigDecimal.ZERO;
                yearInterest = BigDecimal.ZERO;
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
     * Simulate month-by-month but compute monthly interest using the discrete compounding math:
     * monthlyMultiplier = (1 + periodicRate)^(compoundingPeriodsPerYear / 12.0)
     */
    private void generateMonthlySchedule(
        List<MonthlyData> monthlyData,
        BigDecimal startingAmount,
        int years,
        BigDecimal annualReturnRate,
        int compoundingPeriodsPerYear,
        BigDecimal additionalContribution,
        int contributionsPerYear,
        boolean contributeAtBeginning) {

        BigDecimal currentBalance = startingAmount;

        // contribution per event (e.g. if contributionsPerYear=12, this is monthly amount)
        BigDecimal contributionAmountPerEvent = BigDecimal.ZERO;
        if (contributionsPerYear > 0) {
            contributionAmountPerEvent = additionalContribution
                    .divide(BigDecimal.valueOf(contributionsPerYear), 20, RoundingMode.HALF_UP);
        }

        int totalMonths = years * 12;

        // Convert annual percent to decimal (e.g. 7% -> 0.07)
        BigDecimal annualRateDecimal = annualReturnRate
                .divide(BigDecimal.valueOf(100), 30, RoundingMode.HALF_UP);

        // periodic rate per compounding period (nominal) : r_period = annualRateDecimal / compoundingPeriodsPerYear
        BigDecimal periodicRate = annualRateDecimal
                .divide(BigDecimal.valueOf(compoundingPeriodsPerYear), 30, RoundingMode.HALF_UP);

        // multiplier per compounding period: (1 + r_period)
        BigDecimal periodMultiplier = BigDecimal.ONE.add(periodicRate);

        // periods per month (may be fractional, e.g. weekly: 52/12 = 4.3333)
        double periodsPerMonth = (double) compoundingPeriodsPerYear / 12.0;

        // monthly multiplier = (1 + r_period)^(periodsPerMonth)
        // use double pow then convert to BigDecimal with scale
        double periodMultiplierDouble = periodMultiplier.doubleValue();
        double monthlyMultiplierDouble = Math.pow(periodMultiplierDouble, periodsPerMonth);
        BigDecimal monthlyMultiplier = BigDecimal.valueOf(monthlyMultiplierDouble);

        // monthly interest factor = monthlyMultiplier - 1
        BigDecimal monthlyInterestFactor = monthlyMultiplier.subtract(BigDecimal.ONE);

        // Build a simple contribution schedule mapping months -> contribution amount.
        // For standard frequencies (1,4,12) use explicit months; otherwise distribute evenly per month.
        // This preserves the month semantics you described.
        for (int month = 1; month <= totalMonths; month++) {

            BigDecimal monthStartBalance = currentBalance;
            BigDecimal thisMonthContributions = BigDecimal.ZERO;

            // Determine contributions this month
            if (contributionsPerYear > 0 || additionalContribution.compareTo(BigDecimal.ZERO) != 0) {
                switch (contributionsPerYear) {
                    case 1: // annual -> assumed in month 1 (January)
                        if ((month - 1) % 12 == 0) {
                            thisMonthContributions = contributionAmountPerEvent;
                        }
                        break;
                    case 4: // quarterly -> months 1,4,7,10
                        int monthInYear = ((month - 1) % 12) + 1;
                        if (monthInYear == 1 || monthInYear == 4 || monthInYear == 7 || monthInYear == 10) {
                            thisMonthContributions = contributionAmountPerEvent;
                        }
                        break;
                    case 12: // monthly
                        thisMonthContributions = contributionAmountPerEvent;
                        break;
                    default:
                        // distribute evenly across 12 months (for nonstandard contributed-per-year values)
                        thisMonthContributions = additionalContribution.divide(BigDecimal.valueOf(12), 20, RoundingMode.HALF_UP);
                        break;
                }
            }

            // Handle both positive contributions and negative withdrawals
            if (contributeAtBeginning && thisMonthContributions.compareTo(BigDecimal.ZERO) != 0) {
                currentBalance = currentBalance.add(thisMonthContributions); // This will subtract if negative
            }

            // Calculate interest for the month using the discrete compounding monthly factor
            // interest = balance * monthlyInterestFactor
            BigDecimal thisMonthInterest = monthStartBalance.multiply(monthlyInterestFactor).setScale(20, RoundingMode.HALF_UP);

            // Apply interest to current balance (if contribution was added at beginning, interest should be applied to monthStartBalance or to balance including beginning-of-month contribution?)
            // We use monthStartBalance for the interest base so that "contributeAtBeginning" yields contribution earning interest for the month.
            if (contributeAtBeginning) {
                // monthStartBalance was the balance before adding contribution; since we added contribution to currentBalance already,
                // the proper interest base for begin-of-period semantics is (monthStartBalance + contribution).
                BigDecimal beginBalanceForInterest = monthStartBalance.add(thisMonthContributions);
                thisMonthInterest = beginBalanceForInterest.multiply(monthlyInterestFactor).setScale(20, RoundingMode.HALF_UP);
            } else {
                // end-of-period contributions earn no interest during the month
                thisMonthInterest = monthStartBalance.multiply(monthlyInterestFactor).setScale(20, RoundingMode.HALF_UP);
            }

            // Add computed interest to current balance
            currentBalance = currentBalance.add(thisMonthInterest);

            // Handle end-of-period contributions/withdrawals
            if (!contributeAtBeginning && thisMonthContributions.compareTo(BigDecimal.ZERO) != 0) {
                currentBalance = currentBalance.add(thisMonthContributions); // This will subtract if negative
            }

            // Record display data
            int displayYear = ((month - 1) / 12) + 1;
            int displayMonth = ((month - 1) % 12) + 1;
            String monthLabel = String.format("Year %d, Month %d", displayYear, displayMonth);

            // Round values for display/storage (choose a reasonable scale, e.g. 10)
            BigDecimal displayedStart = monthStartBalance.setScale(10, RoundingMode.HALF_UP);
            BigDecimal displayedContrib = thisMonthContributions.setScale(10, RoundingMode.HALF_UP);
            BigDecimal displayedInterest = thisMonthInterest.setScale(10, RoundingMode.HALF_UP);
            BigDecimal displayedEnd = currentBalance.setScale(10, RoundingMode.HALF_UP);

            monthlyData.add(new MonthlyData(monthLabel, displayedStart, displayedContrib, displayedInterest, displayedEnd));
        }
    }

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
                return 12;
        }
    }
}
