package com.packflow.model;

import java.math.BigDecimal;

/**
 * Data point for rendering dynamic Chart.js visualizations.
 */
public class ChartPoint {
    private String label;
    private double value;
    private String secondaryLabel;

    public ChartPoint() {
    }

    public ChartPoint(String label, double value) {
        this.label = label;
        this.value = value;
    }

    public ChartPoint(String label, double value, String secondaryLabel) {
        this.label = label;
        this.value = value;
        this.secondaryLabel = secondaryLabel;
    }

    public static ChartPoint of(String label, BigDecimal value) {
        return new ChartPoint(label, value != null ? value.doubleValue() : 0.0);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getSecondaryLabel() {
        return secondaryLabel;
    }

    public void setSecondaryLabel(String secondaryLabel) {
        this.secondaryLabel = secondaryLabel;
    }
}
