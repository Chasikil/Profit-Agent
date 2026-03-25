package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.model.Shift;
import com.restaurant.pos.service.TimeTrackingService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTimeTrackingService implements TimeTrackingService {

    private final List<Shift> registeredShifts = new ArrayList<>();

    @Override
    public void registerShift(Shift shift) {
        if (shift == null) {
            return;
        }
        registeredShifts.add(shift);
    }

    @Override
    public double getWorkedHours(Long employeeId, LocalDateTime start, LocalDateTime end) {
        if (employeeId == null || start == null || end == null || end.isBefore(start)) {
            return 0.0;
        }
        double totalHours = 0.0;
        for (Shift shift : registeredShifts) {
            if (shift.getEmployeeId() == null || !employeeId.equals(shift.getEmployeeId())) {
                continue;
            }
            LocalDateTime shiftStart = shift.getStartTime();
            LocalDateTime shiftEnd = shift.getEndTime();
            if (shiftStart == null || shiftEnd == null) {
                continue;
            }
            LocalDateTime periodStart = shiftStart.isBefore(start) ? start : shiftStart;
            LocalDateTime periodEnd = shiftEnd.isAfter(end) ? end : shiftEnd;
            if (!periodEnd.isAfter(periodStart)) {
                continue;
            }
            long minutes = Duration.between(periodStart, periodEnd).toMinutes();
            totalHours += minutes / 60.0;
        }
        return totalHours;
    }
}

