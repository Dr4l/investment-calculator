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
    private JPanel chartPanel;
    private JTabbedPane scheduleTabbedPane;
    
    private FinalInvestmentEngine calculator;
    private ChartPanel chartPanelComponent;
    private String selectedCurrency = "USD";

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
        resultsArea.setPreferredSize(new Dimension(400, 200));
        
        // Chart panel
        chartPanel = new JPanel(new BorderLayout());
        chartPanelComponent = new ChartPanel(null);
        chartPanel.add(chartPanelComponent, BorderLayout.CENTER);
        
        // Schedule tabbed pane
        scheduleTabbedPane = new JTabbedPane();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create main panels
        JPanel inputPanel = createInputPanel();
        JPanel resultsPanel = createResultsPanel();
        JPanel bottomPanel = createBottomPanel();
        
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
        
        // Starting Amount
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Starting Amount ($):"), gbc);
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
        panel.add(new JLabel("Annual Additional Contribution ($):"), gbc);
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
        JPanel panel = new JPanel(new BorderLayout());
        
        // Results text area
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Investment Summary"));
        
        panel.add(scrollPane, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
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
            selectedCurrency = ((String) currencyCombo.getSelectedItem()).split("\\(")[0]; // Extract currency code
            
            // Calculate investment
            InvestmentResult result = calculator.calculateInvestment(
                startingAmount, years, annualReturnRate, compoundingFrequency,
                additionalContribution, contributionsPerYear, 
                contributionTiming.equals("Beginning of Period")
            );
            
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error calculating investment: " + e.getMessage() + "\n" + e.getClass().getSimpleName(), 
                "Calculation Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    private void displayResults(InvestmentResult result) {
        String currencySymbol = getCurrencySymbol(selectedCurrency);
        StringBuilder sb = new StringBuilder();
        
        // Key results in bold using HTML formatting
        sb.append("<html><body style='font-family: monospace; font-size: 12px; line-height: 1.4;'>");
        sb.append(String.format("<b style='font-size: 14px; color: #2c5aa0;'>End Balance: %s%,.2f</b><br>", currencySymbol, result.getEndBalance()));
        sb.append(String.format("<b style='font-size: 14px; color: #2c5aa0;'>Starting Amount: %s%,.2f</b><br>", currencySymbol, result.getStartingAmount()));
        sb.append(String.format("<b style='font-size: 14px; color: #2c5aa0;'>Total Contributions: %s%,.2f</b><br>", currencySymbol, result.getTotalContributions()));
        sb.append(String.format("<b style='font-size: 14px; color: #2c5aa0;'>Total Interest: %s%,.2f</b><br>", currencySymbol, result.getTotalInterest()));
        sb.append("<br>");
        
        // Additional details in normal text
        sb.append(String.format("Compounding Frequency: %s<br>", result.getCompoundingFrequency()));
        sb.append(String.format("Annual Return Rate: %.2f%%<br>", result.getAnnualReturnRate()));
        sb.append(String.format("Number of Years: %d<br>", result.getYears()));
        sb.append(String.format("Currency: %s<br>", selectedCurrency));
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
        chartPanelComponent.updateChart(result, selectedCurrency);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void updateSchedules(InvestmentResult result) {
        try {
            scheduleTabbedPane.removeAll();
            
            // Annual Schedule
            JTextArea annualSchedule = new JTextArea();
            annualSchedule.setEditable(false);
            annualSchedule.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            annualSchedule.setText(generateAnnualSchedule(result));
            scheduleTabbedPane.addTab("Annual Schedule", new JScrollPane(annualSchedule));
            
            // Monthly Schedule
            JTextArea monthlySchedule = new JTextArea();
            monthlySchedule.setEditable(false);
            monthlySchedule.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            monthlySchedule.setText(generateMonthlySchedule(result));
            scheduleTabbedPane.addTab("Monthly Schedule", new JScrollPane(monthlySchedule));
        } catch (Exception e) {
            // If schedule generation fails, show error message instead of breaking the GUI
            JTextArea errorSchedule = new JTextArea();
            errorSchedule.setEditable(false);
            errorSchedule.setText("Error generating schedule: " + e.getMessage());
            scheduleTabbedPane.removeAll();
            scheduleTabbedPane.addTab("Error", new JScrollPane(errorSchedule));
            e.printStackTrace();
        }
    }

    private String generateAnnualSchedule(InvestmentResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-5s %-15s %-15s %-15s %-15s%n", 
            "Year", "Start Balance", "Contributions", "Interest", "End Balance"));
        sb.append("-".repeat(80)).append("\n");
        
        List<YearlyData> yearlyData = result.getYearlyData();
        for (YearlyData data : yearlyData) {
            sb.append(String.format("%-5d $%14.2f $%14.2f $%14.2f $%14.2f%n",
                data.getYear(), data.getStartBalance(), data.getContributions(),
                data.getInterestEarned(), data.getEndBalance()));
        }
        
        return sb.toString();
    }

    private String generateMonthlySchedule(InvestmentResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-8s %-15s %-15s %-15s %-15s%n", 
            "Month", "Start Balance", "Contributions", "Interest", "End Balance"));
        sb.append("-".repeat(80)).append("\n");
        
        List<MonthlyData> monthlyData = result.getMonthlyData();
        int maxRows = Math.min(120, monthlyData.size()); // Show max 10 years of monthly data
        
        for (int i = 0; i < maxRows; i++) {
            MonthlyData data = monthlyData.get(i);
            sb.append(String.format("%-8s $%14.2f $%14.2f $%14.2f $%14.2f%n",
                data.getMonth(), data.getStartBalance(), data.getContributions(),
                data.getInterestEarned(), data.getEndBalance()));
        }
        
        if (monthlyData.size() > maxRows) {
            sb.append("... (showing first ").append(maxRows).append(" months)\n");
        }
        
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new InvestmentCalculator().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
