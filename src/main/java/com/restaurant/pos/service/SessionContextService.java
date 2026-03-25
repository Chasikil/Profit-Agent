package com.restaurant.pos.service;

import com.restaurant.pos.domain.enums.Role;
import com.restaurant.pos.domain.enums.ShiftStatus;
import com.restaurant.pos.domain.model.Employee;

/**
 * Unified service for session and shift management.
 * Provides a single interface for controllers to access session state.
 */
public interface SessionContextService {

    /**
     * Get current logged-in employee.
     * @return current employee or null if not logged in
     */
    Employee getCurrentEmployee();

    /**
     * Get current user role.
     * @return current role or null if not logged in
     */
    Role getCurrentRole();

    /**
     * Check if user is logged in.
     * @return true if session is active
     */
    boolean isLoggedIn();

    /**
     * Login with credentials.
     * @param login username/login
     * @param password password
     * @return true if login successful
     */
    boolean login(String login, String password);

    /**
     * Logout current user.
     */
    void logout();

    /**
     * Get current shift status for logged-in employee.
     * @return OPEN or CLOSED, or null if not logged in
     */
    ShiftStatus getShiftStatus();

    /**
     * Open shift for current employee.
     * @return true if shift opened successfully
     */
    boolean openShift();

    /**
     * Close shift for current employee.
     * @return true if shift closed successfully
     */
    boolean closeShift();

    /**
     * Check if current employee has an open shift.
     * @return true if shift is open
     */
    boolean isShiftOpen();
}
