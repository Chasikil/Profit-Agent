package com.restaurant.pos.ui.context;

import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.service.SessionContextService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * UI context for session state with JavaFX properties.
 * Can work standalone or delegate to SessionContextService.
 */
public class SessionContext {

    public enum Role {
        WAITER,
        MANAGER,
        CHEF,
        DIRECTOR
    }

    public enum ShiftStatus {
        OPEN,
        CLOSED
    }

    private SessionContextService sessionContextService;
    private final StringProperty fullName = new SimpleStringProperty(this, "fullName", "");
    private final ObjectProperty<Role> role = new SimpleObjectProperty<>(this, "role", null);
    private final ObjectProperty<ShiftStatus> shiftStatus = new SimpleObjectProperty<>(this, "shiftStatus", ShiftStatus.CLOSED);

    public SessionContext() {
    }

    public SessionContext(SessionContextService sessionContextService) {
        this.sessionContextService = sessionContextService;
    }

    // ===================== Service delegation methods =====================

    /**
     * Get current logged-in employee.
     * Delegates to SessionContextService if available.
     */
    public Employee getCurrentEmployee() {
        if (sessionContextService != null) {
            return sessionContextService.getCurrentEmployee();
        }
        return null;
    }

    /**
     * Get current user role.
     * Delegates to SessionContextService if available.
     */
    public Role getCurrentRole() {
        if (sessionContextService != null) {
            com.restaurant.pos.domain.enums.Role domainRole = sessionContextService.getCurrentRole();
            return domainRole != null ? toUIRole(domainRole) : null;
        }
        return role.get();
    }

    /**
     * Check if user is logged in.
     * Delegates to SessionContextService if available.
     */
    public boolean isLoggedIn() {
        if (sessionContextService != null) {
            return sessionContextService.isLoggedIn();
        }
        return fullName.get() != null && !fullName.get().isEmpty();
    }

    /**
     * Login with credentials.
     * Delegates to SessionContextService if available.
     */
    public boolean login(String login, String password) {
        if (sessionContextService != null) {
            boolean success = sessionContextService.login(login, password);
            if (success) {
                syncFromService();
            }
            return success;
        }
        // Fallback: manual login (for testing)
        setFullName(login);
        setRole(Role.WAITER);
        return true;
    }

    /**
     * Logout current user.
     * Delegates to SessionContextService if available.
     */
    public void logout() {
        if (sessionContextService != null) {
            sessionContextService.logout();
            syncFromService();
        } else {
            setFullName("");
            setRole(null);
            setShiftStatus(ShiftStatus.CLOSED);
        }
    }

    /**
     * Get current shift status.
     * Delegates to SessionContextService if available.
     */
    public ShiftStatus getShiftStatus() {
        if (sessionContextService != null) {
            com.restaurant.pos.domain.enums.ShiftStatus domainStatus = sessionContextService.getShiftStatus();
            return domainStatus != null ? toUIShiftStatus(domainStatus) : ShiftStatus.CLOSED;
        }
        return shiftStatus.get();
    }

    /**
     * Sync UI properties from SessionContextService.
     */
    public void syncFromService() {
        if (sessionContextService != null) {
            Employee employee = sessionContextService.getCurrentEmployee();
            if (employee != null) {
                setFullName(employee.getFullName());
                com.restaurant.pos.domain.enums.Role domainRole = sessionContextService.getCurrentRole();
                setRole(domainRole != null ? toUIRole(domainRole) : null);
            } else {
                setFullName("");
                setRole(null);
            }
            com.restaurant.pos.domain.enums.ShiftStatus domainStatus = sessionContextService.getShiftStatus();
            setShiftStatus(domainStatus != null ? toUIShiftStatus(domainStatus) : ShiftStatus.CLOSED);
        }
    }

    // ===================== JavaFX properties =====================

    public String getFullName() {
        return fullName.get();
    }

    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    public Role getRole() {
        return role.get();
    }

    public void setRole(Role role) {
        this.role.set(role);
    }

    public ObjectProperty<Role> roleProperty() {
        return role;
    }

    public void setShiftStatus(ShiftStatus shiftStatus) {
        this.shiftStatus.set(shiftStatus);
    }

    public ObjectProperty<ShiftStatus> shiftStatusProperty() {
        return shiftStatus;
    }

    public void setSessionContextService(SessionContextService sessionContextService) {
        this.sessionContextService = sessionContextService;
    }

    // ===================== Helper methods =====================

    private Role toUIRole(com.restaurant.pos.domain.enums.Role domainRole) {
        if (domainRole == null) {
            return null;
        }
        switch (domainRole) {
            case WAITER:
                return Role.WAITER;
            case MANAGER:
                return Role.MANAGER;
            case CHEF:
                return Role.CHEF;
            case DIRECTOR:
                return Role.DIRECTOR;
            default:
                return null;
        }
    }

    private ShiftStatus toUIShiftStatus(com.restaurant.pos.domain.enums.ShiftStatus domainStatus) {
        if (domainStatus == null) {
            return ShiftStatus.CLOSED;
        }
        return domainStatus == com.restaurant.pos.domain.enums.ShiftStatus.OPEN ? ShiftStatus.OPEN : ShiftStatus.CLOSED;
    }
}

