package com.investmentcalc;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Custom chart panel for displaying investment growth over time
 */
public class InvestmentChartPanel extends JPanel {
    private JFreeChart chart;
    private org.jfree.chart.ChartPanel jfreeChartPanel;
    
    public InvestmentChartPanel(JFreeChart chart) {
        this.chart = chart;
        setLayout(new BorderLayout());
        
        if (chart != null) {
            jfreeChartPanel = new org.jfree.chart.ChartPanel(chart);
            jfreeChartPanel.setPreferredSize(new Dimension(600, 400));
            add(jfreeChartPanel, BorderLayout.CENTER);
        } else {
            // Show placeholder when no chart is available
            JLabel placeholder = new JLabel("Chart will appear here after calculation", 
                SwingConstants.CENTER);
            placeholder.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
            placeholder.setForeground(Color.GRAY);
            add(placeholder, BorderLayout.CENTER);
        }
    }
    
    public void updateChart(InvestmentResult result, String currency) {
        // Remove all components
        removeAll();
        
        // Create new chart
        chart = createInvestmentChart(result, currency);
        jfreeChartPanel = new org.jfree.chart.ChartPanel(chart);
        jfreeChartPanel.setPreferredSize(new Dimension(600, 400));
        add(jfreeChartPanel, BorderLayout.CENTER);
        
        // Update display
        revalidate();
        repaint();
    }
    
    private JFreeChart createInvestmentChart(InvestmentResult result, String currency) {
        // Create datasets for different lines
        XYSeries balanceSeries = new XYSeries("Total Balance");
        XYSeries contributionsSeries = new XYSeries("Cumulative Additional Contributions"); // Changed for clarity
        XYSeries interestSeries = new XYSeries("Total Interest");
        
        List<YearlyData> yearlyData = result.getYearlyData();
        
        // Initialize cumulative values
        BigDecimal cumulativeInterest = BigDecimal.ZERO;
        // This tracks ONLY additional contributions, starting from zero
        BigDecimal cumulativeContributions = BigDecimal.ZERO;
        
        // Add initial data points at Year 0 for a clean start
        balanceSeries.add(0, result.getStartingAmount().doubleValue());
        contributionsSeries.add(0, 0.0); // Additional contributions are 0 at the start
        interestSeries.add(0, 0.0);
        
        for (YearlyData data : yearlyData) {
            int year = data.getYear();
            
            // Update cumulative values for each year
            cumulativeInterest = cumulativeInterest.add(data.getInterestEarned());
            cumulativeContributions = cumulativeContributions.add(data.getContributions());
            
            balanceSeries.add(year, data.getEndBalance().doubleValue());
            contributionsSeries.add(year, cumulativeContributions.doubleValue());
            interestSeries.add(year, cumulativeInterest.doubleValue());
        }
        
        // Create dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(balanceSeries);
        dataset.addSeries(contributionsSeries);
        dataset.addSeries(interestSeries);
        
        // Create chart with dynamic currency label
        String currencySymbol = getCurrencySymbol(currency);
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Investment Growth Over Time",
            "Years",
            "Amount (" + currencySymbol + ")",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Customize chart appearance
        customizeChart(chart, currency);
        
        return chart;
    }
    
    private void customizeChart(JFreeChart chart, String currency) {
        // Set chart background
        chart.setBackgroundPaint(Color.WHITE);
        
        // Get the plot
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        // Customize the renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        
        // Set colors for different series
        renderer.setSeriesPaint(0, new Color(0, 123, 255)); // Total Balance - Blue
        renderer.setSeriesPaint(1, new Color(40, 167, 69)); // Contributions - Green
        renderer.setSeriesPaint(2, new Color(255, 193, 7)); // Interest - Yellow
        
        // Set line thickness
        renderer.setDefaultStroke(new BasicStroke(2.0f));
        
        plot.setRenderer(renderer);
        
        // Customize Y-axis to show currency format based on selected currency
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        java.util.Locale locale = getLocaleForCurrency(currency);
        rangeAxis.setNumberFormatOverride(java.text.NumberFormat.getCurrencyInstance(locale));
        
        // Customize X-axis
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        // Set chart title font
        chart.getTitle().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
        // Set legend font
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        }
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

    public JFreeChart getCurrentChart() {
    return chart;
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