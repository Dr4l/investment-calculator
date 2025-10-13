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

    public InvestmentCalculator() {
        initializeLookAndFeel();
        calculator = new FinalInvestmentEngine();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Investment Calculator");
        setSize(1000, 700); // Smaller window that requires scrolling
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
        resultsArea.setPreferredSize(new Dimension(350, 180));
        
        // Chart panel
        chartPanelComponent = new InvestmentChartPanel(null);
        
        // Schedule tabbed pane
        scheduleTabbedPane = new JTabbedPane();
    }

    private void setupLayout() {
        // Use a scroll pane as the main container
        JScrollPane mainScrollPane = new JScrollPane(createMainContent());
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        setLayout(new BorderLayout());
        add(mainScrollPane, BorderLayout.CENTER);
    }

    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add components with rigid areas to prevent stretching
        mainPanel.add(createInputPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createResultsPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createBottomPanel());
        
        return mainPanel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Investment Parameters"));
        panel.setBackground(new Color(245, 245, 245));
        panel.setMaximumSize(new Dimension(950, 220)); // Fixed maximum size
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
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
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Investment Summary"));
        panel.setMaximumSize(new Dimension(950, 400)); // Fixed maximum size
        
        // Results area - align to top
        JScrollPane resultsScrollPane = new JScrollPane(resultsArea);
        resultsScrollPane.setPreferredSize(new Dimension(350, 180));
        resultsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Chart panel - fixed size
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBorder(BorderFactory.createTitledBorder("Investment Growth Chart"));
        chartContainer.setPreferredSize(new Dimension(550, 350));
        chartContainer.add(chartPanelComponent, BorderLayout.CENTER);
        
        // Use a split pane to keep results at top and chart below
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsScrollPane, chartContainer);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.5);
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Detailed Schedule"));
        panel.setMaximumSize(new Dimension(950, 300)); // Fixed maximum size
        
        scheduleTabbedPane.setPreferredSize(new Dimension(900, 250));
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
            
            // Calculate investment
            InvestmentResult result = calculator.calculateInvestment(
                startingAmount, years, annualReturnRate, compoundingFrequency,
                additionalContribution, contributionsPerYear, 
                contributionTiming.equals("Beginning of Period")
            );
            
            // Format end balance to 2 decimal places for display
            BigDecimal formattedEndBalance = result.getEndBalance().setScale(2, RoundingMode.HALF_UP);
            BigDecimal formattedTotalContributions = result.getTotalContributions().setScale(2, RoundingMode.HALF_UP);
            BigDecimal formattedTotalInterest = result.getTotalInterest().setScale(2, RoundingMode.HALF_UP);
            
            // Display results
            displayResults(result, formattedEndBalance, formattedTotalContributions, formattedTotalInterest);
            
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

    private void displayResults(InvestmentResult result, BigDecimal formattedEndBalance, 
                              BigDecimal formattedTotalContributions, BigDecimal formattedTotalInterest) {
        String currencySymbol = getCurrencySymbol(selectedCurrency);
        StringBuilder sb = new StringBuilder();
        
        // Investment results at the very top of the summary area
        sb.append("<html><body style='font-family: Arial, sans-serif; margin: 0; padding: 0;'>");
        
        // Main results - always at the top
        sb.append("<div style='padding: 10px; border-bottom: 1px solid #eee;'>");
        sb.append("<div style='font-size: 18px; font-weight: bold; color: #2c5aa0; margin-bottom: 10px;'>Investment Results</div>");
        sb.append(String.format("<div style='font-size: 16px; font-weight: bold; color: #28a745; margin-bottom: 8px;'>End Balance: %s%,.2f</div>", currencySymbol, formattedEndBalance));
        sb.append(String.format("<div style='margin-bottom: 5px;'><b>Starting Amount:</b> %s%,.2f</div>", currencySymbol, result.getStartingAmount()));
        sb.append(String.format("<div style='margin-bottom: 5px;'><b>Total Contributions:</b> %s%,.2f</div>", currencySymbol, formattedTotalContributions));
        sb.append(String.format("<div style='margin-bottom: 5px;'><b>Total Interest Earned:</b> %s%,.2f</div>", currencySymbol, formattedTotalInterest));
        sb.append("</div>");
        
        // Additional details below (will scroll if needed)
        sb.append("<div style='padding: 10px; font-size: 12px; color: #666;'>");
        sb.append("<div style='font-weight: bold; margin-bottom: 5px;'>Details:</div>");
        sb.append(String.format("<div style='margin-bottom: 3px;'>Compounding Frequency: %s</div>", result.getCompoundingFrequency()));
        sb.append(String.format("<div style='margin-bottom: 3px;'>Annual Return Rate: %.2f%%</div>", result.getAnnualReturnRate()));
        sb.append(String.format("<div style='margin-bottom: 3px;'>Number of Years: %d</div>", result.getYears()));
        sb.append(String.format("<div style='margin-bottom: 3px;'>Currency: %s</div>", selectedCurrency));
        sb.append("</div>");
        
        sb.append("</body></html>");
        
        resultsArea.setText(sb.toString());
        // Scroll to top to ensure results are visible
        resultsArea.setCaretPosition(0);
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
                scheduleTabbedPane.addTab("Annual Schedule", new JScrollPane(annualSchedule));
                
                // Monthly Schedule - Show ALL months
                JTextArea monthlySchedule = new JTextArea();
                monthlySchedule.setEditable(false);
                monthlySchedule.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
                monthlySchedule.setText(generateMonthlySchedule(result));
                scheduleTabbedPane.addTab("Monthly Schedule", new JScrollPane(monthlySchedule));
                
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
                currencySymbol, data.getStartBalance().setScale(2, RoundingMode.HALF_UP), 
                currencySymbol, data.getContributions().setScale(2, RoundingMode.HALF_UP),
                currencySymbol, data.getInterestEarned().setScale(2, RoundingMode.HALF_UP), 
                currencySymbol, data.getEndBalance().setScale(2, RoundingMode.HALF_UP)));
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
        
        // Show ALL monthly data - no limit
        for (int i = 0; i < monthlyData.size(); i++) {
            MonthlyData data = monthlyData.get(i);
            sb.append(String.format("%-10s %s%15.2f %s%15.2f %s%15.2f %s%15.2f%n",
                data.getMonth(),
                currencySymbol, data.getStartBalance().setScale(2, RoundingMode.HALF_UP), 
                currencySymbol, data.getContributions().setScale(2, RoundingMode.HALF_UP),
                currencySymbol, data.getInterestEarned().setScale(2, RoundingMode.HALF_UP), 
                currencySymbol, data.getEndBalance().setScale(2, RoundingMode.HALF_UP)));
        }
        
        // Add summary at the end
        sb.append("-".repeat(90)).append("\n");
        sb.append(String.format("Total months: %d%n", monthlyData.size()));
        
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