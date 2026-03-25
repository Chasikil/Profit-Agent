package com.restaurant.pos.ui.components;

import com.restaurant.pos.model.AppContext;
import com.restaurant.pos.model.UserRole;
import com.restaurant.pos.ui.context.SessionContext;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Sidebar extends VBox {

    public enum Section {
        DASHBOARD,
        ORDERS,
        MENU,
        INVENTORY,
        EMPLOYEES,
        ORDER_HISTORY
    }

    private final Consumer<Section> onSectionSelected;
    private final Map<Section, Button> buttons = new HashMap<>();
    private final AppContext appContext;
    private Section activeSection;

    public Sidebar(Consumer<Section> onSectionSelected) {
        this(onSectionSelected, new AppContext());
    }

    public Sidebar(Consumer<Section> onSectionSelected, AppContext appContext) {
        this.onSectionSelected = onSectionSelected;
        this.appContext = appContext;
        initialize();
    }

    public void setSessionContext(SessionContext sessionContext) {
        updateVisibility();
    }

    private void initialize() {
        setPadding(new Insets(16));
        setSpacing(8);
        setPrefWidth(200);
        getStyleClass().add("sidebar");
        setAlignment(Pos.TOP_LEFT);

        buttons.put(Section.DASHBOARD, createNavButton("Dashboard", Section.DASHBOARD));
        buttons.put(Section.ORDERS, createNavButton("Orders", Section.ORDERS));
        buttons.put(Section.MENU, createNavButton("Menu", Section.MENU));
        buttons.put(Section.INVENTORY, createNavButton("Inventory", Section.INVENTORY));
        buttons.put(Section.EMPLOYEES, createNavButton("Employees", Section.EMPLOYEES));
        buttons.put(Section.ORDER_HISTORY, createNavButton("Order History", Section.ORDER_HISTORY));

        getChildren().addAll(buttons.values());
        updateVisibility();
        // По умолчанию активен Dashboard
        setActiveSection(Section.DASHBOARD);
    }

    private Button createNavButton(String text, Section section) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("sidebar-button");
        button.setOnAction(e -> {
            setActiveSection(section);
            onSectionSelected.accept(section);
        });
        return button;
    }

    /**
     * Обновляет визуальное выделение активного пункта меню.
     */
    private void setActiveSection(Section section) {
        this.activeSection = section;
        buttons.forEach((sec, btn) -> {
            if (sec == activeSection) {
                if (!btn.getStyleClass().contains("sidebar-button-active")) {
                    btn.getStyleClass().add("sidebar-button-active");
                }
            } else {
                btn.getStyleClass().remove("sidebar-button-active");
            }
        });
    }

    /**
     * Update button visibility based on current role.
     */
    private void updateVisibility() {
        // Determine current user role from application context (default ADMIN).
        UserRole role = appContext != null ? appContext.getCurrentRole() : UserRole.ADMIN;

        // Hide all buttons by default
        buttons.values().forEach(btn -> btn.setVisible(false));

        if (role == UserRole.WAITER) {
            // WAITER: Only Orders (create orders, add items, process payments)
            showButton(Section.ORDERS);
        } else if (role == UserRole.ADMIN) {
            // ADMIN: Full access
            showButton(Section.DASHBOARD);
            showButton(Section.ORDERS);
            showButton(Section.MENU);
            showButton(Section.INVENTORY);
            showButton(Section.EMPLOYEES);
            showButton(Section.ORDER_HISTORY);
        } else {
            // Fallback: treat as ADMIN
            showButton(Section.DASHBOARD);
            showButton(Section.ORDERS);
            showButton(Section.MENU);
            showButton(Section.INVENTORY);
            showButton(Section.EMPLOYEES);
            showButton(Section.ORDER_HISTORY);
        }
    }

    private void showButton(Section section) {
        Button button = buttons.get(section);
        if (button != null) {
            button.setVisible(true);
        }
    }
}

