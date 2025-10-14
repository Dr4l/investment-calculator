package com.investmentcalc;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * Custom panel for displaying the investment breakdown as a pie chart.
 */
public class InvestmentPieChartPanel extends JPanel {
    private JFreeChart chart;
    private org.jfree.chart.ChartPanel jfreeChartPanel;

    public InvestmentPieChartPanel(JFreeChart chart) {
        this.chart = chart;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("End Balance Breakdown"));

        if (chart != null) {
            jfreeChartPanel = new org.jfree.chart.ChartPanel(chart);
            add(jfreeChartPanel, BorderLayout.CENTER);
        } else {
            showPlaceholder("Pie chart will appear here.");
        }
    }

    private void showPlaceholder(String message) {
        removeAll();
        JLabel placeholder = new JLabel(message, SwingConstants.CENTER);
        placeholder.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        placeholder.setForeground(Color.GRAY);
        add(placeholder, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void updateChart(InvestmentResult result, String currency) {
        // Only show the pie chart if additional contributions are positive.
        BigDecimal additionalContributions = result.getTotalContributions().subtract(result.getStartingAmount());
        if (additionalContributions.compareTo(BigDecimal.ZERO) < 0) {
            showPlaceholder("Pie chart is not shown for withdrawals.");
            this.chart = null; // Clear the chart
            return;
        }

        removeAll();

        // Create and display the new pie chart
        this.chart = createPieChart(result, currency);
        jfreeChartPanel = new org.jfree.chart.ChartPanel(chart);
        add(jfreeChartPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JFreeChart createPieChart(InvestmentResult result, String currency) {
        PieDataset dataset = createDataset(result);

        JFreeChart pieChart = ChartFactory.createPieChart(
                "End Balance Composition", // Chart title
                dataset,                   // Data
                true,                      // Include legend
                true,                      // Generate tooltips
                false);                    // No URLs

        customizePieChart(pieChart, currency);
        return pieChart;
    }

    private PieDataset createDataset(InvestmentResult result) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        // The pie chart shows the breakdown of the final amount
        BigDecimal additionalContributions = result.getTotalContributions().subtract(result.getStartingAmount());

        dataset.setValue("Starting Amount", result.getStartingAmount());
        dataset.setValue("Additional Contributions", additionalContributions);
        dataset.setValue("Interest Earned", result.getTotalInterest());

        return dataset;
    }

    private void customizePieChart(JFreeChart chart, String currency) {
        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setShadowPaint(null);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 180));

        // Set colors to match the line chart for consistency
        plot.setSectionPaint("Starting Amount", new Color(40, 167, 69));      // Green
        plot.setSectionPaint("Additional Contributions", new Color(0, 123, 255)); // Blue
        plot.setSectionPaint("Interest Earned", new Color(255, 193, 7));      // Yellow

        // Customize labels to show currency value and percentage
        StandardPieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})",
                NumberFormat.getCurrencyInstance(getLocaleForCurrency(currency)),
                NumberFormat.getPercentInstance()
        );
        plot.setLabelGenerator(labelGenerator);

        // Set legend and title fonts
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        }
        chart.getTitle().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
    }

    private java.util.Locale getLocaleForCurrency(String currency) {
        switch (currency.trim()) {
            case "USD": return java.util.Locale.US;
            case "EUR": return java.util.Locale.FRANCE;
            case "GBP": return java.util.Locale.UK;
            case "JPY": return java.util.Locale.JAPAN;
            case "CAD": return java.util.Locale.CANADA;
            case "AUD": return java.util.Locale.forLanguageTag("en-AU");
            default: return java.util.Locale.US;
        }
    }
}