package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Shift;

import java.time.LocalDateTime;

public interface TimeTrackingService {

    void registerShift(Shift shift);

    double getWorkedHours(Long employeeId, LocalDateTime start, LocalDateTime end);
}

