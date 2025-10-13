# Investment Calculator

A comprehensive Java-based investment calculator that computes compound interest growth with periodic contributions and provides detailed visualizations and schedules.

## Features

- **Compound Interest Calculations**: Supports various compounding frequencies (annually, monthly, daily, weekly, quarterly)
- **Periodic Contributions**: Add regular contributions with customizable frequency
- **Visual Growth Chart**: Interactive chart showing investment growth over time
- **Detailed Schedules**: View annual and monthly breakdowns of your investment
- **Modern GUI**: Clean, user-friendly interface with FlatLaf look and feel
- **Comprehensive Results**: Shows end balance, total contributions, and interest earned

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Dependencies

- **JFreeChart 1.5.3**: For creating interactive charts and graphs
- **JCommon 1.0.24**: Required dependency for JFreeChart
- **FlatLaf 3.2.5**: Modern look and feel for the GUI

## How to Build and Run

### Using Maven

1. **Clone or download the project**
   ```bash
   cd investment-calculator
   ```

2. **Compile the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn exec:java -Dexec.mainClass="com.investmentcalc.InvestmentCalculator"
   ```

4. **Create executable JAR**
   ```bash
   mvn clean package
   java -jar target/investment-calculator-1.0.0.jar
   ```

### Direct Java Execution

If you have all dependencies in your classpath:
```bash
javac -cp "lib/*" src/main/java/com/investmentcalc/*.java
java -cp "src/main/java:lib/*" com.investmentcalc.InvestmentCalculator
```


## Project Structure

```
src/main/java/com/investmentcalc/
├── InvestmentCalculator.java          # Main GUI application
├── FinalInvestmentEngine.java        # Core calculation logic (working version)
├── InvestmentCalculatorEngine.java    # Original calculation logic
├── InvestmentResult.java             # Data class for results
├── InvestmentChartPanel.java         # Custom chart panel for displaying investment growth over time
├── MonthlyData.java                  # Monthly data structure
└── YearlyData.java                   # Yearly data structure
```

## Technical Details

- **Compound Interest Formula**: Uses the standard compound interest formula with periodic contributions
- **Precision**: Uses BigDecimal for financial calculations to avoid floating-point errors
- **Charting**: JFreeChart provides interactive zooming and tooltips
- **GUI Framework**: Swing with modern FlatLaf styling
- **Architecture**: Clean separation between calculation logic and presentation layer

## Customization

You can easily modify the application by:
- Changing the default values in the input fields
- Adding new compounding frequencies in the engine
- Customizing chart colors and styles in ChartPanel.java
- Modifying the GUI layout in InvestmentCalculator.java

## License

This project is open source and available under the MIT License.
