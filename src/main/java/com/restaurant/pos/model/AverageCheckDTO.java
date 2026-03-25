package com.restaurant.pos.model;

import java.math.BigDecimal;

public class AverageCheckDTO {

    private BigDecimal averageCheck;

    public AverageCheckDTO() {
    }

    public AverageCheckDTO(BigDecimal averageCheck) {
        this.averageCheck = averageCheck;
    }

    public BigDecimal getAverageCheck() {
        return averageCheck;
    }

    public void setAverageCheck(BigDecimal averageCheck) {
        this.averageCheck = averageCheck;
    }
}

