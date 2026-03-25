package com.restaurant.pos.db;

import com.restaurant.pos.domain.model.Receipt;
import com.restaurant.pos.domain.model.ReceiptItem;

import java.sql.*;

public class ReceiptRepository {

    public void save(Receipt r) {
        if (r == null) return;
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            if (r.getId() == null) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO receipts (table_number, waiter_name, time, total) VALUES (?,?,?,?)"
                )) {
                    ps.setInt(1, r.getTableNumber());
                    ps.setString(2, r.getWaiterName());
                    ps.setString(3, r.getTime() != null ? r.getTime().toString() : null);
                    ps.setBigDecimal(4, r.getTotal());
                    ps.executeUpdate();
                    try (Statement keySt = c.createStatement();
                         ResultSet keys = keySt.executeQuery("SELECT last_insert_rowid()")) {
                        if (keys.next()) {
                            r.setId(keys.getLong(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE receipts SET table_number=?, waiter_name=?, time=?, total=? WHERE id=?"
                )) {
                    ps.setInt(1, r.getTableNumber());
                    ps.setString(2, r.getWaiterName());
                    ps.setString(3, r.getTime() != null ? r.getTime().toString() : null);
                    ps.setBigDecimal(4, r.getTotal());
                    ps.setLong(5, r.getId());
                    ps.executeUpdate();
                }
                try (PreparedStatement del = c.prepareStatement("DELETE FROM receipt_item WHERE receipt_id=?")) {
                    del.setLong(1, r.getId());
                    del.executeUpdate();
                }
            }

            if (r.getId() != null && r.getItems() != null) {
                for (ReceiptItem item : r.getItems()) {
                    if (item == null) continue;
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO receipt_item (receipt_id, dish_name, quantity, price) VALUES (?,?,?,?)"
                    )) {
                        ps.setLong(1, r.getId());
                        ps.setString(2, item.getDishName());
                        ps.setInt(3, item.getQuantity());
                        ps.setBigDecimal(4, item.getPrice());
                        ps.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

