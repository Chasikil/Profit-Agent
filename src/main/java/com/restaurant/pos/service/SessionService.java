package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.Session;

public interface SessionService {

    Session startSession(Employee employee);

    void endSession();

    boolean isSessionActive();

    Session getCurrentSession();
}

