package com.restaurant.pos.ui.controller;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Menu;
import com.restaurant.pos.service.MenuService;
import com.restaurant.pos.ui.context.SessionContext;
import com.restaurant.pos.ui.view.MenuView;

import java.util.List;

public class MenuController {

    private final MenuService menuService;
    private final SessionContext sessionContext;
    private MenuView view;

    public MenuController(MenuService menuService, SessionContext sessionContext) {
        this.menuService = menuService;
        this.sessionContext = sessionContext;
    }

    public MenuView getView() {
        if (view == null) {
            view = new MenuView(this);
            loadMenu();
            updateUIState();
        }
        return view;
    }

    public void loadMenu() {
        Menu activeMenu = menuService.getActiveMenu();
        List<Dish> dishes = activeMenu != null ? activeMenu.getDishes() : List.of();
        if (view != null) {
            // Check availability for each dish
            java.util.Map<Long, Boolean> availabilityMap = new java.util.HashMap<>();
            for (Dish dish : dishes) {
                if (dish != null && dish.getId() != null) {
                    boolean isAvailable = menuService.isDishAvailable(dish.getId());
                    availabilityMap.put(dish.getId(), isAvailable);
                }
            }
            view.updateTableWithAvailability(dishes, availabilityMap);
        }
    }

    public void refreshMenu() {
        loadMenu();
    }

    public void toggleAvailability(Dish dish) {
        if (dish == null || dish.getId() == null) {
            return;
        }
        if (!canEditMenu()) {
            if (view != null) {
                view.showError("Недостаточно прав для изменения меню. Только менеджер или директор могут редактировать меню.");
            }
            return;
        }
        if (dish.isActive()) {
            // Пока поддерживается только деактивация через сервис
            menuService.deactivateDish(dish.getId());
        } else {
            // Если понадобится включать обратно, это лучше добавить в MenuService
            dish.setActive(true);
            menuService.updateDish(dish);
        }
        loadMenu();
    }

    /**
     * Check if current user can edit menu.
     */
    public boolean canEditMenu() {
        if (sessionContext == null) {
            return false;
        }
        SessionContext.Role role = sessionContext.getRole();
        return role == SessionContext.Role.MANAGER
                || role == SessionContext.Role.DIRECTOR;
    }

    /**
     * Update UI state based on role restrictions.
     */
    private void updateUIState() {
        if (view == null) {
            return;
        }
        boolean canEdit = canEditMenu();
        view.setEditingEnabled(canEdit);
    }
}

