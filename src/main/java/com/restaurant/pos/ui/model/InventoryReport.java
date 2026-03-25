package com.restaurant.pos.ui.model;

import java.math.BigDecimal;

/**
 * DTO for inventory report.
 * Contains inventory financial metrics.
 */
public class InventoryReport {

    private BigDecimal totalStockValue;
    private BigDecimal totalWriteOffs;

    public InventoryReport() {
        this.totalStockValue = BigDecimal.ZERO;
        this.totalWriteOffs = BigDecimal.ZERO;
    }

    public BigDecimal getTotalStockValue() {
        return totalStockValue;
    }

    public void setTotalStockValue(BigDecimal totalStockValue) {
        this.totalStockValue = totalStockValue != null ? totalStockValue : BigDecimal.ZERO;
    }

    public BigDecimal getTotalWriteOffs() {
        return totalWriteOffs;
    }

    public void setTotalWriteOffs(BigDecimal totalWriteOffs) {
        this.totalWriteOffs = totalWriteOffs != null ? totalWriteOffs : BigDecimal.ZERO;
    }
}
