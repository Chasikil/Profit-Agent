package com.restaurant.pos.ui.controller;

import com.restaurant.pos.domain.model.Product;
import com.restaurant.pos.model.AppContext;
import com.restaurant.pos.model.UserRole;
import com.restaurant.pos.service.InventoryService;
import com.restaurant.pos.ui.view.InventoryView;

import java.math.BigDecimal;
import java.util.List;

public class InventoryController {

    private final InventoryService inventoryService;
    private final AppContext appContext;

    private InventoryView view;

    public InventoryController(InventoryService inventoryService, AppContext appContext) {
        this.inventoryService = inventoryService;
        this.appContext = appContext;
    }

    public InventoryView getView() {
        if (view == null) {
            view = new InventoryView(this);
            refresh();
            updateUIState();
        }
        return view;
    }

    public void refresh() {
        if (!canAccessInventory()) {
            if (view != null) {
                view.showError("Доступ запрещён. Только администратор может работать со складом.");
            }
            return;
        }
        List<Product> products = inventoryService.getAllProducts();
        if (view != null) {
            view.updateTable(products);
        }
    }

    /**
     * Update UI state based on role restrictions.
     */
    private void updateUIState() {
        if (view == null) {
            return;
        }
        boolean hasAccess = canAccessInventory();
        view.setButtonsEnabled(hasAccess);
        if (!hasAccess) {
            view.showError("Доступ запрещён. Только администратор может работать со складом.");
        }
    }

    /**
     * Check if current user can access inventory. ADMIN has full access.
     */
    private boolean canAccessInventory() {
        return appContext != null && appContext.getCurrentRole() == UserRole.ADMIN;
    }

    public void onAddProduct(String name, String unit, BigDecimal quantity, BigDecimal costPerUnit) {
        if (!canModifyInventory()) {
            if (view != null) {
                view.showError("Недостаточно прав для добавления товара на склад.");
            }
            return;
        }
        Product product = new Product();
        product.setName(name);
        product.setUnit(unit);
        product.setQuantityInStock(quantity);
        product.setCostPerUnit(costPerUnit);
        inventoryService.addProduct(product);
        refresh();
    }

    public void onWriteOffProduct(Long productId, BigDecimal quantity, String reason) {
        if (!canModifyInventory()) {
            if (view != null) {
                view.showError("Недостаточно прав для списания товара со склада.");
            }
            return;
        }
        inventoryService.writeOffProduct(productId, quantity, reason);
        refresh();
    }

    private boolean canModifyInventory() {
        return appContext != null && appContext.getCurrentRole() == UserRole.ADMIN;
    }
}

