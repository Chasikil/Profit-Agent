package com.restaurant.pos.service;

import com.restaurant.pos.domain.enums.PaymentMethod;
import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    /**
     * Result of closing an order operation.
     */
    class CloseOrderResult {
        private final boolean success;
        private final String errorMessage;

        private CloseOrderResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static CloseOrderResult success() {
            return new CloseOrderResult(true, null);
        }

        public static CloseOrderResult failure(String errorMessage) {
            return new CloseOrderResult(false, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    Order createOrder(Employee waiter);

    void addDish(Order order, Dish dish, int quantity);

    void removeDish(Order order, Dish dish);

    /**
     * Pay order: validate payment, set paid state and record INCOME.
     * Order must not already be paid. For CASH, amountPaid must be >= order total; change is calculated.
     * For CARD, change is 0 (amountPaid should equal order total).
     *
     * @param order      order to pay
     * @param method     CASH or CARD
     * @param amountPaid amount tendered (for CASH must be >= total)
     * @throws IllegalArgumentException if order already paid, or insufficient amount for CASH
     */
    void payOrder(Order order, PaymentMethod method, BigDecimal amountPaid);

    /**
     * Close order: requires order to be paid; check ingredient availability and write off from inventory.
     *
     * @param order order to close (must be paid first via payOrder)
     * @return CloseOrderResult with success status and error message if failed
     */
    CloseOrderResult closeOrder(Order order);

    Order getOrderById(Long orderId);

    List<Order> getOrdersByWaiter(Long waiterId);

    List<Order> getOrdersByPeriod(LocalDateTime start, LocalDateTime end);

    /**
     * Get all orders.
     * 
     * @return list of all orders
     */
    List<Order> getAllOrders();

    /**
     * Persist order changes (e.g., table assignment) to storage if enabled.
     *
     * @param order order to save
     */
    void saveOrder(Order order);

    /**
     * Check if there is an active (OPEN) order for the given table number.
     *
     * @param tableNumber table number
     * @return true if there is at least one OPEN order for this table, false otherwise
     */
    boolean hasActiveOrder(int tableNumber);

    /**
     * Cancel order: set status to CANCELLED and persist. Caller must restore inventory and free table.
     *
     * @param order order to cancel
     */
    void cancelOrder(Order order);
}

