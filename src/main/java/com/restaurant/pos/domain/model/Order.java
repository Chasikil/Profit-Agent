package com.restaurant.pos.domain.model;

import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private Long id;
    /**
     * Legacy waiter identifier kept for reporting and compatibility.
     * Prefer using {@link #waiter} reference when working with domain model.
     */
    private Long waiterId;
    private Long shiftId;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private OrderStatus status;
    private BigDecimal costPrice;
    private BigDecimal margin;
    /** Total ingredient cost for this order (set when paid). */
    private BigDecimal totalCost;
    /** Order profit: orderTotalPrice - totalCost (set when paid). */
    private BigDecimal totalProfit;
    /**
     * Direct reference to waiter employee who owns this order.
     */
    private Employee waiter;
    private final List<OrderItem> items = new ArrayList<>();

    /** Order is completed only when paid = true. */
    private boolean paid;
    private PaymentMethod paymentMethod;
    private BigDecimal amountPaid;
    private BigDecimal change;
    private Table table;

    public Order() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWaiterId() {
        return waiterId;
    }

    public void setWaiterId(Long waiterId) {
        this.waiterId = waiterId;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Employee getWaiter() {
        return waiter;
    }

    public void setWaiter(Employee waiter) {
        this.waiter = waiter;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getChange() {
        return change;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    /**
     * Get list of products consumed by this order.
     * This is a placeholder method for future inventory logic.
     * Currently returns an empty list.
     * 
     * @return empty list (placeholder for inventory consumption tracking)
     */
    public List<Product> getConsumedProducts() {
        // TODO: Implement inventory consumption logic
        // This should calculate consumed products based on order items and their tech cards
        return new ArrayList<>();
    }
}

