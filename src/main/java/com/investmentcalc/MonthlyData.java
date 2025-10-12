package com.investmentcalc;

import java.math.BigDecimal;

/**
 * Data class to hold monthly investment data
 */
public class MonthlyData {
    private final String month;
    private final BigDecimal startBalance;
    private final BigDecimal contributions;
    private final BigDecimal interestEarned;
    private final BigDecimal endBalance;
    
    public MonthlyData(String month, BigDecimal startBalance, BigDecimal contributions, 
                      BigDecimal interestEarned, BigDecimal endBalance) {
        this.month = month;
        this.startBalance = startBalance;
        this.contributions = contributions;
        this.interestEarned = interestEarned;
        this.endBalance = endBalance;
    }
    
    // Getters
    public String getMonth() { return month; }
    public BigDecimal getStartBalance() { return startBalance; }
    public BigDecimal getContributions() { return contributions; }
    public BigDecimal getInterestEarned() { return interestEarned; }
    public BigDecimal getEndBalance() { return endBalance; }
}
