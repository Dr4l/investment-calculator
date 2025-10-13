package com.investmentcalc;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Investment Calculator Application
 * Provides a comprehensive investment calculator with compound interest calculations,
 * visualization, and detailed scheduling options.
 */
public class InvestmentCalculator extends JFrame {
    private JTextField startingAmountField;
    private JTextField yearsField;
    private JTextField returnRateField;
    private JComboBox<String> compoundingCombo;
    private JTextField additionalContributionField;
    private JTextField contributionFrequencyField;
    private JComboBox<String> contributionTimingCombo;
    private JComboBox<String> currencyCombo;
    private JEditorPane resultsArea;
    private JTabbedPane scheduleTabbedPane;
    
    private FinalInvestmentEngine calculator;
    private InvestmentChartPanel chartPanelComponent;
    private String selectedCurrency = "USD";

    // Main panels that should persist
    private JPanel mainPanel;
    private JPanel inputPanel;
    private JPanel resultsPanel;
    private JPanel bottomPanel;

    public InvestmentCalculator() {
        initializeLookAndFeel();
        calculator = new FinalInvestmentEngine();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Investment Calculator");
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private void initializeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeComponents() {
        // Input fields
        startingAmountField = new JTextField("20000", 15);
        yearsField = new JTextField("10", 15);
        returnRateField = new JTextField("7", 15);
        
        String[] compoundingOptions = {"Annually", "Monthly", "Daily", "Weekly", "Quarterly"};
        compoundingCombo = new JComboBox<>(compoundingOptions);
        compoundingCombo.setSelectedIndex(1); // Default to Monthly
        
        additionalContributionField = new JTextField("12000", 15);
        contributionFrequencyField = new JTextField("12", 15);
        
        String[] timingOptions = {"Beginning of Period", "End of Period"};
        contributionTimingCombo = new JComboBox<>(timingOptions);
        contributionTimingCombo.setSelectedIndex(0); // Default to beginning
        
        String[] currencyOptions = {"USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)", "CAD (C$)", "AUD (A$)"};
        currencyCombo = new JComboBox<>(currencyOptions);
        currencyCombo.setSelectedIndex(0); // Default to USD
        
        // Results area - using JEditorPane for HTML formatting
        resultsArea = new JEditorPane();
        resultsArea.setEditable(false);
        resultsArea.setContentType("text/html");
        
        // Chart panel - initialize with empty chart
        chartPanelComponent = new InvestmentChartPanel(null);
        
        // Schedule tabbed pane
        scheduleTabbedPane = new JTabbedPane();
        
        // Initialize main panels
        mainPanel = new JPanel(new BorderLayout());
        inputPanel = createInputPanel();
        resultsPanel = createResultsPanel();
        bottomPanel = createBottomPanel();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Add panels to main frame
        add(inputPanel, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Investment Parameters"));
        panel.setBackground(new Color(245, 245, 245));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Starting Amount
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Starting Amount:"), gbc);
        gbc.gridx = 1;
        panel.add(startingAmountField, gbc);
        
        // Number of Years
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Number of Years:"), gbc);
        gbc.gridx = 1;
        panel.add(yearsField, gbc);
        
        // Return Rate
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Annual Return Rate (%):"), gbc);
        gbc.gridx = 1;
        panel.add(returnRateField, gbc);
        
        // Compounding Frequency
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Compounding Frequency:"), gbc);
        gbc.gridx = 1;
        panel.add(compoundingCombo, gbc);
        
        // Additional Contribution
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Annual Additional Contribution:"), gbc);
        gbc.gridx = 3;
        panel.add(additionalContributionField, gbc);
        
        // Contribution Frequency
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("Contributions per Year:"), gbc);
        gbc.gridx = 3;
        panel.add(contributionFrequencyField, gbc);
        
        // Contribution Timing
        gbc.gridx = 2; gbc.gridy = 2;
        panel.add(new JLabel("Contribution Timing:"), gbc);
        gbc.gridx = 3;
        panel.add(contributionTimingCombo, gbc);
        
        // Currency
        gbc.gridx = 2; gbc.gridy = 3;
        panel.add(new JLabel("Currency:"), gbc);
        gbc.gridx = 3;
        panel.add(currencyCombo, gbc);
        
        // Calculate Button
        JButton calculateButton = new JButton("Calculate Investment");
        calculateButton.setBackground(new Color(0, 123, 255));
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(calculateButton, gbc);
        
        // Add action listener to button
        calculateButton.addActionListener(e -> calculateInvestment());
        
        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Results text area
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Investment Summary"));
        scrollPane.setPreferredSize(new Dimension(400, 180));
        
        // Chart panel
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBorder(BorderFactory.createTitledBorder("Investment Growth Chart"));
        chartContainer.add(chartPanelComponent, BorderLayout.CENTER);
        
        panel.add(scrollPane, BorderLayout.NORTH);
        panel.add(chartContainer, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Detailed Schedule"));
        panel.add(scheduleTabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private void setupEventHandlers() {
        // Add enter key listeners to input fields
        ActionListener calculateAction = e -> calculateInvestment();
        
        startingAmountField.addActionListener(calculateAction);
        yearsField.addActionListener(calculateAction);
        returnRateField.addActionListener(calculateAction);
        additionalContributionField.addActionListener(calculateAction);
        contributionFrequencyField.addActionListener(calculateAction);
    }

    private void calculateInvestment() {
        try {
            // Get and validate input values
            String startingAmountText = startingAmountField.getText().trim();
            String yearsText = yearsField.getText().trim();
            String returnRateText = returnRateField.getText().trim();
            String additionalContributionText = additionalContributionField.getText().trim();
            String contributionsPerYearText = contributionFrequencyField.getText().trim();
            
            // Validate that fields are not empty
            if (startingAmountText.isEmpty() || yearsText.isEmpty() || returnRateText.isEmpty() || 
                additionalContributionText.isEmpty() || contributionsPerYearText.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please fill in all required fields.", 
                    "Missing Input", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Parse and validate numbers
            BigDecimal startingAmount = new BigDecimal(startingAmountText);
            int years = Integer.parseInt(yearsText);
            BigDecimal annualReturnRate = new BigDecimal(returnRateText);
            BigDecimal additionalContribution = new BigDecimal(additionalContributionText);
            int contributionsPerYear = Integer.parseInt(contributionsPerYearText);
            
            // Validate ranges
            if (startingAmount.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, 
                    "Starting amount cannot be negative.", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (years <= 0 || years > 100) {
                JOptionPane.showMessageDialog(this, 
                    "Years must be between 1 and 100.", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (annualReturnRate.compareTo(BigDecimal.valueOf(-100)) < 0 || 
                annualReturnRate.compareTo(BigDecimal.valueOf(1000)) > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Annual return rate must be between -100% and 1000%.", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (additionalContribution.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, 
                    "Additional contribution cannot be negative.", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (contributionsPerYear < 0 || contributionsPerYear > 365) {
                JOptionPane.showMessageDialog(this, 
                    "Contributions per year must be between 0 and 365.", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String compoundingFrequency = (String) compoundingCombo.getSelectedItem();
            String contributionTiming = (String) contributionTimingCombo.getSelectedItem();
            selectedCurrency = ((String) currencyCombo.getSelectedItem()).split("\\s+")[0].trim();
            
            System.out.println("Starting calculation with:");
            System.out.println("Starting Amount: " + startingAmount);
            System.out.println("Years: " + years);
            System.out.println("Return Rate: " + annualReturnRate);
            System.out.println("Additional Contribution: " + additionalContribution);
            System.out.println("Contributions per Year: " + contributionsPerYear);
            
            // Calculate investment
            InvestmentResult result = calculator.calculateInvestment(
                startingAmount, years, annualReturnRate, compoundingFrequency,
                additionalContribution, contributionsPerYear, 
                contributionTiming.equals("Beginning of Period")
            );
            
            // Debug: Check if we have data
            System.out.println("Calculation completed:");
            System.out.println("Monthly data points: " + result.getMonthlyData().size());
            System.out.println("Yearly data points: " + result.getYearlyData().size());
            System.out.println("End balance: " + result.getEndBalance());
            
            // Display results
            displayResults(result);
            
            // Update chart
            updateChart(result);
            
            // Update schedules
            updateSchedules(result);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numbers for all fields.\nError: " + e.getMessage(), 
                "Invalid Input", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error calculating investment: " + e.getMessage(), 
                "Calculation Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void displayResults(InvestmentResult result) {
        String currencySymbol = getCurrencySymbol(selectedCurrency);
        StringBuilder sb = new StringBuilder();
        
        sb.append("<html><body style='font-family: Arial, sans-serif; font-size: 12px; line-height: 1.4; margin: 5px;'>");
        sb.append("<h3 style='color: #2c5aa0; margin-bottom: 10px;'>Investment Results</h3>");
        sb.append(String.format("<b>End Balance:</b> %s%,.2f<br>", currencySymbol, result.getEndBalance()));
        sb.append(String.format("<b>Starting Amount:</b> %s%,.2f<br>", currencySymbol, result.getStartingAmount()));
        sb.append(String.format("<b>Total Contributions:</b> %s%,.2f<br>", currencySymbol, result.getTotalContributions()));
        sb.append(String.format("<b>Total Interest Earned:</b> %s%,.2f<br>", currencySymbol, result.getTotalInterest()));
        sb.append("<hr style='margin: 10px 0;'>");
        sb.append(String.format("<b>Compounding Frequency:</b> %s<br>", result.getCompoundingFrequency()));
        sb.append(String.format("<b>Annual Return Rate:</b> %.2f%%<br>", result.getAnnualReturnRate()));
        sb.append(String.format("<b>Number of Years:</b> %d<br>", result.getYears()));
        sb.append(String.format("<b>Currency:</b> %s<br>", selectedCurrency));
        sb.append("</body></html>");
        
        resultsArea.setText(sb.toString());
    }
    
    private String getCurrencySymbol(String currency) {
        switch (currency.trim()) {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "CAD": return "C$";
            case "AUD": return "A$";
            default: return "$";
        }
    }

    private void updateChart(InvestmentResult result) {
        // This method now simply delegates to the chart panel component
        // The chart panel handles its own updates internally
        chartPanelComponent.updateChart(result, selectedCurrency);
    }

    private void updateSchedules(InvestmentResult result) {
        SwingUtilities.invokeLater(() -> {
            try {
                scheduleTabbedPane.removeAll();
                
                // Annual Schedule
                JTextArea annualSchedule = new JTextArea();
                annualSchedule.setEditable(false);
                annualSchedule.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
                annualSchedule.setText(generateAnnualSchedule(result));
                JScrollPane annualScroll = new JScrollPane(annualSchedule);
                annualScroll.setPreferredSize(new Dimension(800, 200));
                scheduleTabbedPane.addTab("Annual Schedule", annualScroll);
                
                // Monthly Schedule
                JTextArea monthlySchedule = new JTextArea();
                monthlySchedule.setEditable(false);
                monthlySchedule.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
                monthlySchedule.setText(generateMonthlySchedule(result));
                JScrollPane monthlyScroll = new JScrollPane(monthlySchedule);
                monthlyScroll.setPreferredSize(new Dimension(800, 200));
                scheduleTabbedPane.addTab("Monthly Schedule", monthlyScroll);
                
                // Force UI update
                scheduleTabbedPane.revalidate();
                scheduleTabbedPane.repaint();
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error generating schedule: " + e.getMessage(), 
                    "Schedule Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private String generateAnnualSchedule(InvestmentResult result) {
        StringBuilder sb = new StringBuilder();
        String currencySymbol = getCurrencySymbol(selectedCurrency);
        
        sb.append(String.format("%-6s %-18s %-18s %-18s %-18s%n", 
            "Year", "Start Balance", "Contributions", "Interest", "End Balance"));
        sb.append("-".repeat(90)).append("\n");
        
        List<YearlyData> yearlyData = result.getYearlyData();
        for (YearlyData data : yearlyData) {
            sb.append(String.format("%-6d %s%15.2f %s%15.2f %s%15.2f %s%15.2f%n",
                data.getYear(), 
                currencySymbol, data.getStartBalance(), 
                currencySymbol, data.getContributions(),
                currencySymbol, data.getInterestEarned(), 
                currencySymbol, data.getEndBalance()));
        }
        
        return sb.toString();
    }

    private String generateMonthlySchedule(InvestmentResult result) {
        StringBuilder sb = new StringBuilder();
        String currencySymbol = getCurrencySymbol(selectedCurrency);
        
        sb.append(String.format("%-10s %-18s %-18s %-18s %-18s%n", 
            "Month", "Start Balance", "Contributions", "Interest", "End Balance"));
        sb.append("-".repeat(90)).append("\n");
        
        List<MonthlyData> monthlyData = result.getMonthlyData();
        int maxRows = Math.min(60, monthlyData.size()); // Show max 5 years of monthly data
        
        for (int i = 0; i < maxRows; i++) {
            MonthlyData data = monthlyData.get(i);
            sb.append(String.format("%-10s %s%15.2f %s%15.2f %s%15.2f %s%15.2f%n",
                data.getMonth(),
                currencySymbol, data.getStartBalance(), 
                currencySymbol, data.getContributions(),
                currencySymbol, data.getInterestEarned(), 
                currencySymbol, data.getEndBalance()));
        }
        
        if (monthlyData.size() > maxRows) {
            sb.append("\n... (showing first ").append(maxRows).append(" months, total: ").append(monthlyData.size()).append(" months)\n");
        }
        
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new InvestmentCalculator().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Failed to start application: " + e.getMessage(), 
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}