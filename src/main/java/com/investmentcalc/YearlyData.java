package com.investmentcalc;

import java.math.BigDecimal;

/**
 * Data class to hold yearly investment data
 */
public class YearlyData {
    private final int year;
    private final BigDecimal startBalance;
    private final BigDecimal contributions;
    private final BigDecimal interestEarned;
    private final BigDecimal endBalance;
    
    public YearlyData(int year, BigDecimal startBalance, BigDecimal contributions, 
                     BigDecimal interestEarned, BigDecimal endBalance) {
        this.year = year;
        this.startBalance = startBalance;
        this.contributions = contributions;
        this.interestEarned = interestEarned;
        this.endBalance = endBalance;
    }
    
    // Getters
    public int getYear() { return year; }
    public BigDecimal getStartBalance() { return startBalance; }
    public BigDecimal getContributions() { return contributions; }
    public BigDecimal getInterestEarned() { return interestEarned; }
    public BigDecimal getEndBalance() { return endBalance; }
}
