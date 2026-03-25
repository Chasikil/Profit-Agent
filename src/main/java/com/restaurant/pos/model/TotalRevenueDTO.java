package com.restaurant.pos.model;

import java.math.BigDecimal;

public class TotalRevenueDTO {

    private BigDecimal totalRevenue;

    public TotalRevenueDTO() {
    }

    public TotalRevenueDTO(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}

