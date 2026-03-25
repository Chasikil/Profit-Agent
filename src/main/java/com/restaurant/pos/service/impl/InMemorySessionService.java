package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.Role;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.Session;
import com.restaurant.pos.service.SessionService;

public class InMemorySessionService implements SessionService {

    private Session currentSession;

    @Override
    public Session startSession(Employee employee) {
        Session session = new Session();
        session.setEmployee(employee);
        session.setRole(employee != null ? employee.getRole() : Role.WAITER);
        session.setActive(true);
        this.currentSession = session;
        return session;
    }

    @Override
    public void endSession() {
        if (currentSession != null) {
            currentSession.setActive(false);
        }
        currentSession = null;
    }

    @Override
    public boolean isSessionActive() {
        return currentSession != null && currentSession.isActive();
    }

    @Override
    public Session getCurrentSession() {
        return currentSession;
    }
}

