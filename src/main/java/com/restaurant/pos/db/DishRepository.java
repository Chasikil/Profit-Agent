package com.restaurant.pos.db;

import com.restaurant.pos.domain.model.Dish;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishRepository {

    public List<Dish> findAll() {
        List<Dish> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, description, category, sale_price, active FROM dish");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Dish findById(Long id) {
        if (id == null) return null;
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, description, category, sale_price, active FROM dish WHERE id = ?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(Dish d) {
        if (d == null) return;
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            if (d.getId() == null) {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO dish (name, description, category, sale_price, active) VALUES (?,?,?,?,?)")) {
                    ps.setString(1, d.getName());
                    ps.setString(2, d.getDescription());
                    ps.setString(3, d.getCategory());
                    ps.setBigDecimal(4, d.getSalePrice());
                    ps.setInt(5, d.isActive() ? 1 : 0);
                    ps.executeUpdate();
                    try (Statement keySt = c.createStatement();
                         ResultSet keys = keySt.executeQuery("SELECT last_insert_rowid()")) {
                        if (keys.next()) {
                            d.setId(keys.getLong(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("UPDATE dish SET name=?, description=?, category=?, sale_price=?, active=? WHERE id=?")) {
                    ps.setString(1, d.getName());
                    ps.setString(2, d.getDescription());
                    ps.setString(3, d.getCategory());
                    ps.setBigDecimal(4, d.getSalePrice());
                    ps.setInt(5, d.isActive() ? 1 : 0);
                    ps.setLong(6, d.getId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int count() {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM dish");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Dish mapRow(ResultSet rs) throws SQLException {
        Dish d = new Dish();
        d.setId(rs.getLong("id"));
        d.setName(rs.getString("name"));
        d.setDescription(rs.getString("description"));
        d.setCategory(rs.getString("category"));
        double sp = rs.getDouble("sale_price");
        d.setSalePrice(rs.wasNull() ? null : BigDecimal.valueOf(sp));
        d.setActive(rs.getInt("active") == 1);
        return d;
    }
}
