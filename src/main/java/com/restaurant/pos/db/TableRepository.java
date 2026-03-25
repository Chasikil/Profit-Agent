package com.restaurant.pos.db;

import com.restaurant.pos.domain.model.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableRepository {

    public List<Table> findAll() {
        List<Table> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT name, status FROM tables ORDER BY CAST(REPLACE(name,'Table ','') AS INTEGER)");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                String status = rs.getString("status");
                int number = parseTableNumber(name);
                Table t = new Table(number);
                t.setOccupied(!"FREE".equalsIgnoreCase(status));
                list.add(t);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void save(Table t) {
        if (t == null) return;
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO tables (name, status) VALUES (?,?)")) {
            ps.setString(1, "Table " + t.getNumber());
            ps.setString(2, t.isOccupied() ? "OCCUPIED" : "FREE");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateOccupied(int number, boolean occupied) {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE tables SET status=? WHERE name=?")) {
            ps.setString(1, occupied ? "OCCUPIED" : "FREE");
            ps.setString(2, "Table " + number);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int count() {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM tables");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Ensure tables are strictly Table 1..Table count (no more, no less). */
    public void ensureExactTables(int count) {
        if (count <= 0) return;
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            // Insert missing
            for (int i = 1; i <= count; i++) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT OR IGNORE INTO tables (name, status) VALUES (?,?)"
                )) {
                    ps.setString(1, "Table " + i);
                    ps.setString(2, "FREE");
                    ps.executeUpdate();
                }
            }
            // Delete extras
            StringBuilder sb = new StringBuilder("DELETE FROM tables WHERE name NOT IN (");
            for (int i = 1; i <= count; i++) {
                if (i > 1) sb.append(",");
                sb.append("?");
            }
            sb.append(")");
            try (PreparedStatement del = c.prepareStatement(sb.toString())) {
                for (int i = 1; i <= count; i++) {
                    del.setString(i, "Table " + i);
                }
                del.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static int parseTableNumber(String name) {
        if (name == null) return 0;
        // expected: "Table N"
        String trimmed = name.trim();
        int idx = trimmed.lastIndexOf(' ');
        if (idx < 0 || idx == trimmed.length() - 1) return 0;
        try {
            return Integer.parseInt(trimmed.substring(idx + 1));
        } catch (Exception e) {
            return 0;
        }
    }
}
