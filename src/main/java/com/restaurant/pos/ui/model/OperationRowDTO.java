package com.restaurant.pos.ui.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OperationRowDTO {

    private LocalDateTime time;
    private String operationType;
    private BigDecimal amount;
    private String employee;

    public OperationRowDTO() {
    }

    public OperationRowDTO(LocalDateTime time, String operationType, BigDecimal amount, String employee) {
        this.time = time;
        this.operationType = operationType;
        this.amount = amount;
        this.employee = employee;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }
}
