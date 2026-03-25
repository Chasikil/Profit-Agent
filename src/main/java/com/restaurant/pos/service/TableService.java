package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Table;

import java.util.List;

/**
 * Service for managing restaurant tables.
 */
public interface TableService {

    /** Initialize 10 tables (1–10) if not already done. */
    void initializeDefaultTables();

    List<Table> getAllTables();

    List<Table> getFreeTables();

    void markOccupied(int tableNumber);

    void markFree(int tableNumber);

    /**
     * Ensure that there are at least minCount tables in memory.
     * Implementations may create additional tables if needed.
     */
    void ensureMinimumTables(int minCount);
}
