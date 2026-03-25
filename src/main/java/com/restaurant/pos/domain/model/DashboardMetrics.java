package com.restaurant.pos.domain.model;

import java.math.BigDecimal;

public class DashboardMetrics {

    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal salaryExpenses;
    private BigDecimal netProfit;
    private BigDecimal averageMargin;
    private BigDecimal foodCostPercent;

    public DashboardMetrics() { }

    public BigDecimal getTotalRevenue() { return totalRevenue; }

    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getTotalCost() { return totalCost; }

    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public BigDecimal getSalaryExpenses() { return salaryExpenses; }

    public void setSalaryExpenses(BigDecimal salaryExpenses) { this.salaryExpenses = salaryExpenses; }

    public BigDecimal getNetProfit() { return netProfit; }

    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }

    public BigDecimal getAverageMargin() { return averageMargin; }

    public void setAverageMargin(BigDecimal averageMargin) { this.averageMargin = averageMargin; }

    public BigDecimal getFoodCostPercent() { return foodCostPercent; }

    public void setFoodCostPercent(BigDecimal foodCostPercent) { this.foodCostPercent = foodCostPercent; }
}
