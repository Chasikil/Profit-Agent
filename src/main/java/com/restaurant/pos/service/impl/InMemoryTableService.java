package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.model.Table;
import com.restaurant.pos.service.TableService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryTableService implements TableService {

    private final List<Table> tables = new ArrayList<>();

    @Override
    public void initializeDefaultTables() {
        if (!tables.isEmpty()) {
            return;
        }
        for (int i = 1; i <= 5; i++) {
            tables.add(new Table(i));
        }
    }

    public void loadTables(java.util.List<Table> loaded) {
        if (loaded == null) return;
        tables.clear();
        tables.addAll(loaded);
    }

    @Override
    public List<Table> getAllTables() {
        return new ArrayList<>(tables);
    }

    @Override
    public List<Table> getFreeTables() {
        return tables.stream()
                .filter(t -> !t.isOccupied())
                .collect(Collectors.toList());
    }

    @Override
    public void markOccupied(int tableNumber) {
        tables.stream()
                .filter(t -> t.getNumber() == tableNumber)
                .findFirst()
                .ifPresent(t -> t.setOccupied(true));
    }

    @Override
    public void markFree(int tableNumber) {
        tables.stream()
                .filter(t -> t.getNumber() == tableNumber)
                .findFirst()
                .ifPresent(t -> t.setOccupied(false));
    }

    @Override
    public void ensureMinimumTables(int minCount) {
        if (minCount <= 0) {
            return;
        }
        // Ensure table numbers 1..minCount exist, and remove any extras > minCount
        java.util.Set<Integer> existing = new java.util.HashSet<>();
        for (Table t : tables) {
            if (t != null) existing.add(t.getNumber());
        }
        for (int i = 1; i <= minCount; i++) {
            if (!existing.contains(i)) {
                tables.add(new Table(i));
            }
        }
        tables.removeIf(t -> t == null || t.getNumber() <= 0 || t.getNumber() > minCount);
    }
}
