package com.restaurant.pos.service;

import java.time.LocalDateTime;

/**
 * Service for tracking employee work time.
 * Manages shift start/end and worked hours accumulation.
 */
public interface WorkTimeService {

    /**
     * Start shift for employee.
     * Records shift start time.
     * 
     * @param employeeId employee ID
     * @return shift start time or null if employee not found
     */
    LocalDateTime startShift(Long employeeId);

    /**
     * End shift for employee.
     * Calculates worked hours and adds to employee's total.
     * 
     * @param employeeId employee ID
     * @return worked hours for this shift or 0.0 if shift not started or employee not found
     */
    double endShift(Long employeeId);

    /**
     * Add worked hours directly to employee's total.
     * Useful for manual time entry or corrections.
     * 
     * @param employeeId employee ID
     * @param hours hours to add (can be negative for corrections)
     * @return true if successful, false if employee not found
     */
    boolean addWorkedHours(Long employeeId, double hours);

    /**
     * Get current shift start time for employee.
     * 
     * @param employeeId employee ID
     * @return shift start time or null if no active shift
     */
    LocalDateTime getCurrentShiftStart(Long employeeId);

    /**
     * Check if employee has active shift.
     * 
     * @param employeeId employee ID
     * @return true if shift is active
     */
    boolean hasActiveShift(Long employeeId);
}
