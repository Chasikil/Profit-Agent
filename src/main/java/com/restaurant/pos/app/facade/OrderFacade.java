package com.restaurant.pos.app.facade;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.service.MenuService;
import com.restaurant.pos.service.OrderPricingService;
import com.restaurant.pos.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

public class OrderFacade {

    private final OrderService orderService;
    private final MenuService menuService;
    private final OrderPricingService orderPricingService;

    public OrderFacade(OrderService orderService,
                      MenuService menuService,
                      OrderPricingService orderPricingService) {
        this.orderService = orderService;
        this.menuService = menuService;
        this.orderPricingService = orderPricingService;
    }

    public Order createOrder(Long waiterId) {
        if (waiterId == null) {
            throw new IllegalArgumentException("waiterId must not be null when creating an order");
        }
        Employee waiter = new Employee();
        waiter.setId(waiterId);
        return orderService.createOrder(waiter);
    }

    public Order getActiveOrder(Long waiterId) {
        List<Order> orders = orderService.getOrdersByWaiter(waiterId);
        return orders.stream()
                .filter(order -> order.getStatus() == com.restaurant.pos.domain.enums.OrderStatus.OPEN)
                .findFirst()
                .orElse(null);
    }

    public void addDish(Long orderId, Long dishId, int quantity) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return;
        }
        Dish dish = findDishById(dishId);
        if (dish == null) {
            return;
        }
        orderService.addDish(order, dish, quantity);
    }

    public void removeDish(Long orderId, Long dishId) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return;
        }
        Dish dish = findDishById(dishId);
        if (dish == null) {
            return;
        }
        orderService.removeDish(order, dish);
    }

    public BigDecimal getOrderTotal(Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return BigDecimal.ZERO;
        }
        return orderPricingService.calculateTotal(order);
    }

    public void closeOrder(Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return;
        }
        order.setStatus(com.restaurant.pos.domain.enums.OrderStatus.PAID);
    }

    public List<Dish> getActiveMenuDishes() {
        return menuService.getActiveMenu().getDishes();
    }

    private Dish findDishById(Long dishId) {
        return menuService.getActiveMenu().getDishes().stream()
                .filter(d -> d.getId() != null && d.getId().equals(dishId))
                .findFirst()
                .orElse(null);
    }
}
