package com.restaurant.pos.db;

import com.restaurant.pos.domain.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, unit, quantity_in_stock, cost_per_unit, minimum_threshold FROM product")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Product findById(Long id) {
        if (id == null) return null;
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, unit, quantity_in_stock, cost_per_unit, minimum_threshold FROM product WHERE id = ?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(Product p) {
        if (p == null) return;
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            if (p.getId() == null) {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO product (name, unit, quantity_in_stock, cost_per_unit, minimum_threshold) VALUES (?,?,?,?,?)")) {
                    ps.setString(1, p.getName());
                    ps.setString(2, p.getUnit());
                    ps.setBigDecimal(3, p.getQuantityInStock());
                    ps.setBigDecimal(4, p.getCostPerUnit());
                    ps.setBigDecimal(5, p.getMinimumThreshold());
                    ps.executeUpdate();
                    try (Statement keySt = c.createStatement();
                         ResultSet keys = keySt.executeQuery("SELECT last_insert_rowid()")) {
                        if (keys.next()) {
                            p.setId(keys.getLong(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("UPDATE product SET name=?, unit=?, quantity_in_stock=?, cost_per_unit=?, minimum_threshold=? WHERE id=?")) {
                    ps.setString(1, p.getName());
                    ps.setString(2, p.getUnit());
                    ps.setBigDecimal(3, p.getQuantityInStock());
                    ps.setBigDecimal(4, p.getCostPerUnit());
                    ps.setBigDecimal(5, p.getMinimumThreshold());
                    ps.setLong(6, p.getId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int count() {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM product");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setUnit(rs.getString("unit"));
        p.setQuantityInStock(BigDecimal.valueOf(rs.getDouble("quantity_in_stock")));
        p.setCostPerUnit(BigDecimal.valueOf(rs.getDouble("cost_per_unit")));
        double thresh = rs.getDouble("minimum_threshold");
        p.setMinimumThreshold(rs.wasNull() ? null : BigDecimal.valueOf(thresh));
        return p;
    }
}
