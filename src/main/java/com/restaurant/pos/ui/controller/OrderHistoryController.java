package com.restaurant.pos.ui.controller;

import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.service.EmployeeStorageService;
import com.restaurant.pos.service.OrderPricingService;
import com.restaurant.pos.service.OrderService;
import com.restaurant.pos.db.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class OrderHistoryController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderPricingService orderPricingService;
    private final EmployeeStorageService employeeStorageService;

    public OrderHistoryController(OrderService orderService, OrderRepository orderRepository,
                                  OrderPricingService orderPricingService, EmployeeStorageService employeeStorageService) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.orderPricingService = orderPricingService;
        this.employeeStorageService = employeeStorageService;
    }

    public List<Order> getOrders(LocalDate dateFrom, LocalDate dateTo, Long waiterId, OrderStatus status) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;
        return orderRepository.findByFilter(from, to, waiterId, status);
    }

    public String getWaiterName(Long waiterId) {
        if (waiterId == null) return "—";
        var e = employeeStorageService.getEmployeeById(waiterId);
        return e != null ? e.getFullName() : "Waiter #" + waiterId;
    }

    public BigDecimal getOrderTotal(Order order) {
        return orderPricingService != null ? orderPricingService.calculateTotal(order) : BigDecimal.ZERO;
    }

    public List<com.restaurant.pos.domain.model.Employee> getAllWaiters() {
        return employeeStorageService != null ? employeeStorageService.getAllEmployees() : List.of();
    }
}
