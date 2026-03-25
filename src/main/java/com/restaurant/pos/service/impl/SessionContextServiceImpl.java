package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.Role;
import com.restaurant.pos.domain.enums.ShiftStatus;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.Session;
import com.restaurant.pos.service.AuthService;
import com.restaurant.pos.service.SessionContextService;
import com.restaurant.pos.service.SessionService;
import com.restaurant.pos.service.ShiftService;

/**
 * Implementation of SessionContextService.
 * Wraps AuthService, SessionService, and ShiftService to provide unified session management.
 */
public class SessionContextServiceImpl implements SessionContextService {

    private final AuthService authService;
    private final SessionService sessionService;
    private final ShiftService shiftService;

    public SessionContextServiceImpl(AuthService authService,
                                     SessionService sessionService,
                                     ShiftService shiftService) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.shiftService = shiftService;
    }

    @Override
    public Employee getCurrentEmployee() {
        Session session = getCurrentSession();
        return session != null ? session.getEmployee() : null;
    }

    @Override
    public Role getCurrentRole() {
        Session session = getCurrentSession();
        if (session != null && session.getRole() != null) {
            return session.getRole();
        }
        Employee employee = getCurrentEmployee();
        return employee != null ? employee.getRole() : null;
    }

    @Override
    public boolean isLoggedIn() {
        return sessionService.isSessionActive();
    }

    @Override
    public boolean login(String login, String password) {
        Session session = authService.login(login, password);
        return session != null;
    }

    @Override
    public void logout() {
        Employee employee = getCurrentEmployee();
        if (employee != null && employee.getId() != null) {
            // Optionally close shift on logout
            shiftService.closeShift(employee.getId());
        }
        authService.logout();
    }

    @Override
    public ShiftStatus getShiftStatus() {
        Employee employee = getCurrentEmployee();
        if (employee == null || employee.getId() == null) {
            return null;
        }
        return shiftService.isShiftOpen(employee.getId()) ? ShiftStatus.OPEN : ShiftStatus.CLOSED;
    }

    @Override
    public boolean openShift() {
        Employee employee = getCurrentEmployee();
        if (employee == null || employee.getId() == null) {
            return false;
        }
        return shiftService.openShift(employee.getId()) != null;
    }

    @Override
    public boolean closeShift() {
        Employee employee = getCurrentEmployee();
        if (employee == null || employee.getId() == null) {
            return false;
        }
        return shiftService.closeShift(employee.getId()) != null;
    }

    @Override
    public boolean isShiftOpen() {
        Employee employee = getCurrentEmployee();
        if (employee == null || employee.getId() == null) {
            return false;
        }
        return shiftService.isShiftOpen(employee.getId());
    }

    private Session getCurrentSession() {
        return sessionService.getCurrentSession();
    }
}
