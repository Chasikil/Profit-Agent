package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.service.EmployeeStorageService;
import com.restaurant.pos.service.WorkTimeService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of WorkTimeService.
 * Tracks shift start times and accumulates worked hours in Employee model.
 */
public class InMemoryWorkTimeService implements WorkTimeService {

    private final EmployeeStorageService employeeStorage;
    private final Map<Long, LocalDateTime> shiftStartTimes = new HashMap<>();

    public InMemoryWorkTimeService(EmployeeStorageService employeeStorage) {
        if (employeeStorage == null) {
            throw new IllegalArgumentException("EmployeeStorageService cannot be null");
        }
        this.employeeStorage = employeeStorage;
    }

    @Override
    public LocalDateTime startShift(Long employeeId) {
        if (employeeId == null) {
            return null;
        }

        Employee employee = employeeStorage.getEmployeeById(employeeId);
        if (employee == null) {
            return null;
        }

        LocalDateTime startTime = LocalDateTime.now();
        shiftStartTimes.put(employeeId, startTime);
        return startTime;
    }

    @Override
    public double endShift(Long employeeId) {
        if (employeeId == null) {
            return 0.0;
        }

        Employee employee = employeeStorage.getEmployeeById(employeeId);
        if (employee == null) {
            return 0.0;
        }

        LocalDateTime startTime = shiftStartTimes.remove(employeeId);
        if (startTime == null) {
            return 0.0;
        }

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        double hours = duration.toMinutes() / 60.0;

        // Add worked hours to employee's total
        double currentHours = employee.getWorkedHours();
        employee.setWorkedHours(currentHours + hours);

        return hours;
    }

    @Override
    public boolean addWorkedHours(Long employeeId, double hours) {
        if (employeeId == null) {
            return false;
        }

        Employee employee = employeeStorage.getEmployeeById(employeeId);
        if (employee == null) {
            return false;
        }

        double currentHours = employee.getWorkedHours();
        employee.setWorkedHours(currentHours + hours);
        return true;
    }

    @Override
    public LocalDateTime getCurrentShiftStart(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return shiftStartTimes.get(employeeId);
    }

    @Override
    public boolean hasActiveShift(Long employeeId) {
        if (employeeId == null) {
            return false;
        }
        return shiftStartTimes.containsKey(employeeId);
    }
}
