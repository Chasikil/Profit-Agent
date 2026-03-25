package com.restaurant.pos.domain.model;

import java.util.ArrayList;
import java.util.List;

public class TechCard {

    private Long id;
    private Long dishId;
    private String dishName;
    private final List<TechCardItem> items = new ArrayList<>();

    public TechCard() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public List<TechCardItem> getItems() {
        return items;
    }
}

