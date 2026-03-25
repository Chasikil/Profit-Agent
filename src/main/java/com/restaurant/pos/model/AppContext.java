package com.restaurant.pos.model;

import com.restaurant.pos.domain.model.Employee;

/**
 * Application-wide context holding global state.
 * Passed explicitly to controllers (no static globals, no UI code).
 */
public class AppContext {

    private UserRole currentRole;
    private final InMemoryOrderStore orderStore;
    /** Selected waiter for new orders; set by waiter selection dialog. */
    private Employee currentEmployee;

    public AppContext() {
        this.currentRole = UserRole.ADMIN;
        this.orderStore = new InMemoryOrderStore();
    }

    public UserRole getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(UserRole currentRole) {
        this.currentRole = currentRole != null ? currentRole : UserRole.ADMIN;
    }

    public InMemoryOrderStore getOrderStore() {
        return orderStore;
    }

    public Employee getCurrentEmployee() {
        return currentEmployee;
    }

    public void setCurrentEmployee(Employee currentEmployee) {
        this.currentEmployee = currentEmployee;
    }
}

