package com.restaurant.pos.service.impl;

import com.restaurant.pos.auth.AuthenticationException;
import com.restaurant.pos.domain.enums.Role;
import com.restaurant.pos.model.Session;
import com.restaurant.pos.model.User;
import com.restaurant.pos.model.UserRole;
import com.restaurant.pos.service.AuthService;
import com.restaurant.pos.service.UserAuthService;

/**
 * UI auth adapter that delegates to domain AuthService/SessionService.
 * Uses real Employee records (Ivan, Anna, Maria, Admin) seeded in EmployeeSeeder.
 */
public class InMemoryUserAuthService implements UserAuthService {

    private final AuthService authService;

    public InMemoryUserAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public User login(String username, String password) throws AuthenticationException {
        if (username == null || username.isBlank()) {
            throw new AuthenticationException("Введите логин.");
        }
        if (password == null || password.isBlank()) {
            throw new AuthenticationException("Введите пароль.");
        }

        com.restaurant.pos.domain.model.Session domainSession = authService.login(username.trim(), password);
        if (domainSession == null || !domainSession.isActive() || domainSession.getEmployee() == null) {
            throw new AuthenticationException("Неверный логин или пароль.");
        }

        var emp = domainSession.getEmployee();
        Role role = emp.getRole();
        UserRole uiRole = role == Role.ADMIN ? UserRole.ADMIN : UserRole.WAITER;

        User uiUser = new User(emp.getLogin(), null, uiRole);
        Session.setCurrentUser(uiUser);
        return uiUser;
    }

    @Override
    public void logout() {
        authService.logout();
        Session.clear();
    }
}
