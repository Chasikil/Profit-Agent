package com.restaurant.pos.ui.model;

import java.math.BigDecimal;

/**
 * DTO for sales report.
 * Contains financial metrics for sales operations.
 */
public class SalesReport {

    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal grossProfit;
    private BigDecimal marginPercent;

    public SalesReport() {
        this.totalRevenue = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
        this.grossProfit = BigDecimal.ZERO;
        this.marginPercent = BigDecimal.ZERO;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost != null ? totalCost : BigDecimal.ZERO;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit != null ? grossProfit : BigDecimal.ZERO;
    }

    public BigDecimal getMarginPercent() {
        return marginPercent;
    }

    public void setMarginPercent(BigDecimal marginPercent) {
        this.marginPercent = marginPercent != null ? marginPercent : BigDecimal.ZERO;
    }
}
