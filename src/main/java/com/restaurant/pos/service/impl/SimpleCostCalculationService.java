package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.DishCost;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderItem;
import com.restaurant.pos.service.CostCalculationService;
import com.restaurant.pos.service.TechCardService;

import java.math.BigDecimal;

public class SimpleCostCalculationService implements CostCalculationService {

    private final TechCardService techCardService;

    public SimpleCostCalculationService(TechCardService techCardService) {
        this.techCardService = techCardService;
    }

    @Override
    public DishCost calculateDishCost(Dish dish) {
        DishCost dishCost = new DishCost();
        if (dish == null || dish.getId() == null) {
            return dishCost;
        }
        dishCost.setDishId(dish.getId());
        dishCost.setSalePrice(dish.getSalePrice() != null ? dish.getSalePrice() : BigDecimal.ZERO);
        BigDecimal costPrice = calculateCostPrice(dish);
        dishCost.setCostPrice(costPrice);
        BigDecimal margin = dishCost.getSalePrice().subtract(costPrice);
        dishCost.setMargin(margin);
        if (dishCost.getSalePrice().signum() > 0) {
            BigDecimal marginPercent = margin.divide(dishCost.getSalePrice(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            dishCost.setMarginPercent(marginPercent);
        } else {
            dishCost.setMarginPercent(BigDecimal.ZERO);
        }
        return dishCost;
    }

    @Override
    public BigDecimal calculateOrderCost(Order order) {
        if (order == null || order.getItems() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalCost = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getDish() == null) {
                continue;
            }
            DishCost dishCost = calculateDishCost(item.getDish());
            BigDecimal itemCost = dishCost.getCostPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalCost = totalCost.add(itemCost);
        }
        return totalCost;
    }

    private BigDecimal calculateCostPrice(Dish dish) {
        // Упрощенный расчет: если есть техкарта, считаем по ней, иначе возвращаем 0
        // В реальной системе здесь был бы расчет по техкарте и стоимости продуктов
        return BigDecimal.ZERO;
    }
}
