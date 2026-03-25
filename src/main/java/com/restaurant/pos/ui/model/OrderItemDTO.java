package com.restaurant.pos.ui.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.math.BigDecimal;

/**
 * DTO for order item with JavaFX observable properties for UI binding.
 * Automatically calculates total when quantity or price changes.
 */
public class OrderItemDTO {

    private Long dishId;
    private final StringProperty dishName = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> price = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> total = new SimpleObjectProperty<>();

    public OrderItemDTO() {
        // Auto-calculate total when quantity or price changes
        quantity.addListener((obs, oldVal, newVal) -> calculateTotal());
        price.addListener((obs, oldVal, newVal) -> calculateTotal());
    }

    private void calculateTotal() {
        BigDecimal priceValue = price.get();
        int quantityValue = quantity.get();
        if (priceValue != null && quantityValue > 0) {
            total.set(priceValue.multiply(BigDecimal.valueOf(quantityValue)));
        } else {
            total.set(BigDecimal.ZERO);
        }
    }

    // Dish ID (not observable, used for identification)
    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }

    // Dish name (observable)
    public String getDishName() { return dishName.get(); }
    public void setDishName(String dishName) { this.dishName.set(dishName); }
    public StringProperty dishNameProperty() { return dishName; }

    // Quantity (observable)
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public IntegerProperty quantityProperty() { return quantity; }

    // Price per unit (observable)
    public BigDecimal getPrice() { return price.get(); }
    public void setPrice(BigDecimal price) { this.price.set(price); }
    public ObjectProperty<BigDecimal> priceProperty() { return price; }

    // Total price (observable, auto-calculated)
    public BigDecimal getTotal() { return total.get(); }
    public void setTotal(BigDecimal total) { this.total.set(total); }
    public ObjectProperty<BigDecimal> totalProperty() { return total; }
}
