package com.restaurant.pos.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    private final List<Dish> dishes = new ArrayList<>();
    private boolean active;

    public Menu() {
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

